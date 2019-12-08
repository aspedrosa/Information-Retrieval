package data_containers.indexer;

import data_containers.indexer.post_indexing_actions.PostIndexingActions;
import data_containers.indexer.structures.BaseDocument;
import data_containers.indexer.structures.BaseTerm;
import data_containers.indexer.structures.Block;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class of all indexers
 *
 * @param <T> Type of the terms
 * @param <D> Type of the documents
 */
public abstract class BaseIndexer<T extends Block & BaseTerm, D extends Block & BaseDocument>
    implements IndexerProvider<T, D> {

    /**
     * The main data structure of the index.
     * Has the association between terms and the documents that
     *  contains it
     */
    protected Map<T, List<D>> invertedIndex;

    protected PostIndexingActions<T, D> postIndexingActions;

    /**
     * Used to improve some performance. Since blocks' hashcode()
     *  and compareto() methods only take in account the key
     *  we can use the same object and change the key to get
     *  the posting list of a specific term
     */
    protected T dummyTerm;

    /**
     * Getter for the invertedIndex
     *
     * @return an unmodifiable version of the invertedIndex
     */
    public Map<T, List<D>> getInvertedIndex() {
        return Collections.unmodifiableMap(invertedIndex);
    }

    public PostIndexingActions<T, D> getPostIndexingActions() {
        return postIndexingActions;
    }

    /**
     * Default constructor.
     * Uses a HashMap for the inverted index
     */
    public BaseIndexer() {
        invertedIndex = new HashMap<>();
        postIndexingActions = null;
    }

    protected BaseIndexer(Map<T, List<D>> loadedIndex) {
        invertedIndex = loadedIndex;
        postIndexingActions = null;
    }

    /**
     * Constructor to define the a post indexing actions
     *  for the indexer
     *
     * @param postIndexingActions actions to apply after indexing
     *  and before persisting
     */
    public BaseIndexer(PostIndexingActions<T, D> postIndexingActions) {
        this.invertedIndex = new HashMap<>();
        this.postIndexingActions = postIndexingActions;
    }

    public BaseIndexer(PostIndexingActions<T, D> postIndexingActions, Map<T, List<D>> loadedIndex) {
        this.invertedIndex = loadedIndex;
        this.postIndexingActions = postIndexingActions;
    }

    /**
     * Function called, after the content of a documents goes through
     *  the tokenizer, to index the term.
     *
     * @param documentId to which the terms where extracted
     * @param terms sequence of terms to index
     */
    public void indexTerms(int documentId, List<String> terms) {
        Map<String, Integer> frequencies = new HashMap<>();

        for (String term : terms) {
            if (!frequencies.containsKey(term)) {
                frequencies.put(term, 0);
            }

            int currentCount = frequencies.get(term);

            frequencies.put(term, currentCount + 1);
        }

        // insert the last parsed document
        insertDocument(documentId, frequencies);
    }

    /**
     *  Inserts BaseDocument classes on the respective
     *   posting lists of every term in the frequencies map.
     *  Method called after calculating the frequencies for each term
     *   for the document with the document id.
     * @param documentId id of the document to index
     * @param frequencies frequencies for each term present on the document
     */
    protected abstract void insertDocument(int documentId, Map<String, Integer> frequencies);

    public List<D> getPostingList(String term) {
        dummyTerm.setTerm(term);

        return invertedIndex.get(dummyTerm);
    }

    /**
     * Resets the indexer internal structures. Used mainly to get same memory back.
     */
    public void clear() {
        invertedIndex = new HashMap<>();
    }

}
