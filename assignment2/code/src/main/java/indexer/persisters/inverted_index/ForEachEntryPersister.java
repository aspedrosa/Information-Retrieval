package indexer.persisters.inverted_index;

import indexer.persisters.PostIndexingActions;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Type of inverted indexer persister that iterates over terms sorted
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public abstract class ForEachEntryPersister <T extends Block & BaseTerm,
                                             D extends Block & BaseDocument> extends InvertedIndexBasePersister<T, D> {

    /**
     * Bytes of the separator to write between the term and the posting list
     */
    private byte[] termDocumentSeparator;

    /**
     * Bytes of the terminator to write after each entry
     */
    private byte[] entryTerminator;

    /**
     * Main constructor
     *
     * @param termDocumentSeparator Bytes of the separator to write between the term and the posting list
     * @param entryTerminator Bytes of the terminator to write after each entry
     */
    public ForEachEntryPersister(byte[] termDocumentSeparator, byte[] entryTerminator) {
        this.termDocumentSeparator = termDocumentSeparator;
        this.entryTerminator = entryTerminator;
    }

    /**
     * Getter of the field documentSeparator
     *
     * @return the separator between terms and documents
     */
    public byte[] getTermDocumentSeparator() {
        return termDocumentSeparator;
    }

    /**
     * Getter of the field entryTerminator
     *
     * @return the terminator after entries
     */
    public byte[] getEntryTerminator() {
        return entryTerminator;
    }

    /**
     * Sorts the terms present on the inverted index and then
     *  iterate over them and calls a function handleTerm(), passing
     *  the term and its posting list as argument, that dictates the
     *  output format
     *
     * @param output to where the index will be written
     * @param invertedIndex the inverted index held by the indexer
     * @param postIndexingActions actions to apply to the inverted index before persisting
     * @throws IOException if some errors occur while writing the index to the stream
     */
    @Override
    public final void persist(OutputStream output, Map<T, List<D>> invertedIndex, PostIndexingActions<T, D> postIndexingActions) throws IOException {
        System.out.println("Sorting terms");

        List<T> sortedTerms = new ArrayList<>(invertedIndex.keySet());
        sortedTerms.sort(T::compareTo);

        System.out.println("Finished sorting terms");

        System.out.println("Writing index to disk");

        for (int i = 0; i < sortedTerms.size(); i++) {
            postIndexingActions.postIndexingActions(sortedTerms.get(i), invertedIndex.get(sortedTerms.get(i)));

            byte[] term = handleTerm(sortedTerms.get(i)).getBytes();
            output.write(term, 0, term.length);

            output.write(termDocumentSeparator, 0 , termDocumentSeparator.length);

            handleDocuments(output, invertedIndex.get(sortedTerms.get(i)));

            if (i != sortedTerms.size() - 1) {
                output.write(entryTerminator, 0, entryTerminator.length);
            }
        }
    }

    /**
     * Dictates the output format of the terms
     *
     * @param term term object
     * @return a formatted string representing the document
     */
    public abstract String handleTerm(T term);

    /**
     * Function that dictates the output format of the
     *   posting list and is in charge of writing it
     *   to the stream.
     * This functions doesn't followed the same approach as the
     *  handle term because posting lists can be large, so
     *  creating a string of those posting lists could lead to
     *  out of memory exceptions.
     *
     * @param output where to write the list of documents
     * @param documents documents to format
     * @throws IOException if some error occurs while writing the documents to the stream
     */
    public abstract void handleDocuments(OutputStream output, List<D> documents) throws IOException;

}
