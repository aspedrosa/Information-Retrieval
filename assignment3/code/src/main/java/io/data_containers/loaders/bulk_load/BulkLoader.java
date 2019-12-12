package io.data_containers.loaders.bulk_load;

import io.data_containers.loaders.BaseLoader;

import java.io.IOException;
import java.util.Map;

public abstract class BulkLoader<K, V> implements BaseLoader {

    protected String folder;

    public BulkLoader(String folder) {
        this.folder = folder;
    }

    public abstract Map<K, Object> load(String filename) throws IOException;

    public abstract V getValue(Map<K, Object> loadedMap, K key);

}
