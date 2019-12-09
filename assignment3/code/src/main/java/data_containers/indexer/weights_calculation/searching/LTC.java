package data_containers.indexer.weights_calculation.searching;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoWithIDF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LTC<
    T extends Comparable<T>,
    W extends Number,
    D extends Document<W>,
    I extends TermInfoWithIDF<W, D>
    > implements CalculationsBase<T, W, D, I> {

    private float cosineNormalization = 0;

    @Override
    public Map<T, Float> calculateTermFrequency(List<T> terms) {
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
    public float applyDocumentFrequency(float termFrequency, I termInfo) {
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
