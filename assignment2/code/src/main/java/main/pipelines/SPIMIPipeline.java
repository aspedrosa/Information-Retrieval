package main.pipelines;

import indexer.BaseIndexer;
import indexer.persisters.Constants;
import indexer.persisters.inverted_index.ForEachEntryPersister;
import indexer.persisters.inverted_index.InvertedIndexBasePersister;
import indexer.persisters.inverted_index.SPIMIPersister;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;
import parsers.corpus.CorpusReader;
import parsers.documents.Document;
import parsers.files.FileParser;
import tokenizer.BaseTokenizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
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
     * Maximum memory load factor
     */
    private float maxLoadFactor;

    /**
     * Counter to know how many temporary files where created
     */
    private int tmpFilesCounter;

    /**
     * Persister to persist the temporary files
     *  when the memory reaches the maximum load factor
     */
    private SPIMIPersister<T, D> spimiPersister;

    /**
     * Stream to write the document registry structure
     */
    private ObjectOutputStream docRegistryOutput;

    /**
     * Variable to know if on the indexing step the
     *  program wrote any temporary file to disk
     */
    private boolean wroteToDisk;

    /**
     * Persisting strategy to store the final index
     */
    private ForEachEntryPersister<T, D> forEachEntryPersister;

    /**
     * Number of terms per each file
     */
    private int termCountPerIndexOutputFile;

    /**
     * Number of terms wrote to the opened stream to write the final index
     */
    private int currentTermCountPerIndexOutputFile;

    /**
     * Stream to write the final index
     */
    private BufferedOutputStream finalIndexOutput;

    /**
     * Holds the name of the opened stream to write the final index
     */
    private String finalIndexFilename;

    /**
     * Main constructor
     *
     * @param tokenizer Parsers a document's content an splits it into tokens
     * @param indexer Associates a set o documents to a given term
     * @param corpusReader Class to retrieve the files present on the corpus folder
     * @param indexOutputFileName Name of the file to write the final index file
     * @param docRegistryOutput output to write the document registry structure
     * @param maxLoadFactor maximum memory load factor
     * @param forEachEntryPersister persisting strategy to store the final index
     * @param termCountPerIndexOutputFile number of terms per each file
     *  storing the final index
     */
    public SPIMIPipeline(BaseTokenizer tokenizer,
                         BaseIndexer<T, D> indexer,
                         CorpusReader corpusReader,
                         String indexOutputFileName,
                         ObjectOutputStream docRegistryOutput,
                         float maxLoadFactor,
                         ForEachEntryPersister<T, D> forEachEntryPersister,
                         int termCountPerIndexOutputFile) {
        super(tokenizer, indexer, corpusReader, indexOutputFileName);
        this.forEachEntryPersister = forEachEntryPersister;
        this.docRegistryOutput = docRegistryOutput;
        this.maxLoadFactor = maxLoadFactor;
        this.termCountPerIndexOutputFile = termCountPerIndexOutputFile;

        this.tmpFilesCounter = 0;
        this.spimiPersister = new SPIMIPersister<>();
        this.currentTermCountPerIndexOutputFile = 0;
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

            int docId = document.getId();
            indexer.registerDocument(docId, document.getIdentifier());

            if (!terms.isEmpty()) {
                indexer.indexTerms(docId, terms);
            }

            if (maxLoadFactorExceeded()) {
                persistInvertedIndex(spimiPersister, "indexingTmpFile" + tmpFilesCounter++);
                persistDocumentRegistry();

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
        persistDocumentRegistry();

        try {
            docRegistryOutput.close();
        } catch (IOException e) {
            System.err.println("ERROR while closing document registry file");
            e.printStackTrace();
            System.exit(2);
        }

        if (wroteToDisk) {
            persistInvertedIndex(spimiPersister, "indexingTmpFile" + tmpFilesCounter++);

            indexer.clear();
            System.gc();
        }
        else {
            persistInvertedIndex(forEachEntryPersister, indexOutputFileName);

            return;
        }

        // iterators used to retrieve the entries from each temporary file
        List<Iterator<SPIMIPersister.Entry<T, D>>> tmpFilesReaders = new ArrayList<>();

        for (int i = 0; i < tmpFilesCounter; i++) {
            try {
                tmpFilesReaders.add(
                    spimiPersister.load(
                        new BufferedInputStream(
                            new FileInputStream("indexingTmpFile" + i)
                        ),
                        i
                    )
                );
            } catch (IOException e) {
                System.err.println("ERROR while opening temporary index file" +
                    "indexingTempFile" + i);
                e.printStackTrace();
                System.exit(2);
            }
        }

        // holds the top entry for each temporary file being merged
        List<SPIMIPersister.Entry<T, D>> tmpFilesTopEntries = new ArrayList<>(tmpFilesReaders .size());
        // to do a match between top entry and each temporary file their
        //  size must be the same. for that null are inserted
        for (int i = 0; i < tmpFilesReaders.size(); i++) {
            tmpFilesTopEntries.add(null);
        }

        // stores the entries for the commons terms lexicographically lower across
        //  all top entries of the different temporary files
        List<SPIMIPersister.Entry<T, D>> lowerCommonTerms = new ArrayList<>();

        // used to know from which files the lowerCommonTerms were retrieved
        List<Integer> retrievedTmpFilesTopEntriesIdx = new ArrayList<>();

        // stores the terms and their posting lists to later write and
        //  the memory load factor retches the maximum
        List<SPIMIPersister.Entry<T, D>> entriesToWrite = new ArrayList<>();

        // while there is entries on the temporary files to retrieve
        while (hasTmpFilesToRead(tmpFilesReaders, tmpFilesTopEntries)) {

            // calculates which entries has the terms lexicographically lower across
            //  all top entries of the different temporary files
            for (int i = 0; i < tmpFilesTopEntries.size(); i++) {
                SPIMIPersister.Entry<T, D> entry = tmpFilesTopEntries.get(i);

                // if lowerCommonTerms is empty then is the first to be retrieved
                if (lowerCommonTerms.isEmpty()) {
                    lowerCommonTerms.add(entry);
                    retrievedTmpFilesTopEntriesIdx.add(i);
                } else {
                    // compare the stored terms (all are common, so the first one is used) against the current one
                    int compareResult = entry.getTerm().compareTo(lowerCommonTerms.get(0).getTerm());

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
            {T term = lowerCommonTerms.get(0).getTerm();
            List<D> docs = mergePostingLists(lowerCommonTerms);

            entriesToWrite.add(new SPIMIPersister.Entry<>(term, docs));}

            if (maxLoadFactorExceeded()) {
                writeFinalIndexToDisk(entriesToWrite);

                entriesToWrite = new ArrayList<>();

                System.gc();
            }
        }

        writeFinalIndexToDisk(entriesToWrite);

        try {
            finalIndexOutput.close();
        } catch (IOException e) {
            System.err.println("ERROR while closing final index file " + finalIndexFilename);
            e.printStackTrace();
            System.exit(2);
        }

        // remove temporary indexing files
        for (int i = 0; i < tmpFilesCounter; i++) {
            String filename = "indexingTmpFile" + i;

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
     * @return true if it reaced the max load factor, false otherwise
     */
    private boolean maxLoadFactorExceeded() {
        Runtime runtime = Runtime.getRuntime();

        double used = runtime.totalMemory() - runtime.freeMemory();
        double max = runtime.maxMemory();

        return used / max >= maxLoadFactor;
    }

    /**
     * Auxiliary function to reduce code duplication that stores the inverted index
     *  with a given persister.
     *
     * @param persister that handles the output format
     * @param filename of the file to create file
     */
    private void persistInvertedIndex(InvertedIndexBasePersister<T, D> persister, String filename) {
        BufferedOutputStream output;
        try {
            output = new BufferedOutputStream(
                new FileOutputStream(filename)
            );

            persister.persist(output, indexer.getInvertedIndex(), indexer);

            output.close();
        } catch (IOException e) {
            System.err.println("ERROR while persisting " + filename + " to disk");
            e.printStackTrace();
            System.exit(2);
        }
    }

    /**
     * Used to persist the document registry of the indexer.
     * The identifier on the ith position is relative to the
     *  document with the document i, for that, <b>it is assumed that
     *  no skips on the document id happen</b>
     * This method main porpoise is to reduce code duplication.
     */
    private void persistDocumentRegistry() {
        indexer.getDocumentRegistry().entrySet().stream()
            .sorted(Comparator.comparingInt(Map.Entry::getKey))
            .forEach(entry -> {
                try {
                    docRegistryOutput.writeObject(entry.getValue());
                    docRegistryOutput.reset();
                } catch (IOException e) {
                    System.err.println("ERROR while writing to document registry file");
                    e.printStackTrace();
                    System.exit(2);
                }
            });
    }

    /**
     * Checks if there is any file to read entries to merge to the
     *  final or intermediate index
     *
     * @param tmpFiles iterators used to retrieve the entries from each temporary file
     * @param tmpFilesTopEntry holds the top entry for each temporary file being merged
     * @return true if exists at least one file to read from more entries
     */
    private boolean hasTmpFilesToRead(List<Iterator<SPIMIPersister.Entry<T, D>>> tmpFiles,
                                      List<SPIMIPersister.Entry<T, D>> tmpFilesTopEntry) {
        // for each temporary file
        for (int i = tmpFiles.size() - 1; i >= 0; i--) {
            Iterator<SPIMIPersister.Entry<T, D>> it = tmpFiles.get(i);

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
                else { // else read on more entry
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
    private List<D> mergePostingLists(List<SPIMIPersister.Entry<T, D>> lowerCommonTerms) {
        List<D> mergedPostingList = new ArrayList<>();

        // merge posting lists
        while (!lowerCommonTerms.isEmpty()) {
            // if there's only a posting list add it entirely
            if (lowerCommonTerms.size() == 1) {
                mergedPostingList.addAll(lowerCommonTerms.get(0).getDocuments());
                lowerCommonTerms.clear();
                break;
            }

            // variables to know with posting list has the lowest document id
            // points to the last non empty posting list
            int idx = lowerCommonTerms.size() - 1;
            int lowestDocId = lowerCommonTerms.get(idx).getDocuments().get(0).getDocId();

            // removes the entries where the posting list are empty
            while (lowerCommonTerms.get(idx).getDocuments().isEmpty()) {
                lowerCommonTerms.remove(idx);
                idx--; // and move the index to the next posting list (starting from the end)
            }

            // for subsequent posting lists after the one checked above
            for (int i = idx - 1; i >= 0; i--) {
                // if it is empty
                List<D> postingListToCheck = lowerCommonTerms.get(i).getDocuments();
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

            List<D> postListWithLowestDocId = lowerCommonTerms.get(idx).getDocuments();

            // check if more documents can be inserted to the final posting list from the postListWithLowestDocId
            //  if they are lower than the first on the other posting lists
            //  that had documents with higher document ids
            int furtherIdxWhereDocIdStillLowest = 0;
            aa:             for (int i = 1; i < postListWithLowestDocId.size(); furtherIdxWhereDocIdStillLowest = i++) {
                // for each posting list to merge
                for (SPIMIPersister.Entry<T, D> entry : lowerCommonTerms) {

                    if (entry.getDocuments() != postListWithLowestDocId) {
                        if (entry.getDocuments().get(0).getDocId() < lowestDocId) {
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

    /**
     * Writes a set of entries term-documents to disk using the for each persister received
     *
     * @param entriesToWrite the entries to persist
     */
    private void writeFinalIndexToDisk(List<SPIMIPersister.Entry<T, D>> entriesToWrite) {
        if (finalIndexOutput == null) {
            openFinalIndexOutput(
                entriesToWrite.get(0).getTerm().getTerm()
            );
        }

        try {
            // code similar to the method persist of the class ForEachEntryPersister
            //  only different is the collection that is being iterated
            for (SPIMIPersister.Entry<T, D> entry : entriesToWrite) {
                if (currentTermCountPerIndexOutputFile == termCountPerIndexOutputFile) {
                    currentTermCountPerIndexOutputFile = 0;
                    openFinalIndexOutput(
                        entry.getTerm().getTerm()
                    );
                }

                indexer.postIndexingActions(entry.getTerm(), entry.getDocuments());

                byte[] termBytes = forEachEntryPersister.handleTerm(entry.getTerm()).getBytes();
                finalIndexOutput.write(termBytes, 0 , termBytes.length);

                finalIndexOutput.write(Constants.COMMA, 0, Constants.COMMA.length);

                forEachEntryPersister.handleDocuments(finalIndexOutput, entry.getDocuments());

                if (++currentTermCountPerIndexOutputFile != termCountPerIndexOutputFile) {
                    finalIndexOutput.write(Constants.NEWLINE, 0, Constants.NEWLINE.length);
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR while persisting " + finalIndexFilename + " to disk");
            e.printStackTrace();
            System.exit(2);
        }
    }

    /**
     * Opens the file to where will be stored the final index
     *
     * @param firstTerm to append to the name of the file to know the first term of a given final index file
     */
    private void openFinalIndexOutput(String firstTerm) {
        if (finalIndexOutput != null) {
            try {
                finalIndexOutput.close();
            } catch (IOException e) {
                System.err.println("ERROR while closing final index file " + finalIndexFilename);
                e.printStackTrace();
                System.exit(2);
            }

        }

        finalIndexFilename = indexOutputFileName + "_" + firstTerm;

        try {
            finalIndexOutput = new BufferedOutputStream(
                new FileOutputStream(finalIndexFilename)
            );
        } catch (FileNotFoundException e) {
            System.err.println("ERROR while opening final index file " + finalIndexFilename);
            e.printStackTrace();
            System.exit(2);
        }
    }

}
