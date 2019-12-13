package data_containers.indexer;

import data_containers.indexer.structures.Document;
import data_containers.indexer.weights_calculation.indexing.IndexingCalculations;

/**
 * Specific type a indexer with ranking weights
 */
public class WeightsIndexer extends WeightsIndexerBase<Document<Float>> {

    /**
     * Main constructor
     */
    public WeightsIndexer(IndexingCalculations calculations) {
        super(calculations);
    }

    @Override
    public Document<Float> createDocument(int documentId, float weight, String term) {
        return new Document<>(
            documentId,
            weight
        );
    }

}
