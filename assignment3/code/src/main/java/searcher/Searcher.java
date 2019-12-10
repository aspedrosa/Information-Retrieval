package searcher;

import data_containers.DocumentRegistry;
import data_containers.indexer.BaseIndexer;
import data_containers.indexer.IndexerProvider;
import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;
import data_containers.indexer.weights_calculation.searching.CalculationsBase;
import io.data_containers.loaders.bulk_load.BulkLoader;
import tokenizer.BaseTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Searcher<D extends Document<Float>,
    I extends TermInfoBase<Float, D>
    > {

    private BaseTokenizer tokenizer;

    private CalculationsBase<String, Float, D, I> calculations;

    private TreeMap<Integer, String> docRegMetadata;

    private TreeMap<String, String> indexerMetadata;

    private TreeMap<Integer, DocumentRegistry> docRegsInMemory;

    private TreeMap<String, BaseIndexer<String, Float, D, I>> indexersInMemory;

    private BulkLoader<Integer, String> docRegLoader;

    private BulkLoader<String, I> indexerLoader;

    private IndexerProvider<String, Float, D, I> indexerProvider;

    private int maxDocRegsInMemory;

    private int maxIndexersInMemory;

    private Map<Integer, Integer> docRegsRanks;

    private Map<String, Integer> indexersRanks;

    private int K;

    public Searcher(
        BaseTokenizer tokenizer,
        CalculationsBase<String, Float, D, I> calculations,
        TreeMap<Integer, String> docRegMetadata,
        TreeMap<String, String> indexerMetadata,
        BulkLoader<Integer, String> docRegLoader,
        BulkLoader<String, I> indexerLoader,
        IndexerProvider<String, Float, D, I> indexerProvider,
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
        this.indexerProvider = indexerProvider;
        this.maxDocRegsInMemory = maxDocRegsInMemory;
        this.maxIndexersInMemory = maxIndexersInMemory;
        this.K = K;

        this.docRegsInMemory = new TreeMap<>();
        this.indexersInMemory = new TreeMap<>();

        this.indexersRanks = new HashMap<>();
        this.docRegsRanks = new HashMap<>();
    }

    public List<String> queryIndex(String query) {
        Map<String, Float> termFrequencyWeights = calculations.calculateTermFrequencyWeights(
            tokenizer.tokenizeString(query)
        );

        List<String> terms = new ArrayList<>(termFrequencyWeights.size());
        List<List<D>> postingLists = new ArrayList<>(termFrequencyWeights.size());
        getPostingListsOfQueryTerms(termFrequencyWeights, terms, postingLists);

        List<DocumentRank> relevantDocuments = getRelevantDocuments(terms, postingLists, termFrequencyWeights);

        return translateDocumentIds(relevantDocuments);
    }

    private void getPostingListsOfQueryTerms(Map<String, Float> termFrequencyWeights, List<String> terms, List<List<D>> postingLists) {
        termFrequencyWeights.forEach((term, frequency) -> {
            Map.Entry<String, BaseIndexer<String, Float, D, I>> indexerEntry = indexersInMemory.floorEntry(term);
            I termInfo = indexerEntry == null ? null : indexerEntry.getValue().getTermInfo(term);

            if (indexerEntry != null && termInfo != null) {
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
                System.out.println("indexer in memory");
            }
            else {
                // not in memory

                Map.Entry<String, String> indexFilenameEntry = indexerMetadata.floorEntry(term);
                String indexFilename = indexFilenameEntry == null ? null : indexFilenameEntry.getValue();

                if (indexFilename != null && !indexersInMemory.containsKey(indexFilename)) {
                    // load from disk

                    checkIfCanLoadIndexerFromDisk();

                    BaseIndexer<String, Float, D, I> indexer = null;
                    try {
                        indexer = indexerProvider.createIndexer(indexerLoader.load(indexFilename));
                    } catch (IOException e) {
                        System.err.println("ERROR while loading indexer file " + indexFilename);
                        e.printStackTrace();
                        System.exit(2);
                    }

                    // store index in memory
                    indexersInMemory.put(indexFilenameEntry.getKey(), indexer);

                    if (!indexersRanks.containsKey(indexFilenameEntry.getKey())) {
                        indexersRanks.put(indexFilenameEntry.getKey(), 0);
                    }

                    termInfo = indexer.getTermInfo(term);
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
    }

    private List<DocumentRank> getRelevantDocuments(List<String> terms, List<List<D>> postingLists, Map<String, Float> termFrequencyWeights) {
        List<Integer> postingListsCurrentIndexes = new ArrayList<>(postingLists.size());
        for (int i = 0; i < postingLists.size(); i++) {
            postingListsCurrentIndexes.add(0);
        }

        List<DocumentRank> relevantDocuments = new ArrayList<>();

        // while the end of all posting lists wasn't reached
        while (!indexsAtEnd(postingListsCurrentIndexes, terms, postingLists)) {
            List<Integer> idxWithDocWithLowerID = new ArrayList<>(terms.size());

            // find the first document(s) among all the posting lists with
            //  the lowest docId
            idxWithDocWithLowerID.add(0);
            int lowestDocId = postingLists.get(0).get(0).getDocId();
            for (int i = 1; i < postingListsCurrentIndexes.size(); i++) {
                int docId = postingLists.get(i).get(0).getDocId();
                if (docId < lowestDocId) {
                    idxWithDocWithLowerID.clear();
                    idxWithDocWithLowerID.add(i);
                    lowestDocId = docId;
                }
                else if (docId == lowestDocId) {
                    idxWithDocWithLowerID.add(i);
                }
            }

            // if the number of query terms on the document is greater of equal to half
            //  of the number of query terms
            if (idxWithDocWithLowerID.size() >= termFrequencyWeights.size() * 0.5) {
                // calculate the score for this document
                float cumulativeScore = 0;
                int docId = postingLists.get(idxWithDocWithLowerID.get(0)).get(0).getDocId();

                for (Integer idx : idxWithDocWithLowerID) {
                    float documentWeightForTerm = postingLists.get(idx).get(0).getWeight();
                    float queryTermWeight = termFrequencyWeights.get(terms.get(idx));

                    cumulativeScore += queryTermWeight * documentWeightForTerm;
                }

                // and add it to the relevant documents list
                relevantDocuments.add(new DocumentRank(docId, cumulativeScore));
            }

            for (Integer idx : idxWithDocWithLowerID) {
                int currentIdx = postingListsCurrentIndexes.get(idx);
                postingListsCurrentIndexes.set(idx, currentIdx + 1);
            }
        }

        relevantDocuments.sort((doc1, doc2) ->
            -doc1.weight.compareTo(doc2.weight) // decreasing order
        );

        return relevantDocuments.subList(0, relevantDocuments.size() < K ? relevantDocuments.size() : K);
    }

    private List<String> translateDocumentIds(List<DocumentRank> relevantDocuments) {
        List<String> relevantDocumentsIdentifiers = new ArrayList<>(relevantDocuments.size());

        for (DocumentRank docRank : relevantDocuments) {
            int docId = docRank.docId;

            Map.Entry<Integer, DocumentRegistry> docRegEntry = docRegsInMemory.floorEntry(docId);
            String identifier = docRegEntry == null ? null : docRegEntry.getValue().translateDocId(docId);

            if (docRegEntry != null && identifier != null) {
                // in memory
                docRegsRanks.put(docRegEntry.getKey(), docRegsRanks.get(docRegEntry.getKey()) + 1);

                relevantDocumentsIdentifiers.add(identifier);

                System.out.println("doc reg in memory");
            }
            else {
                // check disk

                Map.Entry<Integer, String> docRegFilenameEntry = docRegMetadata.floorEntry(docId);
                String docRegFilename = docRegFilenameEntry.getValue();

                checkIfCanLoadDocRegFromDisk();

                DocumentRegistry documentRegistry = null;
                try {
                    documentRegistry = new DocumentRegistry(docRegLoader.load(docRegFilename));
                } catch (IOException e) {
                    System.err.println("ERROR while loading document registry file " + docRegFilename);
                    e.printStackTrace();
                    System.exit(2);
                }

                docRegsInMemory.put(docRegFilenameEntry.getKey(), documentRegistry);

                Integer rank = docRegsRanks.get(docRegFilenameEntry.getKey());
                if (rank == null) {
                    docRegsRanks.put(docRegFilenameEntry.getKey(), 1);
                }
                else {
                    docRegsRanks.put(docRegFilenameEntry.getKey(), rank + 1);
                }

                identifier = documentRegistry.translateDocId(docId);

                relevantDocumentsIdentifiers.add(identifier);
            }
        }

        return relevantDocumentsIdentifiers;
    }

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

        System.out.println("cleared mem of indexers");

        System.gc();
    }

    private void checkIfCanLoadDocRegFromDisk() {
        if (docRegsInMemory.size() < maxDocRegsInMemory) {
            return; // there's still space
        }

        Set<Integer> toSave = docRegsRanks
            .entrySet()
            .stream()
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

        System.out.println("cleared mem of doc regs");

        System.gc();
    }

    private boolean indexsAtEnd(List<Integer> idxs, List<String> terms, List<List<D>> postingLists) {
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

    private class DocumentRank {

        private int docId;

        private Float weight;

        public DocumentRank(int docId, float weight) {
            this.docId = docId;
            this.weight = weight;
        }

    }

}
