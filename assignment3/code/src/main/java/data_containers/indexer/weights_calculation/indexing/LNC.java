package data_containers.indexer.weights_calculation.indexing;

import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the document weights considering:
 * <ul>
 *     <li>logarithmic term frequency</li>
 *     <li>no document frequency</li>
 *     <li>cosine normalization</li>
 * </ul>
 */
public class LNC implements CalculationsBase {

    private float cosineNormalization;

    @Override
    public Map<String, Float> calculateWeights(Map<String, Integer> frequencies) {
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

        // instead of iterating again over the entries to apply the
        //  normalization take advantage of the re-iteration that
        //  the indexing process will do over them to insert into
        //  the posting lists

        return weights;
    }

    @Override
    public float applyNormalization(float weight) {
        return weight / cosineNormalization;
    }

}
