package indexer.persisters.indexer;

import indexer.persisters.PostIndexingActions;
import indexer.persisters.document_registry.DocumentRegistryBasePersister;
import indexer.persisters.inverted_index.InvertedIndexBasePersister;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Persists first the inverted index then the document registry
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public class IndexThenDocRegist<T extends Block&BaseTerm, D extends Block &BaseDocument> implements BasePersister<T, D> {

    /**
     * Strategy to store the inverted index
     */
    private InvertedIndexBasePersister<T, D> invertedIndexPersister;

    /**
     * Separator between the two structures
     */
    private byte[] separator;

    /**
     * Strategy to store the document registry
     */
    private DocumentRegistryBasePersister documentRegistryPersister;

    /**
     * Main constructor
     *
     * @param invertedIndexPersister Strategy to store the inverted index
     * @param documentRegistryPersister Strategy to store the document registry
     * @param separator Separator between the two structures
     */
    public IndexThenDocRegist(
        InvertedIndexBasePersister<T, D> invertedIndexPersister,
        DocumentRegistryBasePersister documentRegistryPersister,
        String separator
    ) {
        this.invertedIndexPersister = invertedIndexPersister;
        this.documentRegistryPersister = documentRegistryPersister;
        this.separator = separator.getBytes();
    }

    /**
     * Implements the strategy to store the two internal structures
     *
     * @param output the stream to write the strucutres
     * @param invertedIndex of the indexer
     * @param postIndexingActions actions to apply to the inverted index before persisting
     * @param documentRegistry of the indexer
     * @throws IOException if some error occurs while writing
     */
    @Override
    public void persist(OutputStream output, Map<T, List<D>> invertedIndex, PostIndexingActions<T, D> postIndexingActions, Map<Integer, String> documentRegistry) throws IOException {
        invertedIndexPersister.persist(output, invertedIndex, postIndexingActions);

        output.write(separator, 0, separator.length);

        documentRegistryPersister.persist(output, documentRegistry);
    }

}
