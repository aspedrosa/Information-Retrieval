package indexer.structures;

import indexer.structures.BaseDocument;
import indexer.structures.Block;

public class SimpleDocument extends Block<Integer> implements BaseDocument {

    /**
     * Class contructor
     *
     * @param key block identifier
     */
    public SimpleDocument(Integer key) {
        super(key);
    }

    @Override
    public int getDocId() {
        return key;
    }

}
