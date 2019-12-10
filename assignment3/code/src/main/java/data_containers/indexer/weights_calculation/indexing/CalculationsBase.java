package data_containers.indexer.weights_calculation.indexing;

import java.util.Map;

/**
 * Base class to define a variant to calculate weights
 *  during the indexing pipeline for documents
 */
public interface CalculationsBase {

    /**
     * Calculates the weights for documents without normalization
     *
     * @param frequencies natural term frequency for each term
     */
    Map<String, Float> calculateWeights(Map<String, Integer> frequencies);

    /**
     * Applies normalization to a weight according to the weights calculated
     *  on the previous "calculateWeights" call
     */
    float applyNormalization(float weight);

}
