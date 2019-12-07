package data_containers.indexer.weights_calculation;

import java.util.HashMap;
import java.util.Map;

public class LNC implements CalculationsBase {

    private float cosineNormalization;

    @Override
    public Map<String, Float> preNormalization(Map<String, Integer> frequencies) {
        Map<String, Float> weights = new HashMap<>(frequencies.size());

        float weightsSquareSum = 0;
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            String term = entry.getKey();
            int wordFrequency = entry.getValue();

            float wordWeight = (float) (1 + Math.log10(wordFrequency));
            weights.put(term, wordWeight);
            weightsSquareSum += Math.pow(wordWeight, 2);
        };

        cosineNormalization = (float) Math.sqrt(weightsSquareSum);

        return weights;
    }

    @Override
    public float applyNormalization(float weight) {
        return weight / cosineNormalization;
    }

}
