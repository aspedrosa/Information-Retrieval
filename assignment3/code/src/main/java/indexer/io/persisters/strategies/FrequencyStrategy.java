package indexer.io.persisters.strategies;

import indexer.structures.DocumentWithInfo;
import indexer.structures.SimpleTerm;

/**
 * Output strategy to format the entries of a frequency indexer
 * term,doc1:freq,doc2:freq,doc3:freq
 */
public class FrequencyStrategy extends IndexerStrategy<SimpleTerm, DocumentWithInfo<Integer>> {

    /**
     * Main constructor
     */
    public FrequencyStrategy() {
        super(",".getBytes());
    }

    @Override
    public byte[] handleKey(SimpleTerm key) {
        return key.getTerm().getBytes();
    }

    @Override
    public byte[] handleDocument(DocumentWithInfo<Integer> document) {
        return (document.getDocId() + ":" + document.getExtraInfo()).getBytes();
    }

}
