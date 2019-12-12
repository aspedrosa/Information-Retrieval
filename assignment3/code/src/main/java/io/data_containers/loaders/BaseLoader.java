package io.data_containers.loaders;

import java.io.Serializable;
import java.util.Map;

/**
 * Base class for the loaders of the data_containers
 */
public interface BaseLoader {

    /**
     * Entry to store data_container's internal maps entries, hence
     *  the Serializable extension
     *
     * @param <K> type of the keys
     * @param <V> type of the values
     */
    class Entry<K, V> implements Map.Entry<K, V>, Serializable {

        private K key;

        private V value;

        public Entry(K key) {
            this.key = key;
        }

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V v) {
            this.value = v;
            return v;
        }
    }


}
