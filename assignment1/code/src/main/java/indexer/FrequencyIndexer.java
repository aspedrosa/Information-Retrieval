package indexer;

import indexer.structures.DocumentWithFrequency;
import indexer.structures.SimpleTerm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FrequencyIndexer extends BaseIndexer<SimpleTerm, DocumentWithFrequency> {

    // use to improve some performance
    private SimpleTerm dummyTerm;

    public FrequencyIndexer() {
        invertedIndex = new HashMap<>(); //try treeMap
        dummyTerm = new SimpleTerm(null);
    }

    private void insertDocument(String term, DocumentWithFrequency document) {
        dummyTerm.setTerm(term);

        List<DocumentWithFrequency> postingList = invertedIndex.get(dummyTerm);

        if (postingList == null) {
            postingList = new LinkedList<>();
            invertedIndex.put(new SimpleTerm(term), postingList);
        }

        postingList.add(document);
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
    public void indexTerms(int documentId, List<String> terms) {
        terms.sort(String::compareTo);

        String previousTerm = null;
        DocumentWithFrequency document = null;
        for (String term : terms) {
            if (term.equals(previousTerm)) {
                document.increseFrequency();
            }
            else {
                if (previousTerm != null) {
                    insertDocument(previousTerm, document);
                }

                previousTerm = term;
                document = new DocumentWithFrequency(documentId, 1);
            }
        }

        insertDocument(previousTerm, document);
    }
}
