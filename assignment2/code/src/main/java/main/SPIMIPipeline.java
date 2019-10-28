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

import java.util.Iterator;

public class SPIMIPipeline<T extends Block & BaseTerm, D extends Block & BaseDocument> extends Pipeline<T, D> {

    private float maxLoadFactor;

    public SPIMIPipeline(BaseTokenizer tokenizer,
                         BaseIndexer<T, D> indexer,
                         CorpusReader corpusReader,
                         String indexOutputFileName,
                         BasePersister<T, D> persister,
                         float maxLoadFactor) {
        super(tokenizer, indexer, corpusReader, indexOutputFileName, persister);
        this.maxLoadFactor = maxLoadFactor;
    }

    private boolean hasMemoryAvailable() {
        double freeMem = Runtime.getRuntime().freeMemory();
        double totalMem = Runtime.getRuntime().totalMemory();

        // calculate memory current load factor
        return (totalMem - freeMem) / totalMem >= maxLoadFactor;
    }

    @Override
    public void processFile(FileParser fileParser) {
        Iterator<Document> it = fileParser.iterator();
        while (it.hasNext()) {
            if (!hasMemoryAvailable()) {
                // TODO
                // write current index to disk
            }

            // TODO
            // index terms
        }
    }

    @Override
    public void persistIndex() {
        // TODO
        // merge blocks
    }

}
