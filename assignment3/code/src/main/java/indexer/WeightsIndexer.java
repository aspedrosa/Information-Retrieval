package indexer;

import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeight;
import indexer.weights_calculation.CalculationsBase;

public class WeightsIndexer extends WeightsIndexerBase<DocumentWeight> {

    /**
     * Main constructor
     */
    public WeightsIndexer(CalculationsBase calculations) {
        super(calculations);
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
}
