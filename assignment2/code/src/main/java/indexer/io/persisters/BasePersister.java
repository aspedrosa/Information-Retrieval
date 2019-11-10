package indexer.io.persisters;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Class in charge of persisting a set of entries. Before
 *  persisting them they are sorted, thus the K being descendent
 *  of Comparable. A number of entries per file can be defined,
 *  however all entries can be written to the same file.
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 */
public abstract class BasePersister<K extends Comparable, V> implements Closeable {

    /**
     * Prefix for the files created by the persister
     */
    protected String prefixFilename;

    /**
     * Count of entries currently written
     */
    protected int entriesCurrentCount;

    /**
     * Maximum of entries per file
     */
    protected int entriesLimitCount;

    /**
     * Counter to ensure the several files created
     *  have different names
     */
    protected int filesCounter;

    /**
     * Contains the Strings representing the
     *  keys that were used, in order, to create the filenames
     */
    protected List<String> firstKeys;

    /**
     * Main constructor
     *
     * @param prefixFilename prefix for the files created
     * @param entriesLimitCount maximum of entries per file
     */
    public BasePersister(String prefixFilename, int entriesLimitCount) {
        this.prefixFilename = prefixFilename;
        this.entriesLimitCount = entriesLimitCount;

        this.entriesCurrentCount = 0;
        this.filesCounter = 0;
        this.firstKeys = new ArrayList<>();
    }

    /**
     * Getter of the Strings used to create the output files
     *
     * @return firstKeys field
     */
    public List<String> getFirstKeys() {
        return firstKeys;
    }

    /**
     * Sorts the entries of a map by its key into a list to be persisted
     *
     * @param toPersist map with the entries to persist
     * @param isLast if its the last set of entries to write
     * @throws IOException if some error occurs while persisting the entries
     */
    public void persist(Map<K, V> toPersist, boolean isLast) throws IOException {
        List<Map.Entry<K, V>> sortedEntries = new ArrayList<>(toPersist.entrySet());
        sortedEntries.sort(Comparator.comparing(Map.Entry::getKey));

        persist(sortedEntries, isLast);
    }

    /**
     * Persists a list of sorted entries by keys.
     * If the output is null before persisting a new
     *  output is created.
     * If the maximum of entries per file is greater than 0
     *  whenever the count reaches the maximum, a new output
     *  is created/used
     *
     * @param sortedEntries entries to persist
     * @param isLast if its the last set of entries to write
     * @throws IOException if some error occurs while persisting the entries
     */
    public void persist(List<Map.Entry<K, V>> sortedEntries, boolean isLast) throws IOException {
        if (outputIsNull()) {
            createNewOutput(sortedEntries.get(0).getKey().toString());
        }

        for (int i = 0; i < sortedEntries.size(); i++) {
            if (entriesLimitCount > 0 && entriesCurrentCount >= entriesLimitCount) {
                close();

                createNewOutput(sortedEntries.get(i).getKey().toString());

                entriesCurrentCount = 0;
            }

            if (entriesLimitCount > 0)  {
                writeEntry(sortedEntries.get(i), (i == sortedEntries.size() - 1 && isLast) || ++entriesCurrentCount == entriesLimitCount);
            }
            else {
                writeEntry(sortedEntries.get(i), i == sortedEntries.size() - 1 && isLast);
            }
        }

        if (isLast) {
            close();
        }
    }

    /**
     * Checks if the output stream/writer is null
     *
     * @return true if the output is null, false otherwise
     */
    protected abstract boolean outputIsNull();

    /**
     * Creates a new output appending the to the
     *  prefix received on the constructor the current
     *  file counter and the key of the first entry
     *  that will be written to the new output
     *
     * @param firstKey string representation of the key of
     *  the first entry that will be written to the new output
     * @throws IOException if some error occurs while opening the new output
     */
    protected abstract void createNewOutput(String firstKey) throws IOException;

    /**
     * Descendent classes implement this method accordingly
     *  to the specific output type and format
     *
     * @param entry to write
     * @param lastEntry if its the last entry to write to the output
     * @throws IOException if some error occurs while writing an entry
     */
    protected abstract void writeEntry(Map.Entry<K, V> entry, boolean lastEntry) throws IOException;
}
