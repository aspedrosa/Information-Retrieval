package io.data_containers.loaders.lazy_load;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Specific loader that uses an ObjectInputStream to
 *  read from the input file
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 */
public class ObjectStreamLoader<K, V> extends LazyLoader<K, V> {

    /**
     * Uses an ObjectInputStream to read entries
     *
     * @param filename from where to read the entries
     * @return an iterator to read from a file with entries
     */
    @Override
    public Iterator<Map.Entry<K, V>> load(String filename) {
        ObjectInputStream tmp = null;
        try {
            tmp = new ObjectInputStream(
                new BufferedInputStream(
                    new FileInputStream(
                        filename
                    )
                )
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
        ObjectInputStream input = tmp;

        return new EntryIterator<Map.Entry<K, V>>() {

            @Override
            protected boolean readEntry() {
                try {
                    currentEntry = (Map.Entry<K, V>) input.readObject();

                    if (currentEntry == null) {
                        input.close();
                        closed = true;
                        return false;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("ERROR while reading object from file " + filename);
                    e.printStackTrace();
                    System.exit(2);
                }

                return true;
            }
        };
    }
}
