package indexer.structures;

/**
 * Term that stores some extra info associated
 *  with it
 *
 * @param <V> type of the extra info
 */
public class TermWithInfo<V> extends BlockWithInfo<String, V> implements BaseTerm {

    /**
     * Default constructor. Used by indexers
     *  to instantiate and use as a dummy term
     */
    public TermWithInfo() {
        super(null, null);
    }

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

    /**
     * Setter of the term
     *
     * @param newTerm the new term
     */
    @Override
    public void setTerm(String newTerm) {
        this.key = newTerm;
    }

}
