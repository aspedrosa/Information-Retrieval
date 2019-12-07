package io.loaders.bulk_load;

import io.loaders.BaseLoader;

import java.util.Map;

public abstract class BulkLoader<K, V> implements BaseLoader<K, V> {

    public abstract Map<K, V> load(String filename);

}
