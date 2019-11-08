package indexer.persisters.inverted_index;

import com.sun.istack.internal.Nullable;
import indexer.persisters.PostIndexingActions;
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
public class SPIMIPersister<T extends Block & BaseTerm, D extends Block &BaseDocument> extends InvertedIndexBasePersister<T, D> {

    /**
     * Persists the terms objects followed by the postings lists in binary. To
     *  know the end of the file when loading a null object is written
     *
     * @param output to where the index will be written
     * @param invertedIndex of the indexer
     * @param postIndexingActions since the files written by this persister are temporary, this field is ignored,
     *  thus can be null
     * @throws IOException if some error occurs while writing to the output stream
     */
    @Override
    public void persist(OutputStream output, Map<T, List<D>> invertedIndex, @Nullable PostIndexingActions<T, D> postIndexingActions) throws IOException {
        ObjectOutputStream objOutput = new ObjectOutputStream(output);

        Iterator<T> it = invertedIndex.keySet().stream().sorted((t1, t2) -> t1.compareTo(t2)).iterator();

        while (it.hasNext()) {
            T term = it.next();

            objOutput.writeObject(new Entry<>(term, invertedIndex.get(term)));
            objOutput.reset();
        }

        objOutput.writeObject(null);
    }

    /**
     * Gives an iterator that returns entries from the temporary files stored
     *
     * @param input to read from
     * @param filename of the file from which will be read. Used for error messages
     * @return and iterator to retrieve inverted index's entries
     * @throws IOException if some error occurs while reading from the stream
     */
    public Iterator<Entry<T, D>> load(InputStream input, String filename) {
        ObjectInputStream tmp = null;
        try {
            tmp = new ObjectInputStream(
                input
            );
        } catch (IOException e) {
            System.err.println("ERROR while opening temporary indexing file " + filename);
            e.printStackTrace();
            System.exit(2);
        }

        ObjectInputStream objInput = tmp;

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

                Entry<T, D> entry = null;
                try {
                    entry = (Entry<T, D>) objInput.readObject();
                    if (entry == null) {
                        closed = true;
                        objInput.close();
                        return false;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("ERROR while reading from temporary index file " + filename);
                    e.printStackTrace();
                    System.exit(2);
                }

                currentEntry = entry;

                return true;
            }

            /**
             * Gets the next entry on the iterator. <b>This iterator assumes a
             *  hasNext() call with return value true and only then a call of next()</b>
             *
             * @return the next entry on the temporary file
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

}
