package indexer.persisters;

import indexer.structures.BaseDocument;
import indexer.structures.BaseTerm;
import indexer.structures.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

public abstract class TermByLinePersister<T extends Block &BaseTerm,
    D extends Block &BaseDocument> extends ForEachTermPersister<T, D> {

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
