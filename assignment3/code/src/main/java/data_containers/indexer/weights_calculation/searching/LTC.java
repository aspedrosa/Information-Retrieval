package data_containers.indexer.weights_calculation.searching;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoWithIDF;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates the document weights considering:
 * <ul>
 *     <li>logarithmic term frequency</li>
 *     <li>idf document frequency</li>
 *     <li>cosine normalization</li>
 * </ul>
 *
 * @param <T> type of the term
 * @param <W> type of the weight
 * @param <D> type of the document
 * @param <I> type of the term information
 */
public class LTC<
    T extends Comparable<T> & Serializable,
    W extends Number,
    D extends Document<W>,
    I extends TermInfoWithIDF<W, D>
    > implements SearchingCalculations<T, W, D, I> {

    private float cosineNormalization = 0;

    @Override
    public Map<T, Float> calculateTermFrequencyWeights(List<T> terms) {
        Map<T, Float> termFrequencies = new HashMap<>();

        for (T term : terms) {
            Float frequency = termFrequencies.get(term);

            if (frequency == null) {
                frequency = 1f;
            }
            else {
                frequency++;
            }

            termFrequencies.put(term, frequency);
        }

        termFrequencies.forEach((term, frequency) -> {
            termFrequencies.put(
                term,
                (float) (1 + Math.log10(frequency))
            );
        });

        return termFrequencies;
    }

    @Override
    public float applyDocumentFrequencyWeights(float termFrequency, I termInfo) {
        float weight = termFrequency * termInfo.getIdf();

        cosineNormalization += Math.pow(weight, 2);

        return weight;
    }

    @Override
    public float applyNormalization(float weight) {
        return (float) (weight / Math.sqrt(cosineNormalization));
    }

    @Override
    public void resetNormalization() {
        cosineNormalization = 0;
    }

}
