package indexer.io.persisters;

import indexer.io.persisters.strategies.OutputStreamStrategy;

/**
 *
 * @param <K> type of the terms
 * @param <V> type of the values
 */
public class CSVPersister<K extends Comparable, V> extends OutputStreamPersister<K, V> {

    /**
     * Main constructor that defines the keyValueSeparator and
     *  the entry terminator according to CSV
     *
     * @param prefixFilename prefix for the files created
     * @param entriesLimitCount maximum of entries per file
     * @param strategy Strategy to transform entries in byte[]
     */
    public CSVPersister(String prefixFilename, int entriesLimitCount, OutputStreamStrategy<K, V> strategy) {
        super(prefixFilename, entriesLimitCount, ",".getBytes(), "\n".getBytes(), strategy);
    }

}
