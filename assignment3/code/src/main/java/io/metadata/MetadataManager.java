package io.metadata;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

/**
 * Base class for metadata manager to load
 *  and persist metadata files
 */
public abstract class MetadataManager {

    protected String filename;

    public MetadataManager(String filename) {
        this.filename = filename;
    }

    public abstract void persistMetadata(
        List<String> docRegFirstKeys,
        List<String> indexerFirstKeys
    ) throws IOException;

    /**
     * Loads the metadata into the received structures.
     * Also sets the NUMBER_OF_DOCUMENTS variable of the DocumentRegistry class
     *
     * @param docRegMetadata a treemap reference to where the metadata
     *  of the document registries will be stored
     * @param indexerMetadata a treemap reference to where the metadata
     *  of the indexers will be stored
     * @throws IOException
     */
    public abstract void loadMetadata(
        TreeMap<Integer, String> docRegMetadata,
        TreeMap<String, String> indexerMetadata
    ) throws IOException;

}
