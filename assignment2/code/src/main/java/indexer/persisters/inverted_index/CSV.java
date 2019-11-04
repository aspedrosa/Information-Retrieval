package indexer.persisters.inverted_index;

import indexer.persisters.Constants;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.util.List;

/**
 * Writes term's information in a csv format
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public abstract class CSV<T extends Block & BaseTerm,
                                          D extends Block & BaseDocument> extends ForEachEntryPersister<T, D> {

    /**
     * Main constructor
     */
    public CSV() {
        super(Constants.COMMA, Constants.NEWLINE);
    }

    /**
     * Function that dictates the output format of the
     *   posting list.
     *
     * @param documents documents to format
     * @return a formatted string representing the posting list
     */
    @Override
    public String handleDocuments(List<D> documents) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < documents.size(); i++) {
            sb.append(handleDocument(documents.get(i)));

            if (i < documents.size() - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    /**
     * Method that defines the format of a single document
     *
     * @param document to format
     * @return a formatted string representing the document
     */
    public abstract String handleDocument(D document);
}
