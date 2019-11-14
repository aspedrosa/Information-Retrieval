package indexer;

import indexer.post_indexing_actions.CalculateWeightsPostIndexingAction;
import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeight;

import java.util.Map;

public class WeightsIndexer extends WeightsIndexerBase<DocumentWeight> {

    /**
     * Main constructor
     *
     * @param postIndexingActions calculates the weights that can only be
     *  calculated at the end of the indexing processing
     */
    public WeightsIndexer(CalculateWeightsPostIndexingAction<DocumentWeight> postIndexingActions) {
        super(postIndexingActions);
        dummyTerm = new TermWithInfo<>();
    }

    @Override
    protected void insertDocument(int documentId, Map<String, Integer> frequencies) {
        // TODO
    }

}
