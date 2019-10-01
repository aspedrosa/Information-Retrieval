package indexer;

import indexer.persisters.BasePersister;

import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.List;

public class PostingIndexer implements BaseIndexer {

    private Map<Term<?>, List<Document<Integer>>> invertedIndex;

    public PostingIndexer() {
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
     */
    @Override
    public void addTerm(String term, String documentId) {
        // TODO
    }

    public void persist(BasePersister persister, String fileName) {
        for (Map.Entry<Term<?>, List<Document<Integer>>> a : invertedIndex.entrySet()) {
            Term b = a.getKey();
            List<Document<Integer>> d = a.getValue();
        }
    }
}
