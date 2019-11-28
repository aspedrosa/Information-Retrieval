
package indexer.structures;

/**
 * Implementation of a term to be stored on the inverted index
 * This implementation holds only the term
 */
public class SimpleTerm extends Block<String> implements BaseTerm {

    /**
     * Default constructor. Used by indexers
     *  to instantiate and use as a dummy term
     */
    public SimpleTerm() {
        super(null);
    }

    /**
     * Main constructor
     *
     * @param term the term
     */
    public SimpleTerm(String term) {
        super(term);
    }

    /**
     * Getter of the term
     *
     * @return the term
     */
    public String getTerm() {
        return key;
    }

    /**
     * Setter of the term
     *
     * @param term the new term
     */
    public void setTerm(String term) {
        this.key = term;
    }

}
