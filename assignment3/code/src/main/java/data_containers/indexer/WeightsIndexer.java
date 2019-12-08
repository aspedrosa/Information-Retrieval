package data_containers.indexer;

import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermWithInfo;
import data_containers.indexer.structures.aux_structs.DocumentWeight;
import data_containers.indexer.weights_calculation.CalculationsBase;

import java.util.List;
import java.util.Map;

public class WeightsIndexer extends WeightsIndexerBase<DocumentWeight> {

    /**
     * Main constructor
     */
    public WeightsIndexer(CalculationsBase calculations) {
        super(calculations);
        this.dummyTerm = new TermWithInfo<>();
    }

    public WeightsIndexer(
        CalculationsBase calculations,
        Map<TermWithInfo<Float>, List<DocumentWithInfo<DocumentWeight>>> loadedIndex
        ) {
        super(calculations, loadedIndex);
        this.dummyTerm = new TermWithInfo<>();
    }

    @Override
    public DocumentWithInfo<DocumentWeight> createDocument(int documentId, DocumentWeight weight) {
        return new DocumentWithInfo<>(
            documentId,
            weight
        );
    }

    @Override
    public DocumentWeight createDocumentWeight(String term, float weight) {
        return new DocumentWeight(
            weight
        );
    }

    @Override
    public BaseIndexer<TermWithInfo<Float>, DocumentWithInfo<DocumentWeight>> createIndexer(
        Map<TermWithInfo<Float>, List<DocumentWithInfo<DocumentWeight>>> loadedIndex) {
        return new WeightsIndexer(this.calculations, loadedIndex);
    }
}
