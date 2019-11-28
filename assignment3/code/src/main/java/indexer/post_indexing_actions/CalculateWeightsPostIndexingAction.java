package indexer.post_indexing_actions;

import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeight;

/**
 * Calculates the weights that can only be
 *  calculated at the end of the indexing processing
 *
 * @param <V> type of the extra info of the
 *  documents, which has a term associated
 */
public interface CalculateWeightsPostIndexingAction<V extends DocumentWeight>
    extends PostIndexingActions<TermWithInfo<Float>, DocumentWithInfo<V>> {
}
