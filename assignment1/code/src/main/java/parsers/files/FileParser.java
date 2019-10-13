package parsers.files;

import parsers.documents.Document;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Class in charge of extracting the document(s)
 *  from a specific file
 */
public abstract class FileParser implements Iterable<Document>, Closeable {

    protected BufferedReader reader;

    protected static Integer readerBufferSize;

    private String filename;

    public static void setReaderBufferSize(Integer readerBufferSize) {
        FileParser.readerBufferSize = readerBufferSize;
    }

    public String getFilename() {
        return filename;
    }

    public FileParser(InputStream input, String filename) throws IOException {
        this.reader = inputStreamToBufferedReader(input);
        this.filename = filename;
    }

    public abstract BufferedReader inputStreamToBufferedReader(InputStream input) throws IOException;

    public abstract Document handleLine(String line);

    @Override
    public final Iterator<Document> iterator() {
        return new InternalIterator();
    }

    private class InternalIterator implements Iterator<Document> {

        private Document currentDocument;

        @Override
        public boolean hasNext() {

            try {
                while (reader.ready()) {
                    Document document = handleLine(reader.readLine());

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

        @Override
        public Document next() {
            return currentDocument;
        }
    }

    @Override
    public final void close() throws IOException {
        reader.close();
    }
}
