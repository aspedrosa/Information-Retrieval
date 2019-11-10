package main.pipelines;

import indexer.BaseIndexer;
import indexer.post_indexing_actions.PostIndexingActions;
import indexer.io.persisters.BasePersister;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;
import parsers.corpus.CorpusReader;
import parsers.documents.Document;
import parsers.files.FileParser;
import tokenizer.BaseTokenizer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of a IR indexing pipeline.
 * It doesn't take memory into consideration and writes the index to a
 *  single file
 */
public class SimplePipeline<T extends Block & BaseTerm, D extends Block & BaseDocument> extends Pipeline<T, D> {

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
                          BaseIndexer<T, D> indexer,
                          CorpusReader corpusReader,
                          BasePersister<T, List<D>> finalIndexPersister,
                          BasePersister<Integer, String> docRegistryPersister) {
        super(tokenizer, indexer, corpusReader, finalIndexPersister, docRegistryPersister);
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

            int docId = document.getId();
            indexer.registerDocument(docId, document.getIdentifier());

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
        PostIndexingActions<T, D> postIndexingActions = indexer.getPostIndexingActions();
        if (indexer.getPostIndexingActions() != null) {
            for (Map.Entry<T, List<D>> entry : indexer.getInvertedIndex().entrySet()) {
                postIndexingActions.apply(entry.getKey(), entry.getValue());
            }
        }

        try {
            finalIndexPersister.persist(indexer.getInvertedIndex(), true);
            documentRegistryPersister.persist(indexer.getDocumentRegistry(), true);
        } catch (IOException e) {
            System.err.println("ERROR while writing the index to disk\n");
            e.printStackTrace();
            System.exit(2);
        }
    }

}
