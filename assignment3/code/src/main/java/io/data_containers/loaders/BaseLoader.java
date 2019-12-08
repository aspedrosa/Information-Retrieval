package io.data_containers.loaders;

import java.io.Serializable;
import java.util.Map;

public interface BaseLoader<K, V> {

    /**
     * Entry to store indexer's internal maps entries, hence
     *  the Serializable extension
     *
     * @param <K> type of the keys
     * @param <V> type of the values
     */
    class Entry<K, V> implements Map.Entry<K, V>, Serializable {

        private K key;

        private V value;

        public Entry() {
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

        public void setKey(K key) {
            this.key = key;
        }

        @Override
        public V setValue(V v) {
            this.value = v;
            return v;
        }
    }


}
