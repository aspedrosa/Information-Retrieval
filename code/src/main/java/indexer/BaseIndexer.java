package indexer;

import indexer.persisters.BasePersister;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public abstract class BaseIndexer<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    protected Map<T, List<D>> invertedIndex;

    public void persist(OutputStream output, BasePersister<T, D> persister) throws IOException {
        persister.persist(output, invertedIndex);
    }

    public abstract void addTerm(String term, String documentId);
}
