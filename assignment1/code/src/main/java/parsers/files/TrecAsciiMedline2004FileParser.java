package parsers.files;

import parsers.documents.Document;
import parsers.documents.DocumentParser;
import parsers.documents.TrecAsciiMedline2004DocParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class TrecAsciiMedline2004FileParser extends FileParser {

    private List<String> documentContent;

    private DocumentParser documentParser;

    public TrecAsciiMedline2004FileParser(InputStream input, String filename) throws IOException {
        super(input, filename);
        documentContent = new LinkedList<>();
        documentParser = new TrecAsciiMedline2004DocParser();
    }

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

    @Override
    public Document handleLine(String line) {
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
