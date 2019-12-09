package data_containers.indexer.weights_calculation.searching;

import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermWithInfo;
import data_containers.indexer.structures.aux_structs.DocumentWeight;

import java.util.List;
import java.util.Map;

public interface CalculationsBase {

    Map<String, Float> calculateTermFrequency(List<String> terms);

    float applyDocumentFrequency(float termFrequency, TermWithInfo<Float> term, List<DocumentWithInfo<DocumentWeight>> postingList);

    float applyNormalization(float weight);

    void resetNormalization();

}
