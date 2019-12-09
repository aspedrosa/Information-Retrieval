package io.data_containers.persisters.strategies;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoWithIDF;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * TODO
 * term:idf;...;...;...
 */
public abstract class WeightStrategyBase<W extends Number, D extends Document<W>> extends IndexerStrategy<W, D, TermInfoWithIDF<W, D>> {

    private byte[] separator = ";".getBytes();

    protected int precision;

    public WeightStrategyBase() {
        super("".getBytes());
        this.precision = 3;
    }

    public WeightStrategyBase(int precision) {
        super("".getBytes());
        this.precision = precision;
    }

    @Override
    public void handleValue(OutputStream output, TermInfoWithIDF<W, D> value) {
        {byte[] idfBytes = String.format(":%." + precision + "f", value.getIdf()).getBytes();

        try {
            output.write(idfBytes, 0, idfBytes.length);
            output.write(separator, 0, separator.length);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }}

        List<D> postingList = value.getPostingList();
        for (int i = 0; i < postingList.size(); i++) {
            byte[] docBytes = handleDocument(postingList.get(i));

            try {
                output.write(docBytes, 0, docBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(2);
            }

            if (i < postingList.size() - 1) {
                try {
                    output.write(separator, 0, separator.length);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(2);
                }
            }
        }
    }

    public abstract byte[] handleDocument(D document);

}
