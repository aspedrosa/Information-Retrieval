package indexer.persisters.inverted_index;

import indexer.persisters.PostIndexingActions;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * All persisting strategies for the inverted index must
 *  implement this interface
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public interface InvertedIndexBasePersister<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    /**
     * Method called to by the a BaseIndexer to write the inverted index
     *  to the output stream
     *
     * @param output to where the index will be written
     * @param invertedIndex of the indexer
     * @param postIndexingActions actions to apply to the inverted index before persisting
     * @throws IOException if some error occurs while writing the index to the stream
     */
    void persist(OutputStream output, Map<T, List<D>> invertedIndex, PostIndexingActions<T, D> postIndexingActions) throws IOException;

}
