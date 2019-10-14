package indexer.persisters;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Type of persister that iterates over terms sorted
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public abstract class ForEachTermPersister<T extends Block & BaseTerm,
                                           D extends Block & BaseDocument> implements BasePersister<T, D> {

    /**
     * Sorts the terms present on the inverted index and then
     *  iterate over them and calls a function handleTerm(), passing
     *  the term and its posting list as argument, that dictates the
     *  output format
     *
     * @param output to where the index will be written
     * @param invertedIndex the inverted index held by the indexer
     * @throws IOException if some errors occur while writing the index to the stream
     */
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

    /**
     * Dictates the output format of the terms and their posting lists
     *
     * @param output to where the index will be written
     * @param term term object
     * @param documents posting list associated to the term
     * @throws IOException if some error occurs while writing to the stream
     */
    public abstract void handleTerm(OutputStream output, T term, List<D> documents) throws IOException;

    /**
     * Function that dictates the output format of the
     *  documents of the posting list.
     * This function is called by the handleTerm() method
     *
     * @param document document class
     * @return a formatted string representing the document
     */
    public abstract String handleDocument(D document);

}
