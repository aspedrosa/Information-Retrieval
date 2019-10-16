package indexer.persisters;

import indexer.persisters.document_identification.DocumentIdentificationBasePersister;
import indexer.persisters.inverted_index.InvertedIndexBasePersister;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Persists first the inverted index tha the document identification
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public class IndexThenDocIdent<T extends Block&BaseTerm, D extends Block &BaseDocument> implements BasePersister<T, D> {

    /**
     * Strategy to store the inverted index
     */
    private InvertedIndexBasePersister<T, D> invertedIndexPersister;

    /**
     * Separator between the two structures
     */
    private String separator;

    /**
     * Strategy to store the document identification
     */
    private DocumentIdentificationBasePersister documentIdentificationPersister;

    /**
     * Main constructor
     *
     * @param invertedIndexPersister Strategy to store the inverted index
     * @param documentIdentificationPersister Strategy to store the document identification
     * @param separator Separator between the two structures
     */
    public IndexThenDocIdent(
        InvertedIndexBasePersister<T, D> invertedIndexPersister,
        DocumentIdentificationBasePersister documentIdentificationPersister,
        String separator
    ) {
        this.invertedIndexPersister = invertedIndexPersister;
        this.documentIdentificationPersister = documentIdentificationPersister;
        this.separator = separator;
    }

    /**
     * Implements the strategy to store the two internal structures
     *
     * @param output the stream to write the strucutres
     * @param invertedIndex of the indexer
     * @param documentIdentification of the indexer
     * @throws IOException if some error occurs while writing
     */
    @Override
    public void persist(OutputStream output, Map<T, List<D>> invertedIndex, Map<Integer, String> documentIdentification) throws IOException {
        invertedIndexPersister.persist(output, invertedIndex);

        output.write(separator.getBytes());

        documentIdentificationPersister.persist(output, documentIdentification);
    }

}
