package data_containers.indexer.structures.aux_structs;

import java.util.List;

/**
 * To use as extraInfo of the class BlockWithInfo
 *  and use the Block as a Document
 */
public class DocumentWeightAndPositions extends DocumentWeight {

    /**
     * Stores the positions, after the tokenization, of the terms
     */
    private List<Integer> positions;

    /**
     * Main constructor
     *
     * @param weight weight of a document for a given term
     * @param positions the positions, after the tokenization, of the terms
     */
    public DocumentWeightAndPositions(float weight, List<Integer> positions) {
        super(weight);
        this.positions = positions;
    }

    /**
     * Getter for the positions field
     *
     * @return the positions, after the tokenization, of the terms
     */
    public List<Integer> getPositions() {
        return positions;
    }

}
