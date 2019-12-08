package io.metadata;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

public abstract class MetadataManager {

    protected String filename;

    public MetadataManager(String filename) {
        this.filename = filename;
    }

    public abstract void persistMetadata(
        List<String> docRegFirstKeys,
        List<String> indexerFirstKeys
    ) throws IOException;

    public abstract void loadMetadata(
        TreeMap<Integer, String> docRegMetadata,
        TreeMap<String, String> indexerMetadata
    ) throws IOException;

}
