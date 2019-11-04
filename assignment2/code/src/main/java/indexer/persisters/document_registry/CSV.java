package indexer.persisters.document_registry;

import indexer.persisters.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Stores the document identification structure
 *  in a csv format
 */
public class CSV implements DocumentRegistryBasePersister {

    /**
     * Stores the document identification structure
     *  *  in a csv format
     *
     * @param output to where the index will be written
     * @param documentRegistry of the indexer
     * @throws IOException if some error occurs while writing
     */
    @Override
    public void persist(OutputStream output, Map<Integer, String> documentRegistry) throws IOException {
        Iterator<Map.Entry<Integer, String>> it = documentRegistry.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, String> entry = it.next();

            byte[] documentIdBytes = entry.getKey().toString().getBytes();
            output.write(documentIdBytes, 0, documentIdBytes.length);
            output.write(Constants.COMMA, 0, 1);

            byte[] identifierBytes = entry.getValue().getBytes();
            output.write(identifierBytes, 0, identifierBytes.length);

            if (it.hasNext()) {
                output.write(Constants.NEWLINE, 0, 1);
            }
        }
    }

}
