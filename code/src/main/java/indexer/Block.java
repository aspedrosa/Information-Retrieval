package indexer;

public class Block<T> implements Comparable<Block<?>> {

    protected String key;

    protected T extraInfo;

    protected Block(String key) {
        this.key = key;
    }

    protected Block(String key, T extraInfo) {
        this.key = key;
        this.extraInfo = extraInfo;
    }

    public T getExtraInfo() {
        return extraInfo;
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
    public int compareTo(Block<?> tBlock) {
        return key.compareTo(tBlock.key);
    }
}
