package indexer;

import indexer.post_indexing_actions.CalculateWeightsPostIndexingAction;
import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeight;

import java.util.Map;

/**
 * Indexes terms associating weights to terms
 *  for weight ranking of documents
 *
 * @param <V> type of the extra info of the
 *  documents, which has a term associated
 */
public class WeightsIndexer <V extends DocumentWeight> extends BaseIndexer<TermWithInfo<Float>, DocumentWithInfo<V>> {

    /**
     * Main constructor
     *
     * @param postIndexingActions calculates the weights that can only be
     *  calculated at the end of the indexing processing
     */
    public WeightsIndexer(CalculateWeightsPostIndexingAction<V> postIndexingActions) {
        super(postIndexingActions);
    }

    protected void insertDocument(int documentId, Map<String, Integer> frequencies) {
        // TODO
    }

}
