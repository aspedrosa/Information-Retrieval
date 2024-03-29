package indexer.io.persisters.strategies;

import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeight;

/**
 * Formats the output for indexer with weights.
 * term:weight;doc:weight;doc:weight...
 */
public class WeightStrategy extends IndexerStrategy<TermWithInfo<Float>, DocumentWithInfo<DocumentWeight>> {

    /**
     * Main constructor
     */
    public WeightStrategy() {
        super(";".getBytes());
    }

    @Override
    public byte[] handleKey(TermWithInfo<Float> key) {
        return String.format("%s:%.3f", key.getTerm(), key.getExtraInfo()).getBytes();
    }

    @Override
    public byte[] handleDocument(DocumentWithInfo<DocumentWeight> document) {
        return String.format("%d:%.3f", document.getDocId(), document.getExtraInfo().getWeight()).getBytes();
    }

}
