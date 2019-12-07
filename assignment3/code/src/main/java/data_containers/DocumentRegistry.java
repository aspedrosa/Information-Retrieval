package data_containers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DocumentRegistry {

    protected static int NUMBER_OF_DOCUMENTS = 0;

    /**
     * Associates a document id to an identifier of a document
     */
    protected Map<Integer, String> registry;

    public DocumentRegistry() {
        registry = new HashMap<>();
    }

    public static int getNumberOfDocuments() {
        return NUMBER_OF_DOCUMENTS;
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
     * Resets the document registry internal structures. Used mainly to get same memory back.
     */
    public void clear() {
        registry = new HashMap<>();
    }

}
