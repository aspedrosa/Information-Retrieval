package indexer.structures.aux_structs;

import java.util.List;

/**
 * To use as extraInfo of the class BlockWithInfo
 *  and use the Block as a Document
 */
public class DocumentWeightAndPositions {

    /**
     * Weight of a document for a given term
     */
    private float weight;

    /**
     * Stores the positions, after the tokenization, of the terms
     */
    private List<Integer> positions;

}
