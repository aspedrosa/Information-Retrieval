package indexer.persisters.document_identification;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Stores the document identification structure
 *  in a csv format
 */
public class CSV implements DocumentIdentificationBasePersister {

    /**
     * Stores the document identification structure
     *  *  in a csv format
     *
     * @param output to where the index will be written
     * @param documentIdentification of the indexer
     * @throws IOException if some error occurs while writing
     */
    @Override
    public void persist(OutputStream output, Map<Integer, String> documentIdentification) throws IOException {

        for (Map.Entry<Integer, String> entry : documentIdentification.entrySet()) {
            output.write(entry.getKey().toString().getBytes());
            output.write(',');
            output.write(entry.getValue().getBytes());

            output.write('\n');
        }
    }

}
