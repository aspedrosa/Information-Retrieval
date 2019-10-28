package parsers.files;

import parsers.documents.Document;
import parsers.documents.DocumentParser;
import parsers.documents.TrecAsciiMedline2004DocParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Implementation of a specific file parser
 */
public class TrecAsciiMedline2004FileParser extends FileParser {

    /**
     * List to accumulate lines of a document until
     *  a full document is fetched
     */
    private List<String> documentContent;

    /**
     * Instance of DocumentParser to parse
     *  documents
     */
    private DocumentParser documentParser;

    /**
     * Main constructor
     *
     * @param input stream to read from
     * @param filename absolute path to file to parse
     * @throws IOException if some error occur while creating the BufferedReader
     */
    public TrecAsciiMedline2004FileParser(InputStream input, String filename) throws IOException {
        super(input, filename);
        documentContent = new ArrayList<>(100);
        documentParser = new TrecAsciiMedline2004DocParser();
    }

    /**
     * Wrappes the InputStream with GZIPInputStream and then
     *  create a BufferedReader
     *
     * @param input stream to read from
     * @return a BufferedReader instance
     * @throws IOException if some error occurs while creating the
     *  buffered reader
     */
    @Override
    public BufferedReader inputStreamToBufferedReader(InputStream input) throws IOException {
        InputStreamReader inputReader = new InputStreamReader(
            new GZIPInputStream(
                input
            )
        );

        if (readerBufferSize == null) {
            return new BufferedReader(inputReader);
        }
        return new BufferedReader(inputReader, readerBufferSize);
    }

    /**
     * Keeps adding to the documentContent list until an
     *  empty line is found.
     *
     * @param line to parse
     * @return a Document object if the parsed lines until
     *  now result in a document, null otherwise
     */
    @Override
    public Document handleLine(String line) {
        /*
         * WARNING if the last line is not an empty line
         *  the last document will not be indexed
         */
        // if the line is empty the previous document ended
        if (line.equals("")) {
            Document document = documentParser.parse(documentContent);
            documentContent.clear();
            return document;
        }

        // else keep adding the lines to the document content list
        documentContent.add(line);

        return null;
    }
}
