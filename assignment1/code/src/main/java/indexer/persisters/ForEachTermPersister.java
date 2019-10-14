package indexer.persisters;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ForEachTermPersister<T extends Block & BaseTerm,
                                           D extends Block & BaseDocument> implements BasePersister<T, D> {

    @Override
    public final void persist(OutputStream output, Map<T, List<D>> invertedIndex) throws IOException {
        System.out.println("Sorting terms");

        List<T> sortedTerms = new ArrayList<>(invertedIndex.keySet());
        sortedTerms.sort(T::compareTo);

        System.out.println("Finished sorting terms");

        System.out.println("Writing index to disk");

        for (T term : sortedTerms) {
            handleTerm(output, term, invertedIndex.get(term));
        }
    }

    public abstract void handleTerm(OutputStream output, T term, List<D> documents) throws IOException;

    public abstract String handleDocument(D document);

}
