package searcher;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;
import data_containers.indexer.weights_calculation.searching.SearchingCalculations;
import io.data_containers.loaders.bulk_load.document_registry.DocRegBulkLoader;
import io.data_containers.loaders.bulk_load.indexer.IndexerBulkLoader;
import tokenizer.BaseTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Searcher<D extends Document<Float>,
    I extends TermInfoBase<Float, D>
    > {

    private BaseTokenizer tokenizer;

    /**
     * Defines variant to calculate the weights
     */
    private SearchingCalculations<String, Float, D, I> calculations;

    private TreeMap<Integer, String> docRegMetadata;

    private TreeMap<String, String> indexerMetadata;

    private TreeMap<Integer, DocRegBulkLoader.DocumentRegistry> docRegsInMemory;

    private TreeMap<String, Map<String, Object>> indexersInMemory;

    private DocRegBulkLoader docRegLoader;

    private IndexerBulkLoader<String, Float, D, I> indexerLoader;

    private int maxDocRegsInMemory;

    private int maxIndexersInMemory;

    /**
     * Stores the number of times a document registry file was consulted
     */
    private Map<Integer, Integer> docRegsRanks;

    /**
     * Stores the number of times a indexer file was consulted with success
     */
    private Map<String, Integer> indexersRanks;

    /**
     * Maximum number of documents to return
     */
    private int K;

    public Searcher(
        BaseTokenizer tokenizer,
        SearchingCalculations<String, Float, D, I> calculations,
        TreeMap<Integer, String> docRegMetadata,
        TreeMap<String, String> indexerMetadata,
        DocRegBulkLoader docRegLoader,
        IndexerBulkLoader<String, Float, D, I> indexerLoader,
        int maxDocRegsInMemory,
        int maxIndexersInMemory,
        int K
        ) {
        this.tokenizer = tokenizer;
        this.calculations = calculations;
        this.docRegMetadata = docRegMetadata;
        this.indexerMetadata = indexerMetadata;
        this.docRegLoader = docRegLoader;
        this.indexerLoader = indexerLoader;
        this.maxDocRegsInMemory = maxDocRegsInMemory;
        this.maxIndexersInMemory = maxIndexersInMemory;
        this.K = K;

        this.docRegsInMemory = new TreeMap<>();
        this.indexersInMemory = new TreeMap<>();

        this.indexersRanks = new HashMap<>();
        this.docRegsRanks = new HashMap<>();
    }

    /**
     * Method to execute the ranked retrieval search
     */
    public List<String> queryIndex(String query) {
        Map<String, Float> termFrequencyWeights = calculations.calculateTermFrequencyWeights(
            tokenizer.tokenizeString(query)
        );

        List<DocumentRank> relevantDocuments;
        {List<String> terms = new ArrayList<>(termFrequencyWeights.size());
        List<List<D>> postingLists = new ArrayList<>(termFrequencyWeights.size());
        getPostingListsOfQueryTerms(termFrequencyWeights, terms, postingLists);

        relevantDocuments = getRelevantDocuments(terms, postingLists, termFrequencyWeights);}

        return translateDocumentIds(relevantDocuments);
    }

    /**
     * Consults the indexers in memory, and disk if necessary, and gets the posting lists
     *  of the terms present on the query
     */
    private void getPostingListsOfQueryTerms(Map<String, Float> termFrequencyWeights, List<String> terms, List<List<D>> postingLists) {
        termFrequencyWeights.keySet().stream().sorted(String::compareTo).forEach(term -> {
            float frequency = termFrequencyWeights.get(term);
            Map.Entry<String, Map<String, Object>> indexerEntry = indexersInMemory.floorEntry(term);

            I termInfo = indexerEntry == null ? null : indexerLoader.getValue(indexerEntry.getValue(), term);

            if (indexerEntry != null && termInfo != null) {
                // in memory

                // increase indexer usages
                indexersRanks.put(indexerEntry.getKey(), indexersRanks.get(indexerEntry.getKey()) + 1);

                termFrequencyWeights.put( // apply idf
                    term,
                    calculations.applyDocumentFrequencyWeights(
                        frequency,
                        termInfo
                    )
                );

                terms.add(term);
                postingLists.add(termInfo.getPostingList());
            }
            else {
                // not in memory

                Map.Entry<String, String> indexFilenameEntry = indexerMetadata.floorEntry(term);
                String indexFilename = indexFilenameEntry == null ? null : indexFilenameEntry.getValue();

                if (indexFilename != null && !indexersInMemory.containsKey(indexFilename)) {
                    // load from disk

                    checkIfCanLoadIndexerFromDisk();

                    Map<String, Object> invertedIndex = null;
                    try {
                        invertedIndex = indexerLoader.load(indexFilename);
                    } catch (IOException e) {
                        System.err.println("ERROR while loading indexer file " + indexFilename);
                        e.printStackTrace();
                        System.exit(2);
                    }

                    // store index in memory
                    indexersInMemory.put(indexFilenameEntry.getKey(), invertedIndex);

                    if (!indexersRanks.containsKey(indexFilenameEntry.getKey())) {
                        indexersRanks.put(indexFilenameEntry.getKey(), 0);
                    }

                    termInfo = indexerLoader.getValue(invertedIndex, term);
                    if (termInfo != null) {
                        terms.add(term);
                        postingLists.add(termInfo.getPostingList());

                        termFrequencyWeights.put( // apply idf
                            term,
                            calculations.applyDocumentFrequencyWeights(
                                frequency,
                                termInfo
                            )
                        );

                        // increase indexer usages
                        indexersRanks.put(indexFilenameEntry.getKey(), indexersRanks.get(indexFilenameEntry.getKey()) + 1);
                    }
                    /*else {
                        // term wasn't indexed
                    }*/
                }
                /*else {
                    // term wasn't indexed
                }*/
            }
        });

        // apply normalization on query
        terms.forEach(term -> {
            termFrequencyWeights.put(
                term,
                calculations.applyNormalization(termFrequencyWeights.get(term))
            );
        });

        calculations.resetNormalization();
    }

    /**
     * Iterates over the posting lists to calculate the weights for each document
     * To get the terms for each document we take advantage of the posting lists
     *  being sorted by document id get the documents with common lowest document ids.
     * This way we only need to iterate over the posting lists one time to calculate the
     *  weights for the documents
     */
    private List<DocumentRank> getRelevantDocuments(List<String> terms, List<List<D>> postingLists, Map<String, Float> termFrequencyWeights) {
        List<Integer> postingListsCurrentIndexes = new ArrayList<>(postingLists.size());
        for (int i = 0; i < postingLists.size(); i++) {
            postingListsCurrentIndexes.add(0);
        }

        List<DocumentRank> relevantDocuments = new ArrayList<>();

        // while the end of all posting lists wasn't reached
        while (!indexesAtEnd(postingListsCurrentIndexes, terms, postingLists)) {
            List<Integer> idxWithDocWithLowerID = new ArrayList<>(terms.size());

            // find the first document(s) among all the posting lists with
            //  the lowest docId
            idxWithDocWithLowerID.add(0);
            int lowestDocId = postingLists.get(0).get(postingListsCurrentIndexes.get(0)).getDocId();
            for (int i = 1; i < postingListsCurrentIndexes.size(); i++) {
                int docId = postingLists.get(i).get(postingListsCurrentIndexes.get(i)).getDocId();
                if (docId < lowestDocId) {
                    idxWithDocWithLowerID.clear();
                    idxWithDocWithLowerID.add(i);
                    lowestDocId = docId;
                }
                else if (docId == lowestDocId) {
                    idxWithDocWithLowerID.add(i);
                }
            }

            // to reduce the number of documents considered uncomment the if block
            //if (idxWithDocWithLowerID.size() >= termFrequencyWeights.size() * 0.5) {
                // calculate the score for this document
                float cumulativeScore = 0;
                int docId = postingLists.get(idxWithDocWithLowerID.get(0)).get(postingListsCurrentIndexes.get(idxWithDocWithLowerID.get(0))).getDocId();

                for (Integer idx : idxWithDocWithLowerID) {
                    float documentWeightForTerm = postingLists.get(idx).get(postingListsCurrentIndexes.get(idx)).getWeight();
                    float queryTermWeight = termFrequencyWeights.get(terms.get(idx));

                    cumulativeScore += queryTermWeight * documentWeightForTerm;
                }

                // and add it to the relevant documents list
                relevantDocuments.add(new DocumentRank(docId, cumulativeScore));
            //}

            for (Integer idx : idxWithDocWithLowerID) {
                int currentIdx = postingListsCurrentIndexes.get(idx);
                postingListsCurrentIndexes.set(idx, currentIdx + 1);
            }
        }

        relevantDocuments.sort((doc1, doc2) ->
            -doc1.weight.compareTo(doc2.weight) // decreasing order
        );

        return relevantDocuments.subList(0, Math.min(relevantDocuments.size(), K));
    }

    /**
     * Convert the internal id of the relevant document into their respective
     *  identifier by consulting the document registry files
     */
    private List<String> translateDocumentIds(List<DocumentRank> relevantDocuments) {
        List<String> relevantDocumentsIdentifiers = new ArrayList<>(relevantDocuments.size());
        IntStream.range(0, relevantDocuments.size())
            .forEach(i -> relevantDocumentsIdentifiers.add(""));

        IntStream.range(0, relevantDocuments.size())
            .mapToObj(idx -> new DocumentToTranslate(relevantDocuments.get(idx).docId, idx))
            .sorted(Comparator.comparingInt(doc -> doc.docId))
            .forEach(docToTranslate -> {
                int docId = docToTranslate.docId;

                Map.Entry<Integer, DocRegBulkLoader.DocumentRegistry> docRegEntry = docRegsInMemory.floorEntry(docId);
                Integer identifier = docRegEntry == null ? null : docRegEntry.getValue().translate(docId);

                if (docRegEntry != null && identifier != null) {
                    // in memory
                    docRegsRanks.put(docRegEntry.getKey(), docRegsRanks.get(docRegEntry.getKey()) + 1);
                }
                else {
                    // load from disk

                    Map.Entry<Integer, String> docRegFilenameEntry = docRegMetadata.floorEntry(docId);
                    String docRegFilename = docRegFilenameEntry.getValue();

                    checkIfCanLoadDocRegFromDisk();

                    DocRegBulkLoader.DocumentRegistry documentRegistry = null;
                    try {
                        documentRegistry = docRegLoader.load(docRegFilename, docRegFilenameEntry.getKey());
                    } catch (IOException e) {
                        System.err.println("ERROR while loading document registry file " + docRegFilename);
                        e.printStackTrace();
                        System.exit(2);
                    }

                    docRegsInMemory.put(docRegFilenameEntry.getKey(), documentRegistry);

                    // increment the number of usage of the file loaded
                    docRegsRanks.merge(docRegFilenameEntry.getKey(), 1, Integer::sum);

                    identifier = documentRegistry.translate(docId);

                }

                relevantDocumentsIdentifiers.set(docToTranslate.idx, identifier.toString());
            });

        return relevantDocumentsIdentifiers;
    }

    /**
     * Checks if it can load another index from disk
     * If the number of indexers in memory is higher
     *  than the maximum number then remove half
     *  of the indexers in memory with smaller ranks
     */
    private void checkIfCanLoadIndexerFromDisk() {
        if (indexersInMemory.size() < maxIndexersInMemory) {
            return; // there's still space
        }

        Set<String> toSave = indexersRanks
            .entrySet()
            .stream()
            .filter(entry -> indexersInMemory.containsKey(entry.getKey()))
            .sorted((entry1, entry2) ->
                -entry1.getValue().compareTo(entry2.getValue()) // decreasing order
            )
            .limit(Math.floorDiv(maxIndexersInMemory, 2))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        indexersInMemory.keySet().removeIf(key ->
            !toSave.contains(key)
        );
    }

    /**
     * Checks if it can load another document registry from disk
     * If the number of document registry in memory is higher
     *  than the maximum number then remove half
     *  of the document registry in memory with smaller ranks
     */
    private void checkIfCanLoadDocRegFromDisk() {
        if (docRegsInMemory.size() < maxDocRegsInMemory) {
            return; // there's still space
        }

        Set<Integer> toSave = docRegsRanks
            .entrySet()
            .stream()
            .parallel()
            .filter(entry -> docRegsInMemory.containsKey(entry.getKey()))
            .sorted((entry1, entry2) ->
                -entry1.getValue().compareTo(entry2.getValue()) // decreasing order
            )
            .limit(Math.floorDiv(maxDocRegsInMemory, 2))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        docRegsInMemory.keySet().removeIf(key ->
            !toSave.contains(key)
        );
    }

    /**
     * Checks if all the indexes to go over the posting lists of
     *  the query terms are at the end.
     * Removes the ones that reached the end
     * @return true if all reached the end, false otherwise
     */
    private boolean indexesAtEnd(List<Integer> idxs, List<String> terms, List<List<D>> postingLists) {
        assert idxs.size() == postingLists.size() && postingLists.size() == terms.size();

        boolean atEnd = true;

        for (int i = postingLists.size() - 1; i >= 0; i--) {
            if (idxs.get(i) < postingLists.get(i).size()) {
                atEnd = false;
            }
            else {
                idxs.remove(i);
                terms.remove(i);
                postingLists.remove(i);
            }
        }

        return atEnd;
    }

    /**
     * Auxiliary class to sort document by their weight
     */
    private static class DocumentRank {

        private int docId;

        private Float weight;

        public DocumentRank(int docId, float weight) {
            this.docId = docId;
            this.weight = weight;
        }

    }

    /**
     * Auxiliary class to sort the document ids and
     *  also keep track of the position of the document
     *  in the relevantDocuments list
     */
    private static class DocumentToTranslate {

        private int docId;

        private int idx;

        public DocumentToTranslate(int docId, int idx) {
            this.docId = docId;
            this.idx = idx;
        }
    }

}
