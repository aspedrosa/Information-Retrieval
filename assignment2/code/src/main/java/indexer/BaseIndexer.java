package indexer;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

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
public abstract class BaseIndexer<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    /**
     * The main data structure of the index.
     * Has the association between terms and the documents that
     *  contains it
     */
    protected Map<T, List<D>> invertedIndex;

    /**
     * Associates a document id to an identifier of a document
     */
    protected Map<Integer, String> documentRegistry;

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

    /**
     * Getter for the documentIdentification
     *
     * @return an unmodifiable version of the documentRegistry
     */
    public Map<Integer, String> getDocumentRegistry() {
        return Collections.unmodifiableMap(documentRegistry);
    }

    /**
     * Default constructor.
     * Uses a HashMap for the inverted index
     */
    public BaseIndexer() {
        invertedIndex = new HashMap<>();
        documentRegistry = new HashMap<>();
    }

    /**
     * Associates a documentId to a string identifier
     *
     * @param documentId of the document
     * @param identifier of the document (probably found on the content of the document)
     */
    public final void registerDocument(int documentId, String identifier) {
        documentRegistry.put(documentId, identifier);
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

    /**
     * Resets the indexer internal structures. Used mainly to get same memory back.
     */
    public void clear() {
        invertedIndex = new HashMap<>();
        documentRegistry = new HashMap<>();
    }

}
