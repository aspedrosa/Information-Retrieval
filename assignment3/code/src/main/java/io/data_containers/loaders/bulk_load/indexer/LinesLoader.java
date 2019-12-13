package io.data_containers.loaders.bulk_load.indexer;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Type of indexer bulk loader where the files stored
 * have the terms information written by line
 *
 * @param <K> type of the terms
 * @param <W> type of the weights
 * @param <D> type of the document
 * @param <I> type of the term information
 */
public abstract class LinesLoader<
    K extends Comparable<K> & Serializable,
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>> extends IndexerBulkLoader<K, W, D, I> {

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
