package parsers.files;

import parsers.documents.Document;
import parsers.documents.DocumentParser;
import parsers.documents.TrecAsciiMedline2004DocParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class TrecAsciiMedline2004FileParser extends FileParser {

    public TrecAsciiMedline2004FileParser(InputStream input) throws IOException {
        super(input);
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
    public Iterator<Document> iterator() {
        return new InternalIterator();
    }

    private class InternalIterator implements Iterator<Document> {

        private Document currentDocument;

        @Override
        public boolean hasNext() {
            // list to accumulate the content of a document
            List<String> document = new LinkedList<>();

            try {
                while (reader.ready()) {
                    String line = reader.readLine();

                    // if the line is empty the previous document ended
                    if (line.equals("")) {
                        // so instantiate a document parser to parse it
                        DocumentParser documentParser = new TrecAsciiMedline2004DocParser(document);
                        currentDocument = documentParser.parse();

                        return true;
                    } else {
                        // else keep adding the lines to the document content list
                        document.add(line);
                    }
                }
            }
            catch (IOException e) {
                currentDocument = null;
                return true;
            }

            return false;
        }

        @Override
        public Document next() {
            return currentDocument;
        }
    }
}
