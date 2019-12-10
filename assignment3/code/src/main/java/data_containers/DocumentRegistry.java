package data_containers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores the association between the internal
 *  document id and the document identifier
 */
public class DocumentRegistry {

    /**
     * Number of documents present on the corpus
     */
    protected static int NUMBER_OF_DOCUMENTS = 0;

    /**
     * Associates a document id to an identifier of a document
     */
    protected Map<Integer, String> registry;

    /**
     * Constructor for indexing execution
     */
    public DocumentRegistry() {
        registry = new HashMap<>();
    }

    /**
     * Constructor for search execution
     *
     * @param registry loaded registry
     */
    public DocumentRegistry(Map<Integer, String> registry) {
        this.registry = registry;
    }

    /**
     * Getter for the number of the documents in the corpus
     */
    public static int getNumberOfDocuments() {
        return NUMBER_OF_DOCUMENTS;
    }

    /**
     * Setter for the number of the documents. Used during the search execution
     */
    public static void setNumberOfDocuments(int newNumberOfDocuments) {
        NUMBER_OF_DOCUMENTS = newNumberOfDocuments;
    }

    /**
     * Getter for the documentIdentification
     *
     * @return an unmodifiable version of the documentRegistry
     */
    public Map<Integer, String> getRegistry() {
        return Collections.unmodifiableMap(registry);
    }

    /**
     * Associates a documentId to a string identifier
     *
     * @param identifier of the document (probably found on the content of the document)
     */
    public int registerDocument(String identifier) {
        int docId = ++NUMBER_OF_DOCUMENTS;

        registry.put(docId, identifier);

        return docId;
    }

    /**
     * Gets the translations of the internal document id
     *  to the respective document identifier
     */
    public String translateDocId(int docId) {
        return registry.get(docId);
    }

    /**
     * Resets the document registry internal structures. Used mainly to get same memory back.
     */
    public void clear() {
        registry = new HashMap<>();
    }

}
