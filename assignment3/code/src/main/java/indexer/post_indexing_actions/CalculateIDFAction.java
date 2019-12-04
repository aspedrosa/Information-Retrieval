package indexer.post_indexing_actions;

import indexer.BaseIndexer;
import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeight;
import parsers.documents.Document;

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
                BaseIndexer.getNumberOfDocuments()
                /
                postingList.size()
            )
        );
    }

}
