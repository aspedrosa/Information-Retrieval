package io.data_containers.persisters.strategies;

import data_containers.indexer.structures.Document;

/**
 * Formats the output for indexer with weights.
 * term:weight;doc:weight;doc:weight...
 */
public class WeightStrategy extends WeightStrategyBase<Float, Document<Float>> {

    public WeightStrategy() {
        super();
    }

    public WeightStrategy(int precision) {
        super(precision);
    }

    @Override
    public byte[] handleDocument(Document<Float> document) {
        return String.format("%d:%." + precision +"f", document.getDocId(), document.getWeight()).getBytes();
    }


}
