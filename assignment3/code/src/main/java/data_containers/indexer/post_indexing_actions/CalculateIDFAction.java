package data_containers.indexer.post_indexing_actions;

import data_containers.DocumentRegistry;
import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermWithInfo;
import data_containers.indexer.structures.aux_structs.DocumentWeight;

import java.util.List;

/**
 * Calculates the weights that can only be
 *  calculated at the end of the indexing processing
 *
 * @param <V> type of the extra info of the
 *  documents, which has a term associated
 */
public class CalculateIDFAction<V extends DocumentWeight>
    implements PostIndexingActions<TermWithInfo<Float>, DocumentWithInfo<V>> {

    @Override
    public void apply(TermWithInfo<Float> term, List<DocumentWithInfo<V>> postingList) {
        term.setExtraInfo((float)
            Math.log10((double)
                DocumentRegistry.getNumberOfDocuments()
                /
                postingList.size()
            )
        );
    }

}
