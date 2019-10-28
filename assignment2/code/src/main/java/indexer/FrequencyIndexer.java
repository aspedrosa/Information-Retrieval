package indexer;

import indexer.structures.DocumentWithInfo;
import indexer.structures.SimpleTerm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Specific type of indexer that on the list of documents
 *  stores the number of times a specific term appeared for
 *  a given document
 */
public class FrequencyIndexer extends BaseIndexer<SimpleTerm, DocumentWithInfo<Integer>> {

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
    public FrequencyIndexer(Map<SimpleTerm, List<DocumentWithInfo<Integer>>> invertedIndex) {
        super(invertedIndex);
        dummyTerm = new SimpleTerm(null);
    }

    protected void insertDocument(int documentId, Map<String, Integer> frequencies) {
        frequencies.forEach((term, count) -> {
            dummyTerm.setTerm(term);

            List<DocumentWithInfo<Integer>> postingList = invertedIndex.get(dummyTerm);

            if (postingList == null) {
                postingList = new LinkedList<>();
                invertedIndex.put(new SimpleTerm(term), postingList);
            }

            postingList.add(new DocumentWithInfo<>(documentId, count));
        });

    }

}
