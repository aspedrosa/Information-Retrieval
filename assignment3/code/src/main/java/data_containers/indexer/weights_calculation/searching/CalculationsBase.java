package data_containers.indexer.weights_calculation.searching;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

import java.util.List;
import java.util.Map;

/**
 * Base class to define a variant to calculate weights
 *  during the searching pipeline for queries
 *
 * @param <T> type of the term
 * @param <W> type of the weight
 * @param <D> type of the document
 * @param <I> type of the term information
 */
public interface CalculationsBase<
    T extends Comparable<T>,
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>> {

    /**
     * According to the term present on the query
     *  calculate the term frequency weights
     *
     * @param terms terms of the query
     * @return term frequencies weights
     */
    Map<T, Float> calculateTermFrequencyWeights(List<T> terms);

    /**
     * Apply to the term frequency weight
     *  the pre calculated document frequency weight
     *
     * @param termFrequencyWeight calculated on the method "calculateTermFrequencyWeights"
     * @param termInfo term info where is stored the document frequency
     */
    float applyDocumentFrequencyWeights(float termFrequencyWeight, I termInfo);

    /**
     * Applies normalization to a weights calculated on the method
     *  "calculateTermFrequencyWeights" and "applyDocumentFrequencyWeights"
     */
    float applyNormalization(float weight);

    /**
     * Some calculation may store the accumulative normalization
     *  to then apply, so it is important to reset that accumulative
     *  variable for the next calculation
     */
    void resetNormalization();

}
