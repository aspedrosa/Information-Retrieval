package indexer;

import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;

import java.util.Map;

public abstract class TFIDFIndexerBase<V> extends BaseIndexer<TermWithInfo<Float>, DocumentWithInfo<V>> {

    protected void insertDocument(int documentId, Map<String, Integer> frequencies) {
        // TODO
    }

}
