package mains.indexing.pipelines;

import data_containers.DocumentRegistry;
import data_containers.indexer.BaseIndexer;
import io.metadata.MetadataManager;
import io.data_containers.persisters.BasePersister;
import data_containers.indexer.structures.BaseDocument;
import data_containers.indexer.structures.BaseTerm;
import data_containers.indexer.structures.Block;
import parsers.corpus.CorpusReader;
import parsers.files.FileParser;
import tokenizer.BaseTokenizer;

import java.io.IOException;
import java.util.List;

/**
 * Base class of all pipelines. On this class its defined the
 * steps of the indexing section of a IR system, which are:
 *
 * <ul>
 *     <li>1. Iterate over the corpus folder</li>
 *     <li>2. Index the content of each document on the several files</li>
 *     <li>3. Persist the final index and its auxiliary structures (document registry)</li>
 * </ul>
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public abstract class Pipeline<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    /**
     * Parsers a document's content an splits it into tokens
     */
    protected BaseTokenizer tokenizer;

    /**
     * The principal class of the IR system. Associates
     *  a set o documents to a given term
     */
    protected BaseIndexer<T, D> indexer;

    protected DocumentRegistry documentRegistry;

    /**
     * Class to retrieve the files present
     *  on the corpus folder
     */
    protected CorpusReader corpusReader;

    /**
     * Class to persist the inverted index to disk
     */
    protected BasePersister<T, List<D>> finalIndexPersister;

    /**
     * Class to persist the document registry structure
     *  to disk
     */
    protected BasePersister<Integer, String> documentRegistryPersister;

    private MetadataManager metadataManager;

    /**
     * Main constructor.
     *
     * @param tokenizer Parsers a document's content an splits it into tokens
     * @param indexer Associates a set o documents to a given term
     * @param corpusReader Class to retrieve the files present on the corpus folder
     * @param finalIndexPersister in charge of writing to disk the inverted index
     * @param docRegistryPersister in charge of writing to disk the
     *  document registry structure
     */
    public Pipeline(BaseTokenizer tokenizer,
                    BaseIndexer<T, D> indexer,
                    CorpusReader corpusReader,
                    BasePersister<T, List<D>> finalIndexPersister,
                    BasePersister<Integer, String> docRegistryPersister,
                    MetadataManager metadataManager) {
        this.tokenizer = tokenizer;
        this.indexer = indexer;
        this.corpusReader = corpusReader;
        this.finalIndexPersister = finalIndexPersister;
        this.documentRegistryPersister = docRegistryPersister;
        this.metadataManager = metadataManager;

        this.documentRegistry = new DocumentRegistry();
    }

    /**
     * Executes the pipeline
     */
    public final void execute() {
        System.out.println("Started parsing the corpus");
        long begin = System.currentTimeMillis();

        for (FileParser fileParser : corpusReader) {

            processFile(fileParser);

            try {
                fileParser.close();
            } catch (IOException e) {
                System.err.println("ERROR closing file " + fileParser.getFilename() + "\n");
                e.printStackTrace();
            }
        }

        System.out.println("Finished parsing the corpus in " + (System.currentTimeMillis() - begin));
        System.out.println("Started storing index to disk");
        begin = System.currentTimeMillis();

        persistIndex();

        System.out.println("Finished storing index to disk in " + (System.currentTimeMillis() - begin));
        System.out.println("Started creating metadata file");

        // create metadata file
        try {
            metadataManager.persistMetadata(
                documentRegistryPersister.getFirstKeys(),
                finalIndexPersister.getFirstKeys()
            );
        } catch (IOException e) {
            System.err.println("ERROR while persisting metadata file");
            e.printStackTrace();
            System.exit(2);
        }

        System.out.println("Finished creating metadata file");
    }

    /**
     * Processes a file. Should Iterate over the documents present on a file
     *  and:
     *  <ul>
     *      <li>1. Tokenize the necessary fields</li>
     *      <li>2. Register the document to the document registry</li>
     *      <li>3. Index the terms present on the document</li>
     *  </ul>
     *
     * @param fileParser the file to parse
     */
    public abstract void processFile(FileParser fileParser);

    /**
     * Function called after iterating over the entire corpus.
     * Should persist to disk the inverted index and the necessary
     *  structures.
     */
    public abstract void persistIndex();

    public void createMetadataFile() {
    }

}
