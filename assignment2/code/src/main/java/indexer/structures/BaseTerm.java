package indexer.structures;

/**
 * Base interface for all Term classes.
 * Term classes should also extend the Block class
 */
public interface BaseTerm {

    /**
     * Getter of the term
     *
     * @return the term
     */
    String getTerm();

}
