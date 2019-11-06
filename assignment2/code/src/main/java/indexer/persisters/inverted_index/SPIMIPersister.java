package indexer.persisters.inverted_index;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Stores the inverted index to a temporary file to apply the SPIMI algorithm
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public class SPIMIPersister<T extends Block & BaseTerm, D extends Block &BaseDocument> implements InvertedIndexBasePersister<T, D> {

    /**
     * Persists the terms objects followed by the postings lists in binary. To
     *  know the end of the file when loading a null object is written
     *
     * @param output to where the index will be written
     * @param invertedIndex of the indexer
     * @throws IOException if some occours while writing to the output stream
     */
    @Override
    public void persist(OutputStream output, Map<T, List<D>> invertedIndex) throws IOException {
        ObjectOutputStream objOutput = new ObjectOutputStream(output);

        Iterator<T> it = invertedIndex.keySet().stream().sorted((t1, t2) -> t1.compareTo(t2)).iterator();

        while (it.hasNext()) {
            T term = it.next();

            objOutput.writeObject(term);
            objOutput.reset();
            objOutput.writeObject(invertedIndex.get(term));
            objOutput.reset();
        }

        objOutput.writeObject(null);
    }

    /**
     * Gives an iterator that returns entries from the temporary files stored
     *
     * @param input to read from
     * @param indexingTmpFileCount which temporary file will be read. Used for error messages
     * @return and iterator to retrieve inverted index's entries
     * @throws IOException if some error occurs while reading from the stream
     */
    public Iterator<Entry<T, D>> load(InputStream input, int indexingTmpFileCount) throws IOException {
        ObjectInputStream objInput = new ObjectInputStream(
            input
        );

        return new Iterator<Entry<T, D>>() {

            /**
             * The entry to return on the next next() call
             */
            private Entry<T, D> currentEntry;

            /**
             * True if the input stream was closed, false otherwise
             */
            private boolean closed = false;

            /**
             * Verifies if theres more entries on the temporary file
             *
             * @return true if there is, false otherwise (if read a null while reading the term object)
             */
            @Override
            public boolean hasNext() {
                if (currentEntry != null) {
                    return true;
                }
                else if (closed) {
                    return false;
                }

                T term = null;
                List<D> documents = null;
                try {
                    term = (T) objInput.readObject();
                    if (term == null) {
                        closed = true;
                        objInput.close();
                        return false;
                    }

                    documents = (List<D>) objInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("ERROR while reading from temporary index file" +
                        "indexingTmpFile" + indexingTmpFileCount);
                    e.printStackTrace();
                    System.exit(2);
                }

                currentEntry = new Entry<>(term, documents);

                return true;
            }

            /**
             * Gets the next entry on the iterator. This iterator assumes a
             *  hasNext() call with return value true and only then a call of next()
             *
             * @return
             * @throws NoSuchElementException if the current entry to return is null
             */
            @Override
            public Entry<T, D> next() {
                if (currentEntry == null) {
                    throw new NoSuchElementException();
                }

                Entry<T, D> toReturn = currentEntry;

                currentEntry = null;

                return toReturn;
            }
        };
    }

    /**
     * Structure to group term and its posting list to be used
     *  on the SPIMIPipeline
     *
     * @param <T> type of the term
     * @param <D> type of the documents
     */
    public static class Entry<T extends Block & BaseTerm, D extends Block &BaseDocument> {

        /**
         * Internal term
         */
        private T term;

        /**
         * Posting list
         */
        private List<D> documents;

        /**
         * Main constructor
         *
         * @param term the entry term
         * @param documents posting list of the term
         */
        public Entry(T term, List<D> documents) {
            this.term = term;
            this.documents = documents;
        }

        /**
         * Getter of the term field
         *
         * @return the entry's term
         */
        public T getTerm() {
            return term;
        }

        /**
         * Getter of the documetns fields
         *
         * @return the posting list of the term
         */
        public List<D> getDocuments() {
            return documents;
        }

    }

}
