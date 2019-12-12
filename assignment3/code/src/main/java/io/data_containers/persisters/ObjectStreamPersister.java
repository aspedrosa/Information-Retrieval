package io.data_containers.persisters;

import io.data_containers.loaders.lazy_load.LazyLoader;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Persists to the entries using an object output stream
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 */
public class ObjectStreamPersister<K extends Comparable, V> extends BasePersister<K, V> {

    /**
     * Stream currently open to write entries
     */
    private ObjectOutputStream currentOutput;

    private boolean wroteNull;

    public ObjectStreamPersister(String outputFolder, int limitFileSize) {
        super(outputFolder, limitFileSize);
        wroteNull = false;
    }

    @Override
    protected boolean outputIsNull() {
        return currentOutput == null;
    }

    @Override
    protected void createNewOutput(String newFilename) throws IOException {
        currentOutput = new ObjectOutputStream(
            new BufferedOutputStream(
                new FileOutputStream(
                    newFilename
                )
            )
        );

        wroteNull = false;
    }

    /**
     * Writes an entry as an entry object. If its the last entry
     *  writes a null object, so the loader knows the end of the file.
     *
     * @param entry to write
     * @param lastEntry if its the last entry to write to the output
     * @throws IOException if some error occurs while writing
     */
    @Override
    protected void writeEntry(Map.Entry<K, V> entry, boolean lastEntry) throws IOException {
        currentOutput.writeObject(new LazyLoader.Entry<>(entry.getKey(), entry.getValue()));
        currentOutput.reset();

        if (lastEntry) {
            currentOutput.writeObject(null);
            wroteNull = true;
        }
    }

    @Override
    public void close() throws IOException {
        if (currentOutput != null) {
            if (!wroteNull) {
                currentOutput.writeObject(null);
            }
            currentOutput.close();
            currentOutput = null;
        }
    }
}
