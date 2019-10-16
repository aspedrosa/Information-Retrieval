package indexer.structures;

/**
 * Implementation of a term to be stored on the inverted index
 * This implementation holds the term and it's document frequency
 */
public class TermWithFrequency extends BlockWithInfo<String, Integer> implements BaseTerm {

    /**
     * Main constructor
     *
     * @param term the term itself
     * @param frequency of the term across all documents
     */
    public TermWithFrequency(String term, Integer frequency) {
        super(term, frequency);
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
     * Gets the term frequency across all documents
     *
     * @return term frequency
     */
    public int getFrequency() {
        return extraInfo;
    }

    /**
     * Function to allow update on the term frequency
     *  after instantiation
     *
     * @param quantity to add to the current frequency
     */
    public void increaseFrequency(int quantity) {
        extraInfo += quantity;
    }
}
