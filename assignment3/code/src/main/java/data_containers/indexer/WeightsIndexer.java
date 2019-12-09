package data_containers.indexer;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoWithIDF;
import data_containers.indexer.weights_calculation.indexing.CalculationsBase;

import java.util.Map;

public class WeightsIndexer extends WeightsIndexerBase<Document<Float>> {

    /**
     * Main constructor
     */
    public WeightsIndexer(CalculationsBase calculations) {
        super(calculations);
    }

    public WeightsIndexer(
        CalculationsBase calculations,
        Map<String, TermInfoWithIDF<Float, Document<Float>>> loadedIndex
        ) {
        super(calculations, loadedIndex);
    }

    @Override
    public Document<Float> createDocument(int documentId, float weight, String term) {
        return new Document<>(
            documentId,
            weight
        );
    }

    @Override
    public BaseIndexer<String, Float, Document<Float>, TermInfoWithIDF<Float, Document<Float>>> createIndexer(Map<String, TermInfoWithIDF<Float, Document<Float>>> loadedIndex) {
        return new WeightsIndexer(this.calculations, loadedIndex);
    }
}
