package parser.file;

import parser.document.DocumentParser;
import parser.document.TrecAsciiMedline2004DocParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class TrecAsciiMedline2004FileParser extends FileParser {

    public TrecAsciiMedline2004FileParser(InputStream input) {
        super(input);
    }

    public void parse() throws IOException {
        BufferedReader buffer =
            new BufferedReader(
                new InputStreamReader(
                    new GZIPInputStream(
                        input
                    )
                )
            );

        // list to accumulate the content of a document
        List<String> document = new LinkedList<>();

        while (buffer.ready()) {
            String line = buffer.readLine();

            // if the line is empty the previous document ended
            if (line.equals("")) {
                // so instantiate a document parser to parse it
                DocumentParser documentParser = new TrecAsciiMedline2004DocParser(document);
                documentParser.parse();

                // reset the document content
                document = new LinkedList<>();
            }
            else {
                // else keep adding the lines to the document content list
                document.add(line);
            }
        }
    }
}
