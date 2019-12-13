package io.data_containers.loaders.bulk_load.document_registry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Document registry bulk loader where documents' original
 *  identifier are written in text, one by line
 */
public class LinesLoader extends DocRegBulkLoader {

    public LinesLoader(String folder) {
        super(folder);
    }

    @Override
    public DocumentRegistry load(String filename, int firstDocId) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(folder + filename));

        return new DocumentRegistry(
            firstDocId,
            lines.stream().parallel().map(Integer::parseInt).toArray(Integer[]::new)
        );
    }

}
