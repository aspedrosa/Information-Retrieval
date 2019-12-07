package io.loaders;

import io.loaders.strategies.ReaderStrategy;

import java.util.Map;

/**
 * Specific loader that uses a BufferedReader to
 *  read from the input file
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 */
public class ReaderLoader<K, V> extends BulkLoader<K, V> {

    private ReaderStrategy strategy;

    /**
     * Main constructor
     *
     * @param strategy to create entries from text
     */
    public ReaderLoader(ReaderStrategy strategy) {
        this.strategy = strategy;
    }

    public Map<K, V> load(String filename) {
        return null; // TODO
    }

}
