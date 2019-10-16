package indexer.persisters;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * All persisting strategies must extend this class
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public interface BasePersister<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    /**
     * Method called to by the indexer to write the inverted index
     *  to the output stream
     *
     * @param output to where the index will be written
     * @param invertedIndex the inverted index held by the indexer
     * @throws IOException if some error occurs while writting the index to the stream
     */
    void persist(OutputStream output, Map<T, List<D>> invertedIndex) throws IOException;

}
