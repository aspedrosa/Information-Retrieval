package indexer.structures;

public class BlockWithInfo<K extends Comparable<K>, V> extends Block<K> {

    protected V extraInfo;

    protected BlockWithInfo(K key, V extraInfo) {
        super(key);
        this.extraInfo = extraInfo;
    }

}
