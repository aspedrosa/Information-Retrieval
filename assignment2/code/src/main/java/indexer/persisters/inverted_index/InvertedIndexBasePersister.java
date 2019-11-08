package indexer.persisters.inverted_index;

import indexer.persisters.PostIndexingActions;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * All persisting strategies for the inverted index must
 *  implement this interface
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public abstract class InvertedIndexBasePersister<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    /**
     * Method called to by the a BaseIndexer to write the inverted index
     *  to the output stream
     *
     * @param output to where the index will be written
     * @param invertedIndex of the indexer
     * @param postIndexingActions actions to apply to the inverted index before persisting
     * @throws IOException if some error occurs while writing the index to the stream
     */
    public abstract void persist(OutputStream output, Map<T, List<D>> invertedIndex, PostIndexingActions<T, D> postIndexingActions) throws IOException;
    /**
     * Creates an iterator used to read, one by one,
     *  the entries present on a file written
     *  by the the same persister
     *
     * @param input from where to read the entries
     * @param filename the name of the file to read from. Used for message errors
     * @return iterator to read from a previously written file by this persister
     */
    public abstract Iterator<Entry<T, D>> load(InputStream input, String filename);

    /**
     * Structure to move terms and their posting lists
     *  through out the program
     *
     * @param <T> type of the terms
     * @param <D> type of the documents
     */
    public static class Entry<T, D> implements Serializable {

        /**
         * A term
         */
        private T term;

        /**
         * Posting list
         */
        private List<D> documents;

        /**
         * Main constructor
         *
         * @param term of the entry
         * @param documents of the entry
         */
        public Entry(T term, List<D> documents) {
            this.term = term;
            this.documents = documents;
        }

        /**
         * Getter for the term field
         *
         * @return term of the entry
         */
        public T getTerm() {
            return term;
        }

        /**
         * Getter for posting list of the entry
         *
         * @return posting list of the entry
         */
        public List<D> getDocuments() {
            return documents;
        }

    }

}
