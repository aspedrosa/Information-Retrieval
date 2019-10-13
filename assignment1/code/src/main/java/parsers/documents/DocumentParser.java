package parsers.documents;

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
public interface DocumentParser {

    Document parse(List<String> documentContent);

}
