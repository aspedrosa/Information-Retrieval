package indexer.weights_calculation;

import java.util.Map;

public interface CalculationsBase {

    Map<String, Float> preNormalization(Map<String, Integer> frequencies);

    float applyNormalization(float weight);

}
