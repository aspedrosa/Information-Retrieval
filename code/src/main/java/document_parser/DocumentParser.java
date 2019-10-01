package document_parser;

import tokenizer.BaseTokenizer;

import java.io.IOException;
import java.io.InputStream;

public abstract class DocumentParser {

    protected InputStream input;

    protected BaseTokenizer tokenizer;

    public DocumentParser(InputStream input, BaseTokenizer tokenizer) {
        this.input = input;
        this.tokenizer = tokenizer;
    }

    public abstract void parse() throws IOException;

}
