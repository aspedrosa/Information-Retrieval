package indexer.persisters.inverted_index;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Writes term's information in a csv format
 *
 * @param <T> type of the terms
 * @param <D> type of the documents
 */
public abstract class CSV<T extends Block & BaseTerm,
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
        byte[] termBytes = term.getTerm().getBytes();
        output.write(termBytes, 0, termBytes.length);
        output.write(new byte[] {','}, 0, 1);

        Iterator<D> it = documents.iterator();
        while (it.hasNext()) {
            D doc = it.next();

            byte[] documentBytes = handleDocument(doc).getBytes();

            output.write(documentBytes, 0 , documentBytes.length);

            if (it.hasNext()) {
                output.write(new byte[] {','}, 0, 1);
            }
        }

        output.write(new byte[] {'\n'}, 0, 1);
    }
}
