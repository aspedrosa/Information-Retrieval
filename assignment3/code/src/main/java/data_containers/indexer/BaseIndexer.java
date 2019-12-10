package data_containers.indexer;

import data_containers.indexer.post_indexing_actions.PostIndexingActions;
import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoBase;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class of all indexers
 *
 * @param <T> type of the terms
 * @param <W> type of the document weight
 * @param <D> type of the documents
 * @param <I> type of info related to the term
 */
public abstract class BaseIndexer<
    T extends Comparable<T> & Serializable,
    W extends Number,
    D extends Document<W>,
    I extends TermInfoBase<W, D>
    >
    implements IndexerProvider<T, W, D, I> {

    /**
     * The main data structure of the index.
     * Has the association between terms and the documents that
     *  contains it
     */
    protected Map<T, I> invertedIndex;

    /**
     * Actions to apply before persisting the index to disk
     *  during indexing
     */
    protected PostIndexingActions<W, D, I> postIndexingActions;

    /**
     * Getter for the invertedIndex
     *
     * @return an unmodifiable version of the invertedIndex
     */
    public Map<T, I> getInvertedIndex() {
        return Collections.unmodifiableMap(invertedIndex);
    }

    /**
     * Getter for the post indexing actions field
     */
    public PostIndexingActions<W, D, I> getPostIndexingActions() {
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

    /**
     * Constructor used to create a new indexer after loading from disk
     */
    protected BaseIndexer(Map<T, I> loadedIndex) {
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
    public BaseIndexer(PostIndexingActions<W, D, I> postIndexingActions) {
        this.invertedIndex = new HashMap<>();
        this.postIndexingActions = postIndexingActions;
    }

    /**
     * Constructor used to create a new indexer after loading from disk
     *  having a post indexing actions associated
     */
    public BaseIndexer(PostIndexingActions<W, D, I> postIndexingActions, Map<T, I> loadedIndex) {
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

    public I getTermInfo(T term) {
        return invertedIndex.get(term);
    }

    /**
     * Resets the indexer internal structures. Used mainly to get same memory back.
     */
    public void clear() {
        invertedIndex = new HashMap<>();
    }

}
