package io.data_containers.loaders.bulk_load;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermInfoWithIDF;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class WeightsAndPositionsIndexerLoader extends LinesLoader<String, TermInfoWithIDF<Float, DocumentWithInfo<Float, List<Integer>>>> {

    private static final Pattern separatorsRegex = Pattern.compile("[:;]");

    private static final Pattern positionsSeparatorsRegex = Pattern.compile(",");

    public WeightsAndPositionsIndexerLoader(String folder) {
        super(folder);
    }

    @Override
    public Map<String, Object> parseLines(List<String> lines) {
        Map<String, Object> map = new ConcurrentHashMap<>(lines.size());

        lines.stream().parallel().forEach(line -> {
            int twoPointsIdx = line.indexOf(':');

            map.put(
                line.substring(0, twoPointsIdx),
                line.substring(twoPointsIdx + 1)
            );
        });

        return map;
    }

    @Override
    public TermInfoWithIDF<Float, DocumentWithInfo<Float, List<Integer>>> getValue(Map<String, Object> loadedMap, String term) {
        Object value = loadedMap.get(term);

        if (value == null) {
            return null;
        }

        if (value instanceof TermInfoWithIDF) {
            return (TermInfoWithIDF<Float, DocumentWithInfo<Float, List<Integer>>>) value;
        }
        else {
            String[] elements = separatorsRegex.split((String) value);

            List<DocumentWithInfo<Float, List<Integer>>> postingList = new ArrayList<>((elements.length - 1) / 3);

            for (int i = 1; i < elements.length; i += 3) {
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

            TermInfoWithIDF<Float, DocumentWithInfo<Float, List<Integer>>> termInfo = new TermInfoWithIDF<>(
                postingList,
                Float.parseFloat(elements[0]) // idf
            );

            loadedMap.put(term, termInfo);

            return termInfo;
        }
    }

}
