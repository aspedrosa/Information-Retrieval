package indexer.structures;

/**
 * Term that stores some extra info associated
 *  with it
 *
 * @param <V> type of the extra info
 */
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

    /**
     * Getter of the term
     *
     * @return the term
     */
    @Override
    public String getTerm() {
        return key;
    }

}
