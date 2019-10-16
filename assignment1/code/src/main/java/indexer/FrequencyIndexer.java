package indexer;

import indexer.structures.DocumentWithFrequency;
import indexer.structures.SimpleTerm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Specific type of indexer that on the list of documents
 *  stores the number of times a specific term appeared for
 *  a given document
 */
public class FrequencyIndexer extends BaseIndexer<SimpleTerm, DocumentWithFrequency> {

    /**
     * Used to improve some performance. Since blocks' hashcode()
     *  and compareto() methods only take in account the key
     *  we can use the same object and change the key to get
     *  the posting list of a specific term
     */
    private SimpleTerm dummyTerm;

    /**
     * Default constructor.
     */
    public FrequencyIndexer() {
        super();
        dummyTerm = new SimpleTerm(null);
    }

    /**
     * Allows the user to choose the implementation class to be
     * used by the inverted index
     *
     * @param invertedIndex implementation of the map interface
     */
    public FrequencyIndexer(Map<SimpleTerm, List<DocumentWithFrequency>> invertedIndex) {
        super(invertedIndex);
        dummyTerm = new SimpleTerm(null);
    }

    /**
     * Used to insert a document to the posting list of a term
     *  using the dummy term.
     * If the term is not present on the inverted index
     *  created a posting list and inserts it
     *
     * @param term to which posting list will be added the document received
     * @param document to be added
     */
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
     * Indexes a set of terms of the same document
     *
     * @param documentId to which the terms where extracted
     * @param terms sequence of terms to index
     */
    @Override
    public void indexTerms(int documentId, List<String> terms) {
        // sort so we only need to do one iteration over the terms when indexing
        terms.sort(String::compareTo);

        String previousTerm = null;
        DocumentWithFrequency document = null;
        for (String term : terms) {
            if (term.equals(previousTerm)) {
                document.increaseFrequency();
            }
            else {
                if (previousTerm != null) {
                    insertDocument(previousTerm, document);
                }

                previousTerm = term;
                document = new DocumentWithFrequency(documentId, 1);
            }
        }

        // insert the last parsed document
        insertDocument(previousTerm, document);
    }
}
