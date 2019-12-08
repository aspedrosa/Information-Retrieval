package data_containers.indexer;

import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.SimpleTerm;

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
        dummyTerm = new SimpleTerm();
    }

    public FrequencyIndexer(Map<SimpleTerm, List<DocumentWithInfo<Integer>>> loadedIndex) {
        super(loadedIndex);
        dummyTerm = new SimpleTerm();
    }

    @Override
    public BaseIndexer<SimpleTerm, DocumentWithInfo<Integer>> createIndexer(
        Map<SimpleTerm, List<DocumentWithInfo<Integer>>> loadedIndex) {
        return new FrequencyIndexer(loadedIndex);
    }

    /**
     *  Inserts BaseDocument classes on the respective
     *   posting lists of every term in the frequencies map.
     * @param documentId id of the document to index
     * @param frequencies frequencies for each term present on the document
     */
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
