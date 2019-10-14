package indexer.persisters;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface BasePersister<T extends Block&BaseTerm, D extends Block &BaseDocument> {

    void persist(OutputStream output, Map<T, List<D>> invertedIndex) throws IOException;

}
