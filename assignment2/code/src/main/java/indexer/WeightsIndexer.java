package indexer;

import indexer.post_indexing_actions.CalculateWeightsPostIndexingAction;
import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeight;

import java.util.LinkedList;
import java.util.List;
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
        frequencies.forEach((term, count) -> {
            dummyTerm.setTerm(term);

            List<DocumentWithInfo<DocumentWeight>> postingList = invertedIndex.get(dummyTerm);

            if (postingList == null) {
                postingList = new LinkedList<>();
                invertedIndex.put(new TermWithInfo<>(term, .0f), postingList);
            }

            postingList.add(new DocumentWithInfo<>(documentId, new DocumentWeight(count)));
        });
    }

}
