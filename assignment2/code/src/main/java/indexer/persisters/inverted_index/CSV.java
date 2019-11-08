package indexer.persisters.inverted_index;

import indexer.persisters.Constants;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Writes term's information in a csv format
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public abstract class CSV<T extends Block & BaseTerm,
                          D extends Block & BaseDocument> extends ForEachEntryPersister<T, D> {

    /**
     * Pattern to split fields in each line
     */
    private static final Pattern comma = Pattern.compile(",");

    /**
     * Main constructor
     */
    public CSV() {
        super(Constants.COMMA, Constants.NEWLINE);
    }

    /**
     * Function that dictates the output format of the
     *   posting list and is in charge of writing it
     *   to the stream.
     *
     * @param output where to write the list of documents
     * @param documents documents to format
     */
    @Override
    public void handleDocuments(OutputStream output, List<D> documents) throws IOException {
        for (int i = 0; i < documents.size(); i++) {
            byte[] document = handleDocument(documents.get(i)).getBytes();

            output.write(document, 0, document.length);

            if (i < documents.size() - 1) {
                output.write(Constants.COMMA, 0, Constants.COMMA.length);
            }
        }
    }

    /**
     * Method that defines the format of a single document
     *
     * @param document to format
     * @return a formatted string representing the document
     */
    public abstract String handleDocument(D document);

    /**
     * Creates an iterator used to read, one by one,
     *  the entries present on a file written
     *  by the the same persister
     *
     * @param input from where to read the entries
     * @param filename the name of the file to read from. Used for message errors
     * @return iterator to read from a previously written file by this persister
     */
    @Override
    public Iterator<Entry<T, D>> load(InputStream input, String filename) {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                input
            )
        );

        return new Iterator<Entry<T, D>> () {

            private Entry<T, D> currentEntry;

            private boolean closed = false;

            @Override
            public boolean hasNext() {
                if (currentEntry != null) {
                    return true;
                }
                else if (closed) {
                    return false;
                }

                String line = null;
                try {
                    line = reader.readLine();

                    if (line == null) {
                        reader.close();
                        closed = true;
                        return false;
                    }
                } catch (IOException e) {
                    System.err.println("ERROR while loading index file " + filename);
                    e.printStackTrace();
                    System.exit(2);
                }

                currentEntry = createEntry(comma.split(line));

                return true;
            }

            @Override
            public Entry<T, D> next() {
                if (currentEntry == null) {
                    throw new NoSuchElementException();
                }

                Entry<T, D> tmp = currentEntry;

                currentEntry = null;

                return tmp;
            }

        };
    }

    /**
     * Child classes should parse the entry's content
     *  and create terms and documents accordingly to
     *  their specific type.
     *
     * @param entry entry's content
     * @return an entry with term and its posting list
     */
    public abstract Entry<T, D> createEntry(String[] entry);

}
