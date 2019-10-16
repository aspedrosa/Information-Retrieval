package indexer;

import indexer.persisters.BasePersister;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
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
     * Object that holds
     */
    protected Map<T, List<D>> invertedIndex;

    protected Map<Integer, String> documentIdentification;

    /**
     * Default constructor.
     * Uses a HashMap for the inverted index
     */
    public BaseIndexer() {
        invertedIndex = new HashMap<>();
        documentIdentification = new HashMap<>();
    }

    /**
     * Alternative constructor.
     * Allows the user to choose the implementation of
     *  the interface Map to be used
     *
     * @param invertedIndex a Map implementation
     */
    public BaseIndexer(Map<T, List<D>> invertedIndex) {
        this.invertedIndex = invertedIndex;
        this.documentIdentification = new HashMap<>();
    }

    /**
     * Method called to save the index
     *
     * @param output Stream to write the index
     * @param persister class that handles how to write the index to the stream (Persisting Strategy).
     * @throws IOException if some error occurs while writing the
     *  index to the stream
     */
    public final void persist(OutputStream output, BasePersister<T, D> persister) throws IOException {
        persister.persist(output, invertedIndex);
    }

    /**
     * Associates a documentId to a string identifier
     *
     * @param documentId of the document
     * @param identifier of the document (probably found on the content of the document)
     */
    public final void registerDocument(int documentId, String identifier) {
        documentIdentification.put(documentId, identifier);
    }

    /**
     * Function called, after the content of a documents goes through
     *  the tokenizer, to index the term.
     *
     * @param documentId to which the terms where extracted
     * @param terms sequence of terms to index
     */
    public abstract void indexTerms(int documentId, List<String> terms);
}
