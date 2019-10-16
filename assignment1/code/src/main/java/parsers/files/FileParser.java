package parsers.files;

import parsers.documents.Document;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Class in charge of extracting the document(s)
 *  from a specific file
 * Child classes parse different types of files.
 */
public abstract class FileParser implements Iterable<Document>, Closeable {

    /**
     * Class from where these classes will read
     *  the file. A buffered reader is used to allow
     *  the reading process to be done in blocks
     */
    protected BufferedReader reader;

    /**
     * Since in characters of the buffer of the BufferReader
     */
    protected static Integer readerBufferSize;

    /**
     * Absolute path to the file that this FileParser is parsing
     * Mainly used to display error messages
     */
    private String filename;

    /**
     * Setter of the readerBufferSize
     *
     * @param readerBufferSize the new value to assign
     * @throws IllegalArgumentException if it the received readerBufferSize
     *  is &lt;= 0
     */
    public static void setReaderBufferSize(Integer readerBufferSize) {
        if (readerBufferSize == null) {
            return;
        }
        else if (readerBufferSize <= 0) {
            throw new IllegalArgumentException("readerBufferSize must be > 0");
        }

        FileParser.readerBufferSize = readerBufferSize;
    }

    /**
     * Getter of the filename field
     *
     * @return filename field
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Main constructor
     *
     * @param input stream to read from
     * @param filename absolute path to file to parse
     * @throws IOException if some error occur while creating the BufferedReader
     */
    public FileParser(InputStream input, String filename) throws IOException {
        System.out.println("Parsing file " + filename);

        this.reader = inputStreamToBufferedReader(input);
        this.filename = filename;
    }

    /**
     * This method allows extendability in terms of how
     *  the file is read.
     * On this method child classes must create the necessary
     *  wrappers around InputStream to create a BufferedReader
     *  appropriated to the file that will be parsed
     *
     * @param input stream to read from
     * @return a BufferedReader instance
     * @throws IOException if some error occurs while creating the
     *  buffered reader
     */
    public abstract BufferedReader inputStreamToBufferedReader(InputStream input) throws IOException;

    /**
     * Since different files have different formats, on
     *  this method child classes should parse the lines
     *  considering their specific format
     *
     * @param line to parse
     * @return a Document object if the parsed lines until
     *  now result in a document, null otherwise
     */
    public abstract Document handleLine(String line);

    /**
     * Gets and Iterator object to iterate over
     *  the documents present in the file being
     *  parsed
     *
     * @return an Iterator object
     */
    @Override
    public final Iterator<Document> iterator() {
        return new InternalIterator();
    }

    /**
     * Iterator to retrieve documents from the file
     * <strong>This iterator was implemented assuming that the user
     *  will call hasNext() before a next() call</strong>
     */
    private class InternalIterator implements Iterator<Document> {

        /**
         * The document to return in the next next() call
         */
        private Document currentDocument;

        /**
         * Fetches the reader until it founds a document
         *
         * @return true if a document is found or was
         *  already found, false otherwise
         */
        @Override
        public boolean hasNext() {

            // if it already fetched a document
            if (currentDocument != null) {
                return true;
            }

            try {
                while (reader.ready()) {
                    Document document = handleLine(reader.readLine());

                    // if the parsing of the current line
                    //  generated a document
                    if (document != null) {
                        currentDocument = document;
                        return true;
                    }
                }
            }
            catch (IOException e) {
                System.err.println("ERROR reading from file " + filename + "\n");
                return false;
            }

            return false;
        }

        /**
         * Gets the fetched document on the hasNext() call
         *
         * @return a Document object
         * @throws NoSuchElementException if the current document
         *  is null
         */
        @Override
        public Document next() {
            if (currentDocument == null) {
                throw new NoSuchElementException();
            }

            Document tmp = currentDocument;

            currentDocument = null;

            return tmp;
        }
    }

    /**
     * The user must call this method when they
     *  some reading from the reader
     *
     * @throws IOException if some error occurs while closing
     *  the reader
     */
    @Override
    public final void close() throws IOException {
        reader.close();
    }
}
