package indexer.persisters;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Writes term's information by line
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public abstract class TermByLinePersister<T extends Block & BaseTerm,
                                          D extends Block & BaseDocument> extends ForEachTermPersister<T, D> {

    /**
     * Writes the term and its posting list on a csv format (First column is the term
     *  then the several documents).
     *
     * @param output to where the index will be written
     * @param term term object
     * @param documents posting list associated to the term
     * @throws IOException if some error occurs while writing the index to the stream
     */
    @Override
    public final void handleTerm(OutputStream output, T term, List<D> documents) throws IOException {
        output.write(term.getTerm().getBytes());
        output.write(',');

        Iterator<D> it = documents.iterator();
        while (it.hasNext()) {
            D doc = it.next();

            String documentString = handleDocument(doc);

            output.write(documentString.getBytes());

            if (it.hasNext()) {
                output.write(',');
            }
        }

        output.write('\n');
    }
}
