package indexer.persisters;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.util.List;

/**
 * This interface defines a function that is defined by indexer to
 *  specify the actions that must be done after the indexing process
 *  and before the final persisting process. This function is passed
 *  as an argument for inverted index persisters
 *
 * @param <T> type of the terms
 * @param <D> type of documents
 */
@FunctionalInterface
public interface PostIndexingActions<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    /**
     * Executes actions after the indexing process and
     *  before the final presisting to an entry of the
     *  inverted index
     *
     * @param term of the entry to operate
     * @param postingList of the entry to operate
     */
    void postIndexingActions(T term, List<D> postingList);

}
