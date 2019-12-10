package io.data_containers.loaders.bulk_load;

import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermInfoWithIDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class WeightsAndPositionsIndexerLoader extends LinesLoader<
    String,
    TermInfoWithIDF<Float, DocumentWithInfo<Float, List<Integer>>>> {

    private static final Pattern separatorsRegex = Pattern.compile("[:;]");

    private static final Pattern positionsSeparatorsRegex = Pattern.compile(",");

    public WeightsAndPositionsIndexerLoader(String folder) {
        super(folder);
    }

    @Override
    public Map<String, TermInfoWithIDF<Float, DocumentWithInfo<Float, List<Integer>>>> parseLines(List<String> lines) {
        Map<String, TermInfoWithIDF<Float, DocumentWithInfo<Float, List<Integer>>>> invertedIndex = new HashMap<>(lines.size());

        for (String line : lines) {
            String[] elements = separatorsRegex.split(line);

            Entry<String, TermInfoWithIDF<Float, DocumentWithInfo<Float, List<Integer>>>> entry = new Entry<>();

            String term = elements[0];

            List<DocumentWithInfo<Float, List<Integer>>> postingList = new ArrayList<>((elements.length - 2) / 3);

            for (int i = 2; i < elements.length; i += 3) {
                String[] positionsStrings = positionsSeparatorsRegex.split(elements[i + 2]);

                List<Integer> positions = new ArrayList<>(positionsStrings.length);
                for (String positionsString : positionsStrings) {
                    positions.add(Integer.parseInt(positionsString));
                }

                postingList.add(
                    new DocumentWithInfo<>(
                        Integer.parseInt(elements[i]), // docId
                        Float.parseFloat(elements[i + 1]),
                        positions
                    )
                );
            }

            invertedIndex.put(
                term,
                new TermInfoWithIDF<>(
                    postingList,
                    Float.parseFloat(elements[1]) // idf
                )
            );
        };

        return invertedIndex;
    }

}
