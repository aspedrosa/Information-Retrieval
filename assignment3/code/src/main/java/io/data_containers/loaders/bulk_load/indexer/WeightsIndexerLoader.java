package io.data_containers.loaders.bulk_load.indexer;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoWithIDF;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Specific type of bulk loader for indexer with weights
 */
public class WeightsIndexerLoader extends LinesLoader<
    String,
    Float,
    Document<Float>,
    TermInfoWithIDF<Float, Document<Float>>
    > {

    private static final Pattern separatorsRegex = Pattern.compile("[:;]");

    public WeightsIndexerLoader(String folder) {
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
    public TermInfoWithIDF<Float, Document<Float>> getValue(Map<String, Object> loadedMap, String term) {
        Object value = loadedMap.get(term);

        if (value == null) {
            return null;
        }

        // if the value is already in a TermInfo object
        if (value instanceof TermInfoWithIDF) {
            // return it
            return (TermInfoWithIDF<Float, Document<Float>>) value;
        }
        else {
            // else parse the string and convert it to a TermInfo object
            String[] elements = separatorsRegex.split((String) value);

            List<Document<Float>> postingList = new ArrayList<>((elements.length - 1) / 2);

            for (int i = 1; i < elements.length; i += 2) {
                postingList.add(
                    new Document<>(
                        Integer.parseInt(elements[i]), // docId
                        Float.parseFloat(elements[i + 1])
                    )
                );
            }

            TermInfoWithIDF<Float, Document<Float>> termInfo = new TermInfoWithIDF<>(
                postingList,
                Float.parseFloat(elements[0]) // idf
            );

            // update the term information from raw data to the term information object
            loadedMap.put(term, termInfo);

            return termInfo;
        }
    }

}
