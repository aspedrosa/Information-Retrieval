package indexer.structures.aux_structs;

import java.io.Serializable;

/**
 * To use as extraInfo of the class BlockWithInfo
 *  and use the Block as a Document
 */
public class DocumentWeight implements Serializable {

    /**
     * Weight of a document for a given term
     */
    private float weight;

    /**
     * Main constructor
     *
     * @param weight weight of a document for a given term
     */
    public DocumentWeight(float weight) {
        this.weight = weight;
    }

    /**
     * Getter for the wright field
     *
     * @return weight of a document for a given term
     */
    public float getWeight() {
        return weight;
    }

    /**
     * Setter for the weigth field
     *
     * @param weight weight of a document for a given term
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }
}
