package indexer.structures;

/**
 * Document that stores some extra info associated
 *  with it
 *
 * @param <V> type of the extra info
 */
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

    /**
     * Getter for the document Id
     *
     * @return document id
     */
    @Override
    public int getDocId() {
        return key;
    }
}
