package main;

import indexer.BaseIndexer;
import indexer.persisters.BasePersister;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;
import parsers.corpus.CorpusReader;
import parsers.files.FileParser;
import tokenizer.BaseTokenizer;

import java.io.IOException;

public abstract class Pipeline<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    protected BaseTokenizer tokenizer;

    protected BaseIndexer<T, D> indexer;

    protected CorpusReader corpusReader;

    protected String indexOutputFileName;

    protected BasePersister<T, D> persister;

    public Pipeline(BaseTokenizer tokenizer,
                    BaseIndexer<T, D> indexer,
                    CorpusReader corpusReader,
                    String indexOutputFileName,
                    BasePersister<T, D> persister) {
        this.tokenizer = tokenizer;
        this.indexer = indexer;
        this.corpusReader = corpusReader;
        this.indexOutputFileName = indexOutputFileName;
        this.persister = persister;
    }

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
    }

    public abstract void processFile(FileParser fileParser);

    public abstract void persistIndex();

}
