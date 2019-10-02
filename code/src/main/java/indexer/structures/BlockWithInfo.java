package indexer.structures;

public class BlockWithInfo<T> extends Block {

    protected T extraInfo;

    protected BlockWithInfo(String key, T extraInfo) {
        super(key);
        this.extraInfo = extraInfo;
    }

}
