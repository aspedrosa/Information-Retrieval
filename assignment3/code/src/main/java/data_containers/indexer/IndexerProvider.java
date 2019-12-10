package data_containers.indexer;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

import java.io.Serializable;
import java.util.Map;

public interface IndexerProvider<
    T extends Comparable<T> & Serializable,
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>> {

    BaseIndexer<T, W, D, I> createIndexer(Map<T, I> loadedIndex);

}
