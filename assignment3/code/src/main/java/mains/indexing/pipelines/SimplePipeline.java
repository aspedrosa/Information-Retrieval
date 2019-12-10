package mains.indexing.pipelines;

import data_containers.indexer.BaseIndexer;
import data_containers.indexer.post_indexing_actions.PostIndexingActions;
import data_containers.indexer.structures.TermInfoBase;
import io.metadata.MetadataManager;
import io.data_containers.persisters.BasePersister;
import parsers.corpus.CorpusReader;
import parsers.documents.Document;
import parsers.files.FileParser;
import tokenizer.BaseTokenizer;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of a IR indexing pipeline.
 * It doesn't take memory into consideration and writes the index to a
 *  single file
 */
public class SimplePipeline<
    T extends Comparable<T> & Serializable,
    W extends Number,
    D extends data_containers.indexer.structures.Document<W>,
    I extends TermInfoBase<W, D>
    > extends Pipeline<T, W, D, I> {

    /**
     * SimplePipeline main constructor
     *
     * @param tokenizer Parsers a document's content an splits it into tokens
     * @param indexer Associates a set o documents to a given term
     * @param corpusReader Class to retrieve the files present on the corpus folder
     * @param finalIndexPersister in charge of writing to disk the inverted index
     * @param docRegistryPersister in charge of writing to disk the
     *  document registry structure
     */
    public SimplePipeline(BaseTokenizer tokenizer,
                          BaseIndexer<T, W, D, I> indexer,
                          CorpusReader corpusReader,
                          BasePersister<T, I> finalIndexPersister,
                          BasePersister<Integer, String> docRegistryPersister,
                          MetadataManager metadataManager) {
        super(tokenizer, indexer, corpusReader, finalIndexPersister, docRegistryPersister, metadataManager);
    }

    /**
     * Processes a file. Iterates over the documents present on a file
     *  and:
     *  <ul>
     *      <li>1. Tokenize the necessary fields</li>
     *      <li>2. Register the document to the document registry</li>
     *      <li>3. Index the terms present on the document</li>
     *  </ul>
     *
     * @param fileParser the file to parse
     */
    @Override
    public void processFile(FileParser fileParser) {
        for (Document document : fileParser) {
            List<String> terms = tokenizer.tokenizeDocument(document.getToTokenize());

            int docId = documentRegistry.registerDocument(document.getIdentifier());

            if (!terms.isEmpty()) {
                indexer.indexTerms(docId, terms);
            }
        }
    }

    /**
     * Function called after iterating over the entire corpus.
     * Persists to disk the inverted index and the necessary
     *  structures.
     */
    @Override
    public void persistIndex() {
        PostIndexingActions<W, D, I> postIndexingActions = indexer.getPostIndexingActions();
        if (indexer.getPostIndexingActions() != null) {
            for (Map.Entry<T, I> entry : indexer.getInvertedIndex().entrySet()) {
                postIndexingActions.apply(entry.getValue());
            }
        }

        try {
            finalIndexPersister.persist(indexer.getInvertedIndex(), true);
            documentRegistryPersister.persist(documentRegistry.getRegistry(), true);
        } catch (IOException e) {
            System.err.println("ERROR while writing the index to disk\n");
            e.printStackTrace();
            System.exit(2);
        }
    }

}
