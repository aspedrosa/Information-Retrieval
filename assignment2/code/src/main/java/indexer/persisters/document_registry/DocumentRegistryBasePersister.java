package indexer.persisters.document_registry;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * All persisting strategies for the document identification
 *  structure must implement this interface
 */
public interface DocumentRegistryBasePersister {

    /**
     * Method called to by the a BaseIndexer to write the document
     *  identification structure to the output stream
     *
     * @param output to where the index will be written
     * @param documentRegistry of the indexer
     * @throws IOException if some error occurs while writing the index to the stream
     */
    void persist(OutputStream output, Map<Integer, String> documentRegistry) throws IOException;

}
