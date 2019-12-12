package io.data_containers.loaders.bulk_load;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public abstract class LinesLoader<K, V> extends BulkLoader<K, V> {

    public LinesLoader(String folder) {
        super(folder);
    }

    @Override
    public Map<K, Object> load(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(folder + filename));

        return parseLines(lines);
    }

    public abstract Map<K, Object> parseLines(List<String> lines);

}
