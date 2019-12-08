package io.data_containers.loaders.bulk_load;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

public abstract class LinesLoader<K, V> extends BulkLoader<K, V> {

    public LinesLoader(String folder) {
        super(folder);
    }

    @Override
    public Map<K, V> load(String filename) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(folder + filename));

        return parseLines(lines);
    }

    public abstract Map<K,V> parseLines(Stream<String> lines);

}
