package data_containers.indexer.weights_calculation.searching;

import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermWithInfo;
import data_containers.indexer.structures.aux_structs.DocumentWeight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LTC implements CalculationsBase {

    private float cosineNormalization = 0;

    @Override
    public Map<String, Float> calculateTermFrequency(List<String> terms) {
        Map<String, Float> termFrequencies = new HashMap<>();

        for (String term : terms) {
            Float frequency = termFrequencies.get(term);

            if (frequency == null) {
                frequency = 0f;
            }
            else {
                frequency++;
            }

            termFrequencies.put(term, frequency);
        }

        for (String term : termFrequencies.keySet()) {
            float frequency = termFrequencies.get(term);

            termFrequencies.put(
                term,
                (float) (1 + Math.log10(frequency))
            );
        }

        return termFrequencies;
    }

    @Override
    public float applyDocumentFrequency(float termFrequency, TermWithInfo<Float> term, List<DocumentWithInfo<DocumentWeight>> postingList) {
        float weight = termFrequency * term.getExtraInfo();

        cosineNormalization += Math.pow(weight, 2);

        return weight;
    }

    @Override
    public float applyNormalization(float weight) {
        return (float) (weight / Math.sqrt(weight));
    }

    @Override
    public void resetNormalization() {
        cosineNormalization = 0;
    }

}
