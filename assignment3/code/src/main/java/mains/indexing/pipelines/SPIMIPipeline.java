package mains.indexing.pipelines;

import data_containers.indexer.BaseIndexer;
import data_containers.indexer.post_indexing_actions.PostIndexingActions;
import io.loaders.LazyLoader;
import io.loaders.ObjectStreamLoader;
import io.persisters.BasePersister;
import io.persisters.ObjectStreamPersister;
import data_containers.indexer.structures.BaseDocument;
import data_containers.indexer.structures.BaseTerm;
import data_containers.indexer.structures.Block;
import parsers.corpus.CorpusReader;
import parsers.documents.Document;
import parsers.files.FileParser;
import tokenizer.BaseTokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Pipeline implementation that applies the SPIMI algorithm
 *  to index several documents of several files. This approach
 *  takes into consideration the memory usage. Deals with memory
 *  full situations by writing the current indexer's content to
 *  a temporary binary file to later merge into a final index file(s)
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public class SPIMIPipeline<T extends Block & BaseTerm, D extends Block & BaseDocument> extends Pipeline<T, D> {

    /**
     * Prefix for the temporary indexing files
     */
    private static final String indexingTmpFilePrefix = "indexingTmpFile";

    /**
     * Maximum memory load factor
     */
    private float maxLoadFactor;

    /**
     * Persister for the final index
     */
    private ObjectStreamPersister<T, List<D>> indexingTmpFilesPersister;

    /**
     * Persister for the temporary indexing files
     */
    private ObjectStreamLoader<T, List<D>> indexingTmpFilesLoader;

    /**
     * Variable to know if on the indexing step the
     *  program wrote any temporary file to disk
     */
    private boolean wroteToDisk;

    /**
     * Main constructor
     *
     * @param tokenizer Parsers a document's content an splits it into tokens
     * @param indexer Associates a set o documents to a given term
     * @param corpusReader Class to retrieve the files present on the corpus folder
     * @param finalIndexPersister in charge of writing to disk the inverted index
     * @param docRegistryPersister in charge of writing to disk the
     *  document registry structure
     * @param maxLoadFactor maximum memory load factor
     *  storing the final index
     */
    public SPIMIPipeline(BaseTokenizer tokenizer,
                         BaseIndexer<T, D> indexer,
                         CorpusReader corpusReader,
                         BasePersister<Integer, String> docRegistryPersister,
                         BasePersister<T, List<D>> finalIndexPersister,
                         float maxLoadFactor) {
        super(tokenizer, indexer, corpusReader, finalIndexPersister, docRegistryPersister);
        this.maxLoadFactor = maxLoadFactor;

        this.indexingTmpFilesPersister = new ObjectStreamPersister<>(indexingTmpFilePrefix, -1);
        this.indexingTmpFilesLoader = new ObjectStreamLoader<>();
    }

    /**
     * Indexes in memory the documents present on a file until the
     *  memory load surpasses the limit defined
     * @param fileParser the file to parse
     */
    @Override
    public void processFile(FileParser fileParser) {
        for (Document document : fileParser) {
            List<String> terms = tokenizer.tokenizeDocument(document.getToTokenize());

            int docId = documentRegistry.registerDocument(document.getIdentifier());

            if (!terms.isEmpty()) {
                indexer.indexTerms(docId, terms);
            }

            if (maxLoadFactorExceeded()) {
                try {
                    indexingTmpFilesPersister.persist(indexer.getInvertedIndex(), true);
                } catch (IOException e) {
                    System.err.println("ERROR while writing temporary indexing file");
                    e.printStackTrace();
                    System.exit(2);
                }

                try {
                    documentRegistryPersister.persist(documentRegistry.getRegistry(), false);
                } catch (IOException e) {
                    System.err.println("ERROR while writing to document registry file");
                    e.printStackTrace();
                    System.exit(2);
                }

                indexer.clear();
                System.gc();

                wroteToDisk = true;
            }
        }
    }

    /**
     * Iterator over the temporary files created and merges them. The number
     *  of output files depends on the number of terms and the max of terms
     *  per final index file defined as program argument
     */
    @Override
    public void persistIndex() {
        try {
            documentRegistryPersister.persist(documentRegistry.getRegistry(), true);
        } catch (IOException e) {
            System.err.println("ERROR while persisting final part of the document registry to file");
            e.printStackTrace();
            System.exit(2);
        }

        if (wroteToDisk) {
            try {
                indexingTmpFilesPersister.persist(indexer.getInvertedIndex(), true);
            } catch (IOException e) {
                System.err.println("ERROR while persisting last temporary indexing file");
                e.printStackTrace();
                System.exit(2);
            }

            indexer.clear();
            System.gc();
        }
        else {
            PostIndexingActions<T, D> postIndexingActions = indexer.getPostIndexingActions();
            if (postIndexingActions != null) {
                for (Map.Entry<T, List<D>> entry : indexer.getInvertedIndex().entrySet()) {
                    postIndexingActions.apply(entry.getKey(), entry.getValue());
                }
            }

            try {
                finalIndexPersister.persist(indexer.getInvertedIndex(), true);

                finalIndexPersister.close();
            } catch (IOException e) {
                System.err.println("ERROR while persisting complete final index to disk");
                e.printStackTrace();
                System.exit(2);
            }

            return;
        }

        // iterators used to retrieve the entries from each temporary file
        List<Iterator<Map.Entry<T, List<D>>>> tmpFilesReaders = new ArrayList<>();

        List<String> firstKeys = indexingTmpFilesPersister.getFirstKeys();
        for (int i = 0; i < firstKeys.size(); i++) {
            String filename = String.format("%s_%s_%s", indexingTmpFilePrefix, i, firstKeys.get(i));
            tmpFilesReaders.add(
                indexingTmpFilesLoader.load(filename)
            );
        }

        // holds the top entry for each temporary file being merged
        List<Map.Entry<T, List<D>>> tmpFilesTopEntries = new ArrayList<>(tmpFilesReaders .size());
        // to do a match between top entry and each temporary file their
        //  size must be the same. for that null are inserted
        for (int i = 0; i < tmpFilesReaders.size(); i++) {
            tmpFilesTopEntries.add(null);
        }

        // stores the entries for the commons terms lexicographically lower across
        //  all top entries of the different temporary files
        List<Map.Entry<T, List<D>>> lowerCommonTerms = new ArrayList<>();

        // used to know from which files the lowerCommonTerms were retrieved
        List<Integer> retrievedTmpFilesTopEntriesIdx = new ArrayList<>();

        // stores the terms and their posting lists to later write and
        //  the memory load factor retches the maximum
        List<Map.Entry<T, List<D>>> entriesToWrite = new ArrayList<>();

        // while there is entries on the temporary files to retrieve
        while (hasTmpFilesToRead(tmpFilesReaders, tmpFilesTopEntries)) {

            // calculates which entries has the terms lexicographically lower across
            //  all top entries of the different temporary files
            for (int i = 0; i < tmpFilesTopEntries.size(); i++) {
                Map.Entry<T, List<D>> entry = tmpFilesTopEntries.get(i);

                // if lowerCommonTerms is empty then is the first to be retrieved
                if (lowerCommonTerms.isEmpty()) {
                    lowerCommonTerms.add(entry);
                    retrievedTmpFilesTopEntriesIdx.add(i);
                } else {
                    // compare the stored terms (all are common, so the first one is used) against the current one
                    int compareResult = entry.getKey().getTerm().compareTo(lowerCommonTerms.get(0).getKey().getTerm());

                    // if its lexicographically lower
                    if (compareResult < 0) {
                        lowerCommonTerms.clear();
                        retrievedTmpFilesTopEntriesIdx.clear();
                    }

                    // if its lexicographically lower or equal
                    if (compareResult <= 0) {
                        lowerCommonTerms.add(entry);
                        retrievedTmpFilesTopEntriesIdx.add(i);
                    }
                }
            }

            // set to null the tmpFilesTopEntries positions related to the
            //  files which the lowerCommonTerms were retrieved
            retrievedTmpFilesTopEntriesIdx.forEach(usedFileIdx -> tmpFilesTopEntries.set(usedFileIdx, null));
            retrievedTmpFilesTopEntriesIdx.clear();

            // variables to store the current term being parsed and
            //  its posting list
            {T term = lowerCommonTerms.get(0).getKey();
            List<D> docs = mergePostingLists(lowerCommonTerms);

            entriesToWrite.add(new LazyLoader.Entry<>(term, docs));}

            if (maxLoadFactorExceeded()) {
                PostIndexingActions<T, D> postIndexingActions = indexer.getPostIndexingActions();
                if (postIndexingActions != null) {
                    for (Map.Entry<T, List<D>> entry : entriesToWrite) {
                        postIndexingActions.apply(entry.getKey(), entry.getValue());
                    }
                }

                try {
                    finalIndexPersister.persist(entriesToWrite, false);
                } catch (IOException e) {
                    System.err.println("ERROR while persisting final index file");
                    e.printStackTrace();
                    System.exit(2);
                }

                entriesToWrite = new ArrayList<>();

                System.gc();
            }
        }

        PostIndexingActions<T, D> postIndexingActions = indexer.getPostIndexingActions();
        if (postIndexingActions != null) {
            for (Map.Entry<T, List<D>> entry : entriesToWrite) {
                postIndexingActions.apply(entry.getKey(), entry.getValue());
            }
        }

        try {
            finalIndexPersister.persist(entriesToWrite, true);

            finalIndexPersister.close();
        } catch (IOException e) {
            System.err.println("ERROR while writing the last part of the final index to file");
            e.printStackTrace();
            System.exit(2);
        }

        System.gc();

        // remove temporary indexing files
        for (int i = 0; i < firstKeys.size(); i++) {
            String filename = String.format("%s_%d_%s", indexingTmpFilePrefix, i, firstKeys.get(i));

            try {
                Files.delete(Paths.get(filename));
            } catch (IOException e) {
                System.err.println("WARNING unable to remove indexing temporary file " + filename);
            }
        }
    }

    /**
     * Checks if the memory reached the load factor defined
     *
     * @return true if it reached the max load factor, false otherwise
     */
    private boolean maxLoadFactorExceeded() {
        Runtime runtime = Runtime.getRuntime();

        double used = runtime.totalMemory() - runtime.freeMemory();
        double max = runtime.maxMemory();

        return used / max >= maxLoadFactor;
    }

    /**
     * Checks if there is any file to read entries to merge to the
     *  final or intermediate index
     *
     * @param tmpFiles iterators used to retrieve the entries from each temporary file
     * @param tmpFilesTopEntry holds the top entry for each temporary file being merged
     * @return true if exists at least one file to read from more entries
     */
    private boolean hasTmpFilesToRead(List<Iterator<Map.Entry<T, List<D>>>> tmpFiles,
                                      List<Map.Entry<T, List<D>>> tmpFilesTopEntry) {
        // for each temporary file
        for (int i = tmpFiles.size() - 1; i >= 0; i--) {
            Iterator<Map.Entry<T, List<D>>> it = tmpFiles.get(i);

            // if the top entry stored is null it means that it was used
            //  or its the first time read
            if (tmpFilesTopEntry.get(i) == null) {
                // if the file doesn't has more entries
                if (!it.hasNext()) {
                    // remove it from the tmp files list
                    tmpFiles.remove(i);
                    // and remove its stop on the top entries list
                    tmpFilesTopEntry.remove(i);
                }
                else { // else read one more entry
                    tmpFilesTopEntry.set(i, it.next());
                }
            }
        }

        return !tmpFiles.isEmpty();
    }

    /**
     * Merges the posting lists of common terms retrieved from different
     *  temporary files
     *
     * @param lowerCommonTerms stores the entries for the commons terms lexicographically lower across
     *  all top entries of the different temporary files
     * @return a posting list with all the posting lists merged
     */
    private List<D> mergePostingLists(List<Map.Entry<T, List<D>>> lowerCommonTerms) {
        List<D> mergedPostingList = new ArrayList<>();

        // merge posting lists
        while (!lowerCommonTerms.isEmpty()) {
            // if there's only a posting list add it entirely
            if (lowerCommonTerms.size() == 1) {
                mergedPostingList.addAll(lowerCommonTerms.get(0).getValue());
                lowerCommonTerms.clear();
                break;
            }

            // variables to know with posting list has the lowest document id
            // points to the last non empty posting list
            int idx = lowerCommonTerms.size() - 1;
            int lowestDocId = lowerCommonTerms.get(idx).getValue().get(0).getDocId();

            // removes the entries where the posting list are empty
            while (lowerCommonTerms.get(idx).getValue().isEmpty()) {
                lowerCommonTerms.remove(idx);
                idx--; // and move the index to the next posting list (starting from the end)
            }

            // for subsequent posting lists after the one checked above
            for (int i = idx - 1; i >= 0; i--) {
                // if it is empty
                List<D> postingListToCheck = lowerCommonTerms.get(i).getValue();
                if (postingListToCheck.isEmpty()) {
                    lowerCommonTerms.remove(i);
                    idx--; // a remove shifts the elements on the list
                    //  and since we are iterating from the end
                    //  we need to also shift the index of the posting list with current document
                    //  with the lowest document id
                    continue;
                }

                int firstDocIdOnPostToCheck = postingListToCheck.get(0).getDocId();
                if (firstDocIdOnPostToCheck < lowestDocId) {
                    lowestDocId = firstDocIdOnPostToCheck;
                    idx = i;
                }
            }

            List<D> postListWithLowestDocId = lowerCommonTerms.get(idx).getValue();

            // check if more documents can be inserted to the final posting list from the postListWithLowestDocId
            //  if they are lower than the first on the other posting lists
            //  that had documents with higher document ids
            int furtherIdxWhereDocIdStillLowest = 0;
aa:         for (int i = 1; i < postListWithLowestDocId.size(); furtherIdxWhereDocIdStillLowest = i++) {
                // for each posting list to merge
                for (Map.Entry<T, List<D>> entry : lowerCommonTerms) {

                    if (entry.getValue() != postListWithLowestDocId) {
                        if (entry.getValue().get(0).getDocId() < lowestDocId) {
                            break aa;
                        }
                    }
                }
            }

            if (furtherIdxWhereDocIdStillLowest  == 0) {
                mergedPostingList.add(postListWithLowestDocId.remove(0));
            }
            else {
                mergedPostingList.addAll(postListWithLowestDocId.subList(0, furtherIdxWhereDocIdStillLowest + 1));
                postListWithLowestDocId.subList(0, furtherIdxWhereDocIdStillLowest + 1).clear();
            }

            if (postListWithLowestDocId.isEmpty()) {
                lowerCommonTerms.remove(idx);
            }
        }

        return mergedPostingList;
    }
}
