package main;

import indexer.BaseIndexer;
import indexer.persisters.BasePersister;
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

public class SimplePipeline<T extends Block&BaseTerm, D extends Block &BaseDocument> extends Pipeline<T, D> {

    public SimplePipeline(BaseTokenizer tokenizer,
                          BaseIndexer<T, D> indexer,
                          CorpusReader corpusReader,
                          String indexOutputFileName,
                          BasePersister<T, D> persister) {
        super(tokenizer, indexer, corpusReader, indexOutputFileName, persister);
    }

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
            System.err.println("ERROR error while opening file to write the index\n");
            System.exit(2);
        }

        try {
            indexer.persist(output, persister);

            output.close();
        } catch (IOException e) {
            System.err.println("ERROR while writing the index to disk\n");
            e.printStackTrace();
            System.exit(2);
        }
    }

}
