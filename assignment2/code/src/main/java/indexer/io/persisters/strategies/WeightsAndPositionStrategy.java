package indexer.io.persisters.strategies;

import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeightAndPositions;

import java.util.List;

/**
 * Output strategy to format the entries indexer with weights and positions.
 * term:weight;doc1:weight,1,2;doc2:weight,3,4;...
 */
public class WeightsAndPositionStrategy extends IndexerStrategy<TermWithInfo<Float>, DocumentWithInfo<DocumentWeightAndPositions>> {

    /**
     * Main constructor
     */
    public WeightsAndPositionStrategy() {
        super(";".getBytes());
    }

    @Override
    public byte[] handleKey(TermWithInfo<Float> key) {
        return String.format("%s:%.3f", key.getTerm(), key.getExtraInfo()).getBytes();
    }

    @Override
    public byte[] handleDocument(DocumentWithInfo<DocumentWeightAndPositions> document) {
        StringBuilder sb = new StringBuilder(
            String.format("%s:%.3f:", document.getDocId(), document.getExtraInfo().getWeight())
        );

        List<Integer> positions = document.getExtraInfo().getPositions();
        for (int i = 0; i < positions.size(); i++) {
            sb.append(positions.get(i));

            if (i < positions.size() - 1) {
                sb.append(',');
            }
        }

        return sb.toString().getBytes();
    }

}
