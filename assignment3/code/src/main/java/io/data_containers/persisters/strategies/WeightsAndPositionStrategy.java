package io.data_containers.persisters.strategies;

import data_containers.indexer.structures.DocumentWithInfo;

import java.util.List;

/**
 * Output strategy to format the entries indexer with weights and positions.
 * term:weight;doc1:weight,1,2;doc2:weight,3,4;...
 */
public class WeightsAndPositionStrategy extends WeightStrategyBase<Float, DocumentWithInfo<Float, List<Integer>>> {

    @Override
    public byte[] handleDocument(DocumentWithInfo<Float, List<Integer>> document) {
        StringBuilder sb = new StringBuilder(
            String.format("%s:%." + precision + "f:", document.getDocId(), document.getWeight())
        );

        List<Integer> positions = document.getExtraInfo();
        for (int p = 0; p < positions.size(); p++) {
            sb.append(positions.get(p));

            if (p < positions.size() - 1) {
                sb.append(',');
            }
        }

        return sb.toString().getBytes();
    }

}
