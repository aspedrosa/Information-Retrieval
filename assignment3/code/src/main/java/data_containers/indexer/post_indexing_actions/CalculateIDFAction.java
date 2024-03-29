package data_containers.indexer.post_indexing_actions;

import data_containers.DocumentRegistry;
import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoWithIDF;

/**
 * Before persisting an indexer entry to disk calculates
 *  the idf of the term
 *
 * @param <W> type of the weight
 * @param <D> type of the document class
 */
public class CalculateIDFAction<W extends Number, D extends Document<W>> implements PostIndexingActions<
        W, D, TermInfoWithIDF<W, D>> {

    @Override
    public void apply(TermInfoWithIDF<W, D> termInfo) {
        termInfo.setIdf((float)
            Math.log10((double)
                DocumentRegistry.getNumberOfDocuments()
                /
                termInfo.getPostingList().size()
            )
        );
    }
}
