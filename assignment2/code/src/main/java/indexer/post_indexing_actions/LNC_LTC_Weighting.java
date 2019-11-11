package indexer.post_indexing_actions;

import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeight;

import java.util.List;

/**
 * Applies the lnc.ltc tf-idf weighting variant
 *
 * @param <V> type of the extra info of the
 *  documents, which has a term associated
 */
public class LNC_LTC_Weighting<V extends DocumentWeight> implements CalculateWeightsPostIndexingAction<V>  {

    @Override
    public void apply(TermWithInfo<Float> term, List<DocumentWithInfo<V>> postingList) {
        // TODO
    }

}
