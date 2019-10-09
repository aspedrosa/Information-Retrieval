package indexer;

import indexer.persisters.BasePersister;
import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseIndexer<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    protected Map<T, List<D>> invertedIndex;

    protected Map<Integer, String> documentIdentification;

    public BaseIndexer() {
        documentIdentification = new HashMap<>();
    }

    public final void persist(OutputStream output, BasePersister<T, D> persister) throws IOException {
        persister.persist(output, invertedIndex);
    }

    public final void registerDocument(int documentId, String identifier) {
        documentIdentification.put(documentId, identifier);
    }

    public abstract void indexTerms(int documentId, List<String> terms);
}
