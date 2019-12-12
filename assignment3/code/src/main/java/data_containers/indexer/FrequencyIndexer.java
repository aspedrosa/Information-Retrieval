package data_containers.indexer;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

import java.util.Map;

/**
 * Specific type of indexer that on the list of documents
 *  stores the number of times a specific term appeared for
 *  a given document
 */
public class FrequencyIndexer extends BaseIndexer<
    String,
    Integer,
    Document<Integer>,
    TermInfoBase<Integer, Document<Integer>>> {

    /**
     *  Inserts BaseDocument classes on the respective
     *   posting lists of every term in the frequencies map.
     * @param documentId id of the document to index
     * @param frequencies frequencies for each term present on the document
     */
    protected void insertDocument(int documentId, Map<String, Integer> frequencies) {
        frequencies.forEach((term, count) -> {
            TermInfoBase<Integer, Document<Integer>> termInfo = invertedIndex.get(term);

            if (termInfo == null) {
                termInfo = new TermInfoBase<>();
                invertedIndex.put(term, termInfo);
            }

            termInfo.addToPostingList(new Document<>(documentId, count));
        });

    }

}
