package indexer.structures;

/**
 * Document that holds just it's document id
 */
public class SimpleDocument extends Block<Integer> implements BaseDocument {

    /**
     * Class constructor
     *
     * @param key block identifier
     */
    public SimpleDocument(Integer key) {
        super(key);
    }

    /**
     * Getters for the document id
     *
     * @return document's id
     */
    @Override
    public int getDocId() {
        return key;
    }

}
