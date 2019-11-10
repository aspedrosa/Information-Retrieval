package indexer.io.loaders;

import indexer.io.loaders.strategies.ReaderStrategy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;

/**
 * Specific loader that uses a BufferedReader to
 *  read from the input file
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 */
public class ReaderLoader<K, V> extends BaseLoader<K, V> {

    /**
     * Strategy to transform text into an entry
     */
    private ReaderStrategy strategy;

    /**
     * Main constructor
     *
     * @param strategy to create entries from text
     */
    public ReaderLoader(ReaderStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Uses a BufferedReader to read entries
     *
     * @param filename from where to read the entries
     * @return an iterator to read from a file with entries
     */
    @Override
    public Iterator<Map.Entry<K, V>> load(String filename) {
        BufferedReader tmp = null;
        try {
            tmp = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(filename)
                )
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader input = tmp;

        return new EntryIterator<Map.Entry<K, V>>() {

            @Override
            protected boolean readEntry() {
                String line = null;
                try {
                    line = input.readLine();

                    if (line == null) {
                        input.close();
                        closed = true;
                        return false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                strategy.createEntry(line);

                return true;
            }

        };
    }

}
