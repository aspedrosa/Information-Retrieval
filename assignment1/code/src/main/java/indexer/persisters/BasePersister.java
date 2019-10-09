package indexer.persisters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface BasePersister<T, D> {

    void persist(OutputStream output, Map<T, List<D>> invertedIndex) throws IOException;

}
