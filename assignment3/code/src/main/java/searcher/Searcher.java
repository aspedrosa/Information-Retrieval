package searcher;

import data_containers.indexer.structures.BaseDocument;
import data_containers.indexer.structures.BaseTerm;
import data_containers.indexer.structures.Block;
import tokenizer.BaseTokenizer;

import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class Searcher<T extends Block & BaseTerm, D extends Block & BaseDocument> {

    private BaseTokenizer tokenizer;

    private TreeSet<String> indexesFilenames;

    private String indexesFolder;

    private TreeMap<String, Integer> onMemory; // Integer should be the indexer structure

    public Searcher(BaseTokenizer tokenizer, TreeSet<String> indexFilenames, String indexesFolder) {
        this.tokenizer = tokenizer;
        this.indexesFilenames = indexFilenames;
        this.indexesFolder = indexesFolder;

        this.onMemory = new TreeMap<>();
    }

    public List<String> queryIndex(String query) {
        List<String> terms = tokenizer.tokenizeString(query);

        terms.forEach(term -> {
            if (onMemory.lowerKey(term) == null ) { // && not in indexer structure
                // in memory
            }
            else {
                // not in memory

                String indexFilename = indexesFilenames.lower(term);

                if (indexFilename != null & !onMemory.containsKey(indexFilename)) {
                    // load from disk
                }
                /*else {
                    // don't have the term indexed
                    // or
                    // the file to load is already in memory and it does not contain the term
                }*/
            }

            // get documents
        });

        return null;
    }

}
