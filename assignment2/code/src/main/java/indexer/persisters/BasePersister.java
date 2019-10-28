package indexer.persisters;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Base class of a strategy to store the two
 *  internal structures of an indexer
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public interface BasePersister<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    /**
     * Applies the persisting strategy
     *
     * @param output the stream to write the strucutres
     * @param invertedIndex of the indexer
     * @param documentIdentification of the indexer
     * @throws IOException if some error occurs while writing
     */
    void persist(OutputStream output, Map<T, List<D>> invertedIndex, Map<Integer, String> documentIdentification ) throws IOException;

}
