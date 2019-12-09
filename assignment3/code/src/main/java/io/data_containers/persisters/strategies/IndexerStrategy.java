package io.data_containers.persisters.strategies;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

public abstract class IndexerStrategy<
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>
    > extends OutputStreamStrategy<String, I> {

    /**
     * Main constructor
     *
     * @param separator to write between terms and documents
     */
    public IndexerStrategy(byte[] separator) {
        super(separator, "\n".getBytes());
    }

    @Override
    public byte[] handleKey(String key) {
        return key.getBytes();
    }

}
