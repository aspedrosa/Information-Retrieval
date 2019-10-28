package indexer.structures;

import indexer.structures.BaseTerm;
import indexer.structures.BlockWithInfo;

public class TermWithInfo<V> extends BlockWithInfo<String, V> implements BaseTerm {

    /**
     * Main constructor
     *
     * @param key       block identifier
     * @param extraInfo to store
     */
    public TermWithInfo(String key, V extraInfo) {
        super(key, extraInfo);
    }

    @Override
    public String getTerm() {
        return key;
    }

}
