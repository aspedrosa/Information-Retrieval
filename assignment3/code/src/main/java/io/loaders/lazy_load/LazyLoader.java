package io.loaders.lazy_load;

import io.loaders.BaseLoader;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A class in charge of creating an iterator to load
 *  iteratively content from a file that was written
 *  by a persister.
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 */
public abstract class LazyLoader<K, V> implements BaseLoader<K, V> {

    /**
     * Descendent classes should implement this method
     *  and use the specific reader/stream
     *
     * @param filename from where to read the entries
     * @return an iterator to read from a file with entries
     */
    public abstract Iterator<Map.Entry<K, V>> load(String filename);

    /**
     * Iterator to read from a file with entries persisted
     *
     * @param <T> type of the entries
     */
    protected static abstract class EntryIterator<T> implements Iterator<T> {

        /**
         * To know if the file was/is closed
         */
        protected boolean closed = false;

        /**
         * The entry to return on the next
         *  next() call
         */
        protected T currentEntry;

        @Override
        public boolean hasNext() {
            if (closed) {
                return false;
            }
            else if (currentEntry != null) {
                return true;
            }

            return readEntry();
        }

        /**
         * Reads from the input and tries
         *  to create an entry, in case
         *  the input has data.
         *
         * @return true if was able to create
         *  an entry, false otherwise
         */
        protected abstract boolean readEntry();

        @Override
        public T next() {
            if (currentEntry == null) {
                throw new NoSuchElementException();
            }

            T tmp = currentEntry;

            currentEntry = null;

            return tmp;
        }
    }
}
