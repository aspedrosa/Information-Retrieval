package io.data_containers.loaders.bulk_load;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DocRegBinaryBulkLoader extends BulkLoader<Integer, String> {

    public DocRegBinaryBulkLoader(String folder) {
        super(folder);
    }

    @Override
    public Map<Integer, String> load(String filename) throws IOException {
        ObjectInputStream input = new ObjectInputStream(
            new FileInputStream(
                folder + filename
            )
        );

        List<Map.Entry<Integer, String>> entries = new LinkedList<>();

        {Map.Entry<Integer, String> entry;
        while (true) {
            try {
                if ((entry = (Map.Entry<Integer, String>) input.readObject()) == null) {
                    break;
                }
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }

            entries.add(entry);
        }}

        Map<Integer, String> documentRegistry = new HashMap<>(entries.size());

        entries.forEach(entry -> {
            documentRegistry.put(entry.getKey(),entry.getValue());
        });

        input.close();

        return documentRegistry;
    }

}
