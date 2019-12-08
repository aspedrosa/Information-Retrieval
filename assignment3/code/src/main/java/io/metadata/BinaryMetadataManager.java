package io.metadata;

import data_containers.DocumentRegistry;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.TreeMap;

public class BinaryMetadataManager extends MetadataManager {

    public BinaryMetadataManager(String filename) {
        super(filename);
    }

    @Override
    public void persistMetadata(
        List<String> docRegFirstKeys,
        List<String> indexerFirstKeys
        ) throws IOException {

        ObjectOutputStream output = new ObjectOutputStream(
            new BufferedOutputStream(
                new FileOutputStream(filename)
            )
        );

        output.writeInt(DocumentRegistry.getNumberOfDocuments());

        TreeMap<Integer, String> docRegMetadata = new TreeMap<>();
        for (int i = 0; i < docRegFirstKeys.size(); i++) {
            docRegMetadata.put(
                Integer.parseInt(docRegFirstKeys.get(i)),
                i + ""
            );
        }
        output.writeObject(docRegMetadata);

        TreeMap<String, String> indexerMetadata = new TreeMap<>();
        for (int i = 0; i < indexerFirstKeys.size(); i++) {
            indexerMetadata.put(indexerFirstKeys.get(i), i + "");
        }
        output.writeObject(indexerMetadata);

        output.close();
    }

    @Override
    public void loadMetadata(
        TreeMap<Integer, String> docRegMetadata,
        TreeMap<String, String> indexerMetadata
    ) throws IOException {
        ObjectInputStream input = new ObjectInputStream(
            new FileInputStream(
                filename
            )
        );

        DocumentRegistry.setNumberOfDocuments(input.readInt());

        TreeMap<Integer, String> docRegMetadataTmp;
        TreeMap<String, String> indexerMetadataTmp;
        try {
            docRegMetadataTmp = (TreeMap<Integer, String>) input.readObject();
            indexerMetadataTmp = (TreeMap<String, String>) input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }

        input.close();

        docRegMetadata.putAll(docRegMetadataTmp);
        indexerMetadata.putAll(indexerMetadataTmp);
    }
}
