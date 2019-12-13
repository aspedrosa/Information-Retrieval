package mains.indexing.pipelines;

import data_containers.indexer.BaseIndexer;
import data_containers.indexer.post_indexing_actions.PostIndexingActions;
import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;
import io.metadata.MetadataManager;
import io.data_containers.loaders.lazy_load.LazyLoader;
import io.data_containers.loaders.lazy_load.ObjectStreamLoader;
import io.data_containers.persisters.BasePersister;
import io.data_containers.persisters.ObjectStreamPersister;
import parsers.corpus.CorpusReader;
import parsers.files.FileParser;
import tokenizer.BaseTokenizer;

import java.io.IOException;
import java.io.Serializable;
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
public class SPIMIPipeline<
    T extends Comparable<T> & Serializable,
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>
    > extends Pipeline<T, W, D, I> {

    /**
     * Maximum memory load factor
     */
    private float maxLoadFactor;

    /**
     * Persister for the final index
     */
    private ObjectStreamPersister<T, I> indexingTmpFilesPersister;

    /**
     * Persister for the temporary indexing files
     */
    private ObjectStreamLoader<T, I> indexingTmpFilesLoader;

    /**
     * Variable to know if on the indexing step the
     *  program wrote any temporary file to disk
     */
    private boolean wroteToDisk;

    private String tmpFolder;

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
                         BaseIndexer<T, W, D, I> indexer,
                         CorpusReader corpusReader,
                         String tmpFolder,
                         BasePersister<Integer, Integer> docRegistryPersister,
                         BasePersister<T, I> finalIndexPersister,
                         MetadataManager metadataManager,
                         float maxLoadFactor) {
        super(tokenizer, indexer, corpusReader, finalIndexPersister, docRegistryPersister, metadataManager);
        this.maxLoadFactor = maxLoadFactor;
        this.tmpFolder = tmpFolder;

        this.indexingTmpFilesPersister = new ObjectStreamPersister<>(tmpFolder, -1);
        this.indexingTmpFilesLoader = new ObjectStreamLoader<>();
    }

    /**
     * Indexes in memory the documents present on a file until the
     *  memory load surpasses the limit defined
     * @param fileParser the file to parse
     */
    @Override
    public void processFile(FileParser fileParser) {
        for (parsers.documents.Document document : fileParser) {
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
                documentRegistry.clear();
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
            // since is the last time to write to the document registry it will call close internally
            documentRegistryPersister.persist(documentRegistry.getRegistry(), true);
        } catch (IOException e) {
            System.err.println("ERROR while persisting final part of the document registry to file");
            e.printStackTrace();
            System.exit(2);
        }

        if (wroteToDisk) {
            try {
                // since is the last time to write to the indexer it will call close internally
                indexingTmpFilesPersister.persist(indexer.getInvertedIndex(), true);
            } catch (IOException e) {
                System.err.println("ERROR while persisting last temporary indexing file");
                e.printStackTrace();
                System.exit(2);
            }

            indexer.clear();
            documentRegistry.clear();
            System.gc();
        }
        else {
            PostIndexingActions<W, D, I> postIndexingActions = indexer.getPostIndexingActions();
            if (postIndexingActions != null) {
                for (Map.Entry<T, I> entry : indexer.getInvertedIndex().entrySet()) {
                    postIndexingActions.apply(entry.getValue());
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
        List<Iterator<Map.Entry<T, I>>> tmpFilesReaders = new ArrayList<>();

        for (int i = 0; i < indexingTmpFilesPersister.getAmountOfFilesCreated(); i++) {
            String filename = String.format("%s%s", tmpFolder, i);
            tmpFilesReaders.add(
                indexingTmpFilesLoader.load(filename)
            );
        }

        // holds the top entry for each temporary file being merged
        List<Map.Entry<T, I>> tmpFilesTopEntries = new ArrayList<>(tmpFilesReaders .size());
        // to do a match between top entry and each temporary file their
        //  size must be the same. for that null are inserted
        for (int i = 0; i < tmpFilesReaders.size(); i++) {
            tmpFilesTopEntries.add(null);
        }

        // stores the entries for the commons terms lexicographically lower across
        //  all top entries of the different temporary files
        List<Map.Entry<T, I>> lowerCommonTerms = new ArrayList<>();

        // used to know from which files the lowerCommonTerms were retrieved
        List<Integer> retrievedTmpFilesTopEntriesIdx = new ArrayList<>();

        // stores the terms and their posting lists to later write and
        //  the memory load factor retches the maximum
        List<Map.Entry<T, I>> entriesToWrite = new ArrayList<>();

        // while there is entries on the temporary files to retrieve
        while (hasTmpFilesToRead(tmpFilesReaders, tmpFilesTopEntries)) {

            // calculates which entries has the terms lexicographically lower across
            //  all top entries of the different temporary files
            for (int i = 0; i < tmpFilesTopEntries.size(); i++) {
                Map.Entry<T, I> entry = tmpFilesTopEntries.get(i);

                // if lowerCommonTerms is empty then is the first to be retrieved
                if (lowerCommonTerms.isEmpty()) {
                    lowerCommonTerms.add(entry);
                    retrievedTmpFilesTopEntriesIdx.add(i);
                } else {
                    // compare the stored terms (all are common, so the first one is used) against the current one
                    int compareResult = entry.getKey().compareTo(lowerCommonTerms.get(0).getKey());

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
            {I commonTermInfo = lowerCommonTerms.get(0).getValue();
            T term = lowerCommonTerms.get(0).getKey();
            List<D> docs = mergePostingLists(lowerCommonTerms);

            commonTermInfo.setPostingList(docs);
            // TODO WARNING here it is assumed that the data present on
            //  the term info structure beyond the posting list
            //  is the same for different termInfos

            entriesToWrite.add(new LazyLoader.Entry<>(term, commonTermInfo));}

            if (maxLoadFactorExceeded()) {
                PostIndexingActions<W, D, I> postIndexingActions = indexer.getPostIndexingActions();
                if (postIndexingActions != null) {
                    for (Map.Entry<T, I> entry : entriesToWrite) {
                        postIndexingActions.apply(entry.getValue());
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

        PostIndexingActions<W, D, I> postIndexingActions = indexer.getPostIndexingActions();
        if (postIndexingActions != null) {
            for (Map.Entry<T, I> entry : entriesToWrite) {
                postIndexingActions.apply(entry.getValue());
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
    private boolean hasTmpFilesToRead(List<Iterator<Map.Entry<T, I>>> tmpFiles,
                                      List<Map.Entry<T, I>> tmpFilesTopEntry) {
        // for each temporary file
        for (int i = tmpFiles.size() - 1; i >= 0; i--) {
            Iterator<Map.Entry<T, I>> it = tmpFiles.get(i);

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
    private List<D> mergePostingLists(List<Map.Entry<T, I>> lowerCommonTerms) {
        List<D> mergedPostingList = new ArrayList<>();

        // merge posting lists
        while (!lowerCommonTerms.isEmpty()) {
            // if there's only a posting list add it entirely
            if (lowerCommonTerms.size() == 1) {
                mergedPostingList.addAll(lowerCommonTerms.get(0).getValue().getPostingList());
                lowerCommonTerms.clear();
                break;
            }

            // variables to know with posting list has the lowest document id
            // points to the last non empty posting list
            int idx = lowerCommonTerms.size() - 1;
            int lowestDocId = lowerCommonTerms.get(idx).getValue().getPostingList().get(0).getDocId();

            // removes the entries where the posting list are empty
            while (lowerCommonTerms.get(idx).getValue().getPostingList().isEmpty()) {
                lowerCommonTerms.remove(idx);
                idx--; // and move the index to the next posting list (starting from the end)
            }

            // for subsequent posting lists after the one checked above
            for (int i = idx - 1; i >= 0; i--) {
                // if it is empty
                List<D> postingListToCheck = lowerCommonTerms.get(i).getValue().getPostingList();
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

            List<D> postListWithLowestDocId = lowerCommonTerms.get(idx).getValue().getPostingList();

            // check if more documents can be inserted to the final posting list from the postListWithLowestDocId
            //  if they are lower than the first on the other posting lists
            //  that had documents with higher document ids
            int furtherIdxWhereDocIdStillLowest = 0;
for1:         for (int i = 1; i < postListWithLowestDocId.size(); furtherIdxWhereDocIdStillLowest = i++) {
                // for each posting list to merge
                for (Map.Entry<T, I> entry : lowerCommonTerms) {

                    if (entry.getValue() != postListWithLowestDocId && entry.getValue().getPostingList().get(0).getDocId() < lowestDocId) {
                        break for1;
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
