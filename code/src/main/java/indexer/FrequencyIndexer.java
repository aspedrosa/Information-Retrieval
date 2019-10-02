package indexer;

import indexer.structures.DocumentWithFrequency;
import indexer.structures.SimpleTerm;

import java.util.TreeMap;

public class FrequencyIndexer extends BaseIndexer<SimpleTerm, DocumentWithFrequency> {

    public FrequencyIndexer() {
        invertedIndex = new TreeMap<>();
    }

    /**
     * 1. Check if the inverted index contains the term
     *  1.If not
     *   1. Create a new posting list
     *   2. Insert the documentId and count (1) to the posting list
     *   3. Associate the posting list to the term
     *  2. else
     *   1. Get the existing posting list
     *   2. Search for the document
     *    1. If exists increment frequency by 1
     *    2. If not insert a new the documentId and count (1)
     *
     * Check difference between LinkedList or ArrayList
     */
    @Override
    public void addTerm(String term, String documentId) {
        // TODO
    }
}
