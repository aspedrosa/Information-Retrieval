package io.persisters.strategies;

import data_containers.indexer.structures.BaseDocument;
import data_containers.indexer.structures.BaseTerm;
import data_containers.indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Base strategy to format terms and documents
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public abstract class IndexerStrategy<T extends Block & BaseTerm, D extends Block &BaseDocument> extends OutputStreamStrategy<T, List<D>> {

    /**
     * Main constructor
     *
     * @param separator to write between terms and documents
     */
    public IndexerStrategy(byte[] separator) {
        super(separator, "\n".getBytes());
    }

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
            byte[] document = handleDocument(documents.get(i));

            try {
                output.write(document, 0, document.length);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(2);
            }

            if (i < documents.size() - 1) {
                try {
                    output.write(keyValueSeparator, 0, keyValueSeparator.length);
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
     * @return a byte representation of the document
     */
    public abstract byte[] handleDocument(D document);

}
