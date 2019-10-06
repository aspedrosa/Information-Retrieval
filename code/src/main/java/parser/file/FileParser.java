package parser.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class in charge of extracting the document(s)
 *  from a specific file
 */
public abstract class FileParser {

    protected InputStream input;

    public FileParser(InputStream input) {
        this.input = input;
    }

    public abstract void parse() throws IOException;
}
