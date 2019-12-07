package data_containers.indexer.structures;

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
     * Getter for the extraInfo field
     *
     * @return the block extra info
     */
    public V getExtraInfo() {
        return extraInfo;
    }

    /**
     * Setter for the extraInfo field
     *
     * @param extraInfo the new extra info
     */
    public void setExtraInfo(V extraInfo) {
        this.extraInfo = extraInfo;
    }

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
