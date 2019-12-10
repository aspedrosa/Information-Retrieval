package data_containers.indexer.post_indexing_actions;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

/**
 * Defines actions to be applied over the inverted index
 *  before persisting it to disk during the indexing process
 *
 * @param <W> type of the weight
 * @param <D> type of the document
 * @param <I> type of the term information
 */
@FunctionalInterface
public interface PostIndexingActions<
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>> {

    void apply(I termInfo);

}
