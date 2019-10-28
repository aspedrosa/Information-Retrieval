package indexer;

import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.TermWeight;

import java.util.Map;

public abstract class TFIDFIndexerBase<V extends TermWeight> extends BaseIndexer<TermWithInfo<Float>, DocumentWithInfo<V>> {

    protected void insertDocument(int documentId, Map<String, Integer> frequencies) {
        // TODO
    }

}
