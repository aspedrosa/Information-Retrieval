package data_containers.indexer.weights_calculation.searching;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

import java.util.List;
import java.util.Map;

public interface CalculationsBase<
    T extends Comparable<T>,
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>> {

    Map<T, Float> calculateTermFrequencyWeights(List<T> terms);

    float applyDocumentFrequencyWeights(float termFrequency, I termInfo);

    float applyNormalization(float weight);

    void resetNormalization();

}
