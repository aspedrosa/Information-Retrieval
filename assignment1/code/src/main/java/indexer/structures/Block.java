package indexer.structures;

public class Block<T extends Comparable<T>> implements Comparable<Block<T>> {

    protected T key;

    protected Block(T key) {
        this.key = key;
    }

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

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public int compareTo(Block<T> block) {
        return key.compareTo(block.key);
    }
}
