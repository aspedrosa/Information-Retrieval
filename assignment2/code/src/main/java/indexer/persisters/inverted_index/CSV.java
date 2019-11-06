package indexer.persisters.inverted_index;

import indexer.persisters.Constants;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
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
     *   posting list and is in charge of writing it
     *   to the stream.
     *
     * @param output where to write the list of documents
     * @param documents documents to format
     */
    @Override
    public void handleDocuments(OutputStream output, List<D> documents) throws IOException {
        for (int i = 0; i < documents.size(); i++) {
            byte[] document = handleDocument(documents.get(i)).getBytes();

            output.write(document, 0, document.length);

            if (i < documents.size() - 1) {
                output.write(Constants.COMMA, 0, Constants.COMMA.length);
            }
        }
    }

    /**
     * Method that defines the format of a single document
     *
     * @param document to format
     * @return a formatted string representing the document
     */
    public abstract String handleDocument(D document);
}
