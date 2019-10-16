package indexer.structures;

/**
 * Implementation of a document to store on a posting list
 * This specific implementation holds the document id and the
 *  therm frequency on this document
 */
public class DocumentWithFrequency extends BlockWithInfo<Integer, Integer> implements BaseDocument {

    /**
     * Constructor initializing frequency at 0
     *
     * @param docId of the new document
     */
    public DocumentWithFrequency(int docId) {
        super(docId, 0);
    }

    /**
     * Constructor allowing the user to choose the
     *  term frequency
     *
     * @param docId of the new document
     * @param frequency of the new document
     */
    public DocumentWithFrequency(int docId, int frequency) {
        super(docId, frequency);
    }

    /**
     * Getter for the document Id
     *
     * @return document id
     */
    public int getDocId() {
        return key;
    }

    /**
     * Function to allow update on the term frequency
     *  after instantiation
     */
    public void increaseFrequency() {
        extraInfo++;
    }

    /**
     * Gets the term frequency on this document
     *
     * @return term frequency on this document
     */
    public int getFrequency() {
        return extraInfo;
    }

}
