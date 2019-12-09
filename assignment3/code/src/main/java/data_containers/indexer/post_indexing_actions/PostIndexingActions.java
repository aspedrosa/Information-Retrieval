package data_containers.indexer.post_indexing_actions;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

@FunctionalInterface
public interface PostIndexingActions<
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>> {

    void apply(I termInfo);

}
