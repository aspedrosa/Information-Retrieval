package indexer.structures;

/**
 * Base type used on the inverted index of the BaseIndexer class for
 *  both keys and values.
 *
 * @param <T> type of the key
 */
public class Block<T extends Comparable<T>> implements Comparable<Block<T>> {

    /**
     * Block indentifier
     */
    protected T key;

    /**
     * Class contructor
     *
     * @param key block identifier
     */
    public Block(T key) {
        this.key = key;
    }

    /**
     * Used to check equality agains other objects
     *
     * @param o other object
     * @return true if it is a block and has the
     *  same identifier, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Block that = (Block) o;
        return key.equals(that.key);
    }

    /**
     * Used by hash dependent data structures
     *
     * @return identifier hashcode
     */
    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Used to order Block objects
     *
     * @param block other block
     * @return the value 0 if the block have the same key block;
     *  a value less than 0 if this block is comparably less than
     *  the block argument; and a value greater than 0 if this block
     *  is comparably greater than the block argument.
     */
    @Override
    public int compareTo(Block<T> block) {
        return key.compareTo(block.key);
    }
}
