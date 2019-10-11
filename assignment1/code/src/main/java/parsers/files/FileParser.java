package parsers.files;

import parsers.documents.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class in charge of extracting the document(s)
 *  from a specific file
 */
public abstract class FileParser implements Iterable<Document> {

    protected BufferedReader reader;

    protected static Integer readerBufferSize;

    public static void setReaderBufferSize(Integer readerBufferSize) {
        FileParser.readerBufferSize = readerBufferSize;
    }

    public FileParser(InputStream input) throws IOException {
        reader = inputStreamToBufferedReader(input);
    }

    public abstract BufferedReader inputStreamToBufferedReader(InputStream input) throws IOException;
}
