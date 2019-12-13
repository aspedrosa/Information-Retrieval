package io.data_containers.persisters.strategies;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Strategy to persist the document registry in text
 */
public class DocumentRegistryStrategy extends OutputStreamStrategy<Integer, Integer> {

    public DocumentRegistryStrategy() {
        super(new byte[0], "\n".getBytes());
    }

    @Override
    public byte[] handleKey(Integer key) {
        return new byte[0];
    }

    @Override
    public void handleValue(OutputStream output, Integer value) {
        byte[] identifierBytes = value.toString().getBytes();

        try {
            output.write(identifierBytes, 0, identifierBytes.length);
        } catch (IOException e) {
            System.err.println("ERROR while persisting");
            e.printStackTrace();
            System.exit(2);
        }
    }

}
