package data_containers.indexer.weights_calculation.indexing;

import data_containers.indexer.structures.BaseDocument;
import data_containers.indexer.structures.TermWithInfo;

import java.util.List;
import java.util.Map;

public interface CalculationsBase {

    Map<String, Float> calculateWeights(Map<String, Integer> frequencies);

    float applyNormalization(float weight);

}
