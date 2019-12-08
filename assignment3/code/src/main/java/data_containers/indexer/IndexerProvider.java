package data_containers.indexer;

import data_containers.indexer.structures.BaseDocument;
import data_containers.indexer.structures.BaseTerm;
import data_containers.indexer.structures.Block;

import java.util.List;
import java.util.Map;

public interface IndexerProvider<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    BaseIndexer<T, D> createIndexer(Map<T, List<D>> loadedIndex);

}
