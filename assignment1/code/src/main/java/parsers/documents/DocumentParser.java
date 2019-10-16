package parsers.documents;

import java.util.List;

/**
 * Class with the responsibility to
 *  extract needed information from a document.
 * This class assumes that all documents will be indexed
 *  with the same indexer and tokenize with the same
 *  tokenizer.
 */
public interface DocumentParser {

    /**
     * Parses document's content and extracts the information
     *  to be tokenized
     *
     * @param documentContent to parse
     * @return Document instance with the content to tokenize
     */
    Document parse(List<String> documentContent);

}
