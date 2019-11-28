package indexer.io.persisters;

import indexer.io.loaders.BaseLoader;

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

    /**
     * Main constructor
     *
     * @param prefixFilename prefix for the files created
     * @param entriesLimitCount maximum of entries per file
     */
    public ObjectStreamPersister(String prefixFilename, int entriesLimitCount) {
        super(prefixFilename, entriesLimitCount);
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
        currentOutput.writeObject(new BaseLoader.Entry<>(entry.getKey(), entry.getValue()));
        currentOutput.reset();

        if (lastEntry) {
            currentOutput.writeObject(null);
        }
    }

    @Override
    public void close() throws IOException {
        if (currentOutput != null) {
            currentOutput.close();
            currentOutput = null;
        }
    }
}
