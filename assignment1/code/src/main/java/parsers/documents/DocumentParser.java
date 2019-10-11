package parsers.documents;

import indexer.BaseIndexer;
import tokenizer.BaseTokenizer;

import java.util.List;

/**
 * Class with the responsibility to
 *  extract needed information from a document.
 * This class assumes that all documents will be indexed
 *  with the same indexer and tokenize with the same
 *  tokenizer.
 * Furthermore this class is in charge of calling
 *  the indexer's registerDocument method
 *  to associate an integer to a document identifier
 */
public abstract class DocumentParser {

    protected int documentId;

    private static int CURRENT_DOC_ID = 1;

    protected List<String> document;

    protected static BaseTokenizer tokenizer;

    protected static BaseIndexer indexer;

    public DocumentParser(List<String> document) {
        this.documentId = CURRENT_DOC_ID++;
        this.document = document;
    }

    public static void setTokenizer(BaseTokenizer newTokenizer) {
        tokenizer = newTokenizer;
    }

    public static void setIndexer(BaseIndexer newIndexer) {
        indexer = newIndexer;
    }

    public abstract void parse();

}
