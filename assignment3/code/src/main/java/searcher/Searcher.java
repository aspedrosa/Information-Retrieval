package searcher;

import data_containers.DocumentRegistry;
import data_containers.indexer.BaseIndexer;
import data_containers.indexer.IndexerProvider;
import data_containers.indexer.WeightsIndexerBase;
import data_containers.indexer.structures.BaseDocument;
import data_containers.indexer.structures.BaseTerm;
import data_containers.indexer.structures.Block;
import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermWithInfo;
import data_containers.indexer.structures.aux_structs.DocumentWeight;
import data_containers.indexer.weights_calculation.searching.CalculationsBase;
import io.data_containers.loaders.bulk_load.BulkLoader;
import tokenizer.BaseTokenizer;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Searcher<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    private BaseTokenizer tokenizer;

    private CalculationsBase calculations;

    private TreeMap<Integer, String> docRegMetadata;

    private TreeMap<String, String> indexerMetadata;

    private TreeMap<Integer, DocumentRegistry> docRegsInMemory;

    private TreeMap<String, BaseIndexer<T, D>> indexersInMemory;

    private BulkLoader<Integer, String> docRegLoader;

    private BulkLoader<T, List<D>> indexerLoader;

    private IndexerProvider<T, D> indexerProvider;

    private int maxDocRegsInMemory;

    private int maxIndexersInMemory;

    private Map<Integer, Integer> docRegsRanks;

    private Map<String, Integer> indexersRanks;

    public Searcher(
        BaseTokenizer tokenizer,
        TreeMap<Integer, String> docRegMetadata,
        TreeMap<String, String> indexerMetadata,
        BulkLoader<Integer, String> docRegLoader,
        BulkLoader<T, List<D>> indexerLoader,
        IndexerProvider<T, D> indexerProvider,
        int maxDocRegsInMemory,
        int maxIndexersInMemory
        ) {
        this.tokenizer = tokenizer;
        this.docRegMetadata = docRegMetadata;
        this.indexerMetadata = indexerMetadata;
        this.docRegLoader = docRegLoader;
        this.indexerLoader = indexerLoader;
        this.indexerProvider = indexerProvider;
        this.maxDocRegsInMemory = maxDocRegsInMemory;
        this.maxIndexersInMemory = maxIndexersInMemory;

        this.docRegsInMemory = new TreeMap<>();
        this.indexersInMemory = new TreeMap<>();

        this.indexersRanks = new HashMap<>();
        this.docRegsRanks = new HashMap<>();
    }

    public List<String> queryIndex(String query) {
        Map<String, Float> termFrequencies = calculations.calculateTermFrequency(
            tokenizer.tokenizeString(query)
        );

        List<List<D>> postingLists = new LinkedList<>();

        Set<String> toRemove = new HashSet<>();

        for (String term : termFrequencies.keySet()) {
            Map.Entry<String, BaseIndexer<T, D>> indexerEntry = indexersInMemory.lowerEntry(term);
            List<D> postingList = indexerEntry == null ? null : indexerEntry.getValue().getPostingList(term);

            if (indexerEntry != null && postingList != null) {
                indexersRanks.put(indexerEntry.getKey(), indexersRanks.get(indexerEntry.getKey()) + 1);

                postingLists.add(postingList);
            }
            else {
                // not in memory

                Map.Entry<String, String> indexFilenameEntry = indexerMetadata.lowerEntry(term);
                String indexFilename = indexFilenameEntry == null ? null : indexFilenameEntry.getValue();

                if (indexFilename != null && !indexersInMemory.containsKey(indexFilename)) {
                    // load from disk

                    checkIfCanLoadIndexerFromDisk();

                    BaseIndexer<T, D> indexer = null;
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

                    postingList = indexer.getPostingList(term);
                    if (postingList != null) {
                        postingLists.add(postingList);

                        indexersRanks.put(indexFilenameEntry.getKey(), indexersRanks.get(indexFilenameEntry.getKey()) + 1);
                    }
                    else {
                        // term wasn't indexed
                        toRemove.add(term);
                    }
                }
                else {
                    toRemove.add(term);
                }
            }
        }

        // remove not indexed terms
        for (String termToRemove : toRemove) {
            termFrequencies.remove(termToRemove);
        }

        // TODO apply idf on query
        // TODO apply normalization on query

        // TODO Get docs that contain more than x terms

        // TODO calculate their weights

        // TODO sort docs by weights

        // TODO translate doc id

        return null;
    }

    private void checkIfCanLoadIndexerFromDisk() {
        // there's still space
        if (indexersInMemory.size() < maxIndexersInMemory) {
            return;
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

        System.gc();
    }

    private void checkIfCanLoadDocRegFromDisk() {
        Set<Integer> toSave = docRegsRanks.entrySet().stream().filter(entry ->
            docRegsInMemory.containsKey(entry.getKey())
        ).sorted(Comparator.comparingInt(Map.Entry::getValue))
            .limit(4)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        for (Integer key : docRegsInMemory.keySet()) {
            if (!toSave.contains(key)) {
                docRegsInMemory.remove(key);
            }
        }

        System.gc();
    }

}
