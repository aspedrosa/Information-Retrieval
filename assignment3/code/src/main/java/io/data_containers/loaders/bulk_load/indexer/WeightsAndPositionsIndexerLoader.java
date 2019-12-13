package io.data_containers.loaders.bulk_load.indexer;

import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermInfoWithIDF;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Specific type of bulk loader for indexer with weights and positions
 */
public class WeightsAndPositionsIndexerLoader extends LinesLoader<
    String,
    Float,
    DocumentWithInfo<Float, List<Integer>>,
    TermInfoWithIDF<Float, DocumentWithInfo<Float, List<Integer>>>> {

    private static final Pattern separatorsRegex = Pattern.compile("[:;]");

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

        // if the value is already in a TermInfoWithIdf object
        if (value instanceof TermInfoWithIDF) {
            // return it
            return (TermInfoWithIDF<Float, DocumentWithInfo<Float, List<Integer>>>) value;
        }
        else {
            // else parse the string and convert it to a TermInfoWithIdf object
            String[] elements = separatorsRegex.split((String) value);

            List<DocumentWithInfo<Float, List<Integer>>> postingList = new ArrayList<>((elements.length - 1) / 3);

            for (int i = 1; i < elements.length; i += 3) {
                String[] positionsStrings = elements[i + 2].split(",");

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

            // update the term information from raw data to the term information object
            loadedMap.put(term, termInfo);

            return termInfo;
        }
    }

}
