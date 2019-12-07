package io.loaders.bulk_load;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

public abstract class LinesLoader<K, V> extends BulkLoader<K, V> {

    @Override
    public Map<K, V> load(String filename) {
        Stream<String> lines = null;

        try {
            lines = Files.lines(Paths.get(filename));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        return parseLines(lines);
    }

    public abstract Map<K,V> parseLines(Stream<String> lines);

}
