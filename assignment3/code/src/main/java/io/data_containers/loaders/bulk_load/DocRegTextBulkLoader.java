package io.data_containers.loaders.bulk_load;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DocRegTextBulkLoader extends LinesLoader<Integer, String> {

    public DocRegTextBulkLoader(String folder) {
        super(folder);
    }

    @Override
    public Map<Integer, Object> parseLines(List<String> lines) {
        Map<Integer, Object> map = new ConcurrentHashMap<>(lines.size());

        lines.stream().parallel().forEach(line -> {
            int commaIdx = line.indexOf(',');

            map.put(
                Integer.parseInt(line.substring(0, commaIdx)),
                line.substring(commaIdx + 1)
            );
        });

        return map;
    }

    @Override
    public String getValue(Map<Integer, Object> loadedMap, Integer docId) {
        return (String) loadedMap.get(docId);
    }

}
