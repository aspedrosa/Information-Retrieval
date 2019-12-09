package io.data_containers.persisters.strategies;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Output strategy to format the entries of a frequency indexer
 * term,doc1:freq,doc2:freq,doc3:freq
 */
public class FrequencyStrategy extends IndexerStrategy<
    Integer,
    Document<Integer>,
    TermInfoBase<Integer, Document<Integer>>> {

    /**
     * Main constructor
     */
    public FrequencyStrategy() {
        super(",".getBytes());
    }

    @Override
    public void handleValue(OutputStream output, TermInfoBase<Integer, Document<Integer>> value) {
        List<Document<Integer>> postingList = value.getPostingList();
        for (int i = 0; i < postingList.size(); i++) {
            Document<Integer> document = postingList.get(i);
            byte[] docBytes = (document.getDocId() + ":" + document.getWeight()).getBytes();

            try {
                output.write(docBytes, 0, docBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(2);
            }

            if (i < postingList.size() - 1) {
                try {
                    output.write(keyValueSeparator, 0, keyValueSeparator.length);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(2);
                }
            }
        }
    }
}
