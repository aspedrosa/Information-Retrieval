package indexer.io.persisters.strategies;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Base strategy to format terms and documents
 *  into a CSV format
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public abstract class CSVStrategy<T extends Block & BaseTerm, D extends Block &BaseDocument> extends OutputStreamStrategy<T, List<D>> {

    /**
     * Byte representation of a comma. Avoids having
     *  to instantiate the same byte[] every time
     */
    private static final byte[] COMMA = ",".getBytes();

    /**
     * Iterates over the documents, writing documents one by one
     *  document, transforming them into bytes before write.
     *
     * @param output output stream
     * @param documents to write
     */
    @Override
    public void handleValue(OutputStream output, List<D> documents) {
        for (int i = 0; i < documents.size(); i++) {
            byte[] document = handleDocument(documents.get(i)).getBytes();

            try {
                output.write(document, 0, document.length);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(2);
            }

            if (i < documents.size() - 1) {
                try {
                    output.write(COMMA, 0, COMMA.length);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(2);
                }
            }
        }
    }

    /**
     * Transforms a document into a String
     *  representation
     *
     * @param document to transform
     * @return String representation of the document
     */
    public abstract String handleDocument(D document);

}
