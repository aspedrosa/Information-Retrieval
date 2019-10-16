package indexer.structures;

/**
 * Extended version of the Block class that stores
 *  some extra info beyond the block key.
 * e.g. Document with frequency for a specific term
 *
 * @param <K> type of the key
 * @param <V> type of the extra info
 */
public class BlockWithInfo<K extends Comparable<K>, V> extends Block<K> {

    /**
     * the extra info stored
     */
    protected V extraInfo;

    /**
     * Main constructor
     *
     * @param key block identifier
     * @param extraInfo to store
     */
    public BlockWithInfo(K key, V extraInfo) {
        super(key);
        this.extraInfo = extraInfo;
    }

}
