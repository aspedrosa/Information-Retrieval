package indexer.io.persisters.strategies;

import java.io.OutputStream;

/**
 * Strategy to transform entries in bytes for the
 *  output stream persister
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 */
public abstract class OutputStreamStrategy<K extends Comparable, V> {

    /**
     * To write between keys and values
     */
    protected byte[] keyValueSeparator;

    /**
     * To write after an entry
     */
    protected byte[] entryTerminator;

    /**
     * Main constructor
     *
     * @param keyValueSeparator To write between keys and values
     * @param entryTerminator To write after an entry
     */
    public OutputStreamStrategy(byte[] keyValueSeparator, byte[] entryTerminator) {
        this.keyValueSeparator = keyValueSeparator;
        this.entryTerminator = entryTerminator;
    }

    /**
     * Getter of the key and value separator
     *
     * @return to write after the key
     */
    public byte[] getKeyValueSeparator() {
        return keyValueSeparator;
    }

    /**
     * Getter of the entry terminator field
     *
     * @return to write after an entry
     */
    public byte[] getEntryTerminator() {
        return entryTerminator;
    }

    /**
     * Transforms the key into bytes
     *
     * @param key to transform
     * @return a byte representation of the key
     */
    public abstract byte[] handleKey(K key);

    /**
     * Writes to the stream the value of the entry.
     *  This doesn't follow the same approach as the handle key
     *  since on the inverted index the value is a list. Since this
     *  list can be gigantic, the user would have to create a String
     *  representation and then convert to bytes what can lead to
     *  OutOfMemoryException.
     *
     * @param output output stream
     * @param value to write
     */
    public abstract void handleValue(OutputStream output, V value);
}
