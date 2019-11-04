package main.pipelines;

import indexer.BaseIndexer;
import indexer.persisters.indexer.BasePersister;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;
import parsers.corpus.CorpusReader;
import parsers.documents.Document;
import parsers.files.FileParser;
import tokenizer.BaseTokenizer;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Simple implementation of a IR indexing pipeline.
 * It doesn't take memory into consideration and writes the index to a
 *  single file
 */
public class SimplePipeline<T extends Block&BaseTerm, D extends Block &BaseDocument> extends Pipeline<T, D> {

    /**
     * Persister that defines the persisting strategy to store the
     *  indexer's structures to disk
     */
    private BasePersister<T, D> persister;

    /**
     * SimplePipeline main constructor
     *
     * @param tokenizer Parsers a document's content an splits it into tokens
     * @param indexer Associates a set o documents to a given term
     * @param corpusReader Class to retrieve the files present on the corpus folder
     * @param indexOutputFileName Name of the file to write the final index file
     * @param persister persising strategy
     */
    public SimplePipeline(BaseTokenizer tokenizer,
                          BaseIndexer<T, D> indexer,
                          CorpusReader corpusReader,
                          String indexOutputFileName,
                          BasePersister<T, D> persister) {
        super(tokenizer, indexer, corpusReader, indexOutputFileName);
        this.persister = persister;
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
        BufferedOutputStream output = null;
        try {
            output = new BufferedOutputStream(
                new FileOutputStream(
                    indexOutputFileName
                )
            );
        } catch (FileNotFoundException e) {
            System.err.println("ERROR while opening file to write the index\n");
            System.exit(2);
        }

        try {
            persister.persist(output, indexer.getInvertedIndex(), indexer.getDocumentRegistry());

            output.close();
        } catch (IOException e) {
            System.err.println("ERROR while writing the index to disk\n");
            e.printStackTrace();
            System.exit(2);
        }
    }

}
