package data_containers.indexer;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

import java.io.Serializable;
import java.util.Map;

/**
 * Interface that indexers implement
 * With this we can create a generic searcher
 *  that is able to create different indexers
 *
 * @param <T> type of the terms
 * @param <W> type of the wights of the documents
 * @param <D> type of the documents
 * @param <I> type of the term information
 */
public interface IndexerProvider<
    T extends Comparable<T> & Serializable,
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>> {

    /**
     * Creates a indexer with the received inverted index
     *
     * @param loadedIndex the internal inverted index for the new indexer
     */
    BaseIndexer<T, W, D, I> createIndexer(Map<T, I> loadedIndex);

}
