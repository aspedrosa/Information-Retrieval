package indexer.io.persisters;

import indexer.io.persisters.strategies.OutputStreamStrategy;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Persists a set of entries to a byte output stream
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 */
public class OutputStreamPersister<K extends Comparable, V> extends BasePersister<K, V> {

    /**
     * To write between keys and values
     */
    private byte[] keyValueSeparator;

    /**
     * To write after an entry
     */
    private byte[] entryTerminator;

    /**
     * Currently open output stream to write entries
     */
    private OutputStream currentOutput;

    /**
     * Strategy to transform entries in byte[]
     */
    private OutputStreamStrategy<K, V> strategy;

    /**
     * Main constructor
     *
     * @param prefixFilename prefix for the files created
     * @param entriesLimitCount maximum of entries per file
     * @param keyValueSeparator To write between keys and values
     * @param entryTerminator To write after an entry
     * @param strategy Strategy to transform entries in byte[]
     */
    public OutputStreamPersister(String prefixFilename,
                                 int entriesLimitCount,
                                 byte[] keyValueSeparator,
                                 byte[] entryTerminator,
                                 OutputStreamStrategy<K, V> strategy) {
        super(prefixFilename, entriesLimitCount);
        this.keyValueSeparator = keyValueSeparator;
        this.entryTerminator = entryTerminator;
        this.strategy = strategy;
    }

    @Override
    protected boolean outputIsNull() {
        return currentOutput == null;
    }

    @Override
    protected void createNewOutput(String firstKey) throws IOException {
        currentOutput = new BufferedOutputStream(
            new FileOutputStream(
                String.format("%s_%s_%s", prefixFilename, filesCounter++, firstKey)
            )
        );

        firstKeys.add(firstKey);
    }

    /**
     * Writes to the stream the key, the keyValue separator,
     *  the value and, if it's not the last entry, the entry terminator.
     *  To obtain the key and value byte representation the strategy
     *  is used.
     *
     * @param entry to write
     * @param lastEntry if its the last entry to write to the output
     * @throws IOException if some error occurs while writing an entry
     */
    @Override
    protected void writeEntry(Map.Entry<K, V> entry, boolean lastEntry) throws IOException {
        currentOutput.write(strategy.handleKey(entry.getKey()));
        currentOutput.write(keyValueSeparator, 0, keyValueSeparator.length);
        strategy.handleValue(currentOutput, entry.getValue());

        if (!lastEntry) {
            currentOutput.write(entryTerminator, 0, entryTerminator.length);
        }
    }

    @Override
    public void close() throws IOException {
        if (currentOutput != null) {
            currentOutput.close();
            currentOutput = null;
        }
    }
}
