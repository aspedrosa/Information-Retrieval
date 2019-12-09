package data_containers.indexer.weights_calculation.indexing;

import java.util.Map;

public interface CalculationsBase {

    Map<String, Float> calculateWeights(Map<String, Integer> frequencies);

    float applyNormalization(float weight);

}
