package io.data_containers.loaders.bulk_load.indexer;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;
import io.data_containers.loaders.BaseLoader;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Base class for bulk loaders of indexers
 * Since terms of the inverted index can have really long
 *  posting lists. The structure returned on the load
 *  method can contain the raw data retrieved from the file
 *  or the objects List<D>. This avoids having to waste time
 *  on converting all the terms into objects since only
 *  some of the term's posting lists will be used
 *
 * @param <K> type of the key
 * @param <W> type of the document weight
 * @param <D> type of the document
 * @param <I> type of the term information
 */
public abstract class IndexerBulkLoader<
    K extends Comparable<K> &Serializable,
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>> implements BaseLoader {

    protected String folder;

    public IndexerBulkLoader(String folder) {
        this.folder = folder;
    }

    /**
     * Loads terms and respective posting lists from a indexer file
     */
    public abstract Map<K, Object> load(String filename) throws IOException;

    /**
     * Since the inverted index returned by the "load" method can still
     *  contain the raw data retrieved from the file, the specific
     *  indexer bulk loader is in charge of parsing that raw data
     *  and updating the map to the necessary objects
     */
    public abstract I getValue(Map<K, Object> loadedMap, K key);

}
