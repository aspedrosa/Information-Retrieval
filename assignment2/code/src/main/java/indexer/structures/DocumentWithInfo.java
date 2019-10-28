package indexer.structures;

public class DocumentWithInfo<V> extends BlockWithInfo<Integer, V> implements BaseDocument {

    /**
     * Main constructor
     *
     * @param key       block identifier
     * @param extraInfo to store
     */
    public DocumentWithInfo(Integer key, V extraInfo) {
        super(key, extraInfo);
    }

    @Override
    public int getDocId() {
        return key;
    }
}
