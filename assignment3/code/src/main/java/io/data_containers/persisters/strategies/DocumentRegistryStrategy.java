package io.data_containers.persisters.strategies;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Strategy to persist the document registry in text
 */
public class DocumentRegistryStrategy extends OutputStreamStrategy<Integer, String> {

    public DocumentRegistryStrategy() {
        super(",".getBytes(), "\n".getBytes());
    }

    @Override
    public byte[] handleKey(Integer key) {
        return key.toString().getBytes();
    }

    @Override
    public void handleValue(OutputStream output, String value) {
        byte[] identifierBytes = value.getBytes();

        try {
            output.write(identifierBytes, 0, identifierBytes.length);
        } catch (IOException e) {
            System.err.println("ERROR while persisting");
            e.printStackTrace();
            System.exit(2);
        }
    }
}
