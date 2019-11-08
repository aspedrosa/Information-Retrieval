package indexer.persisters.inverted_index;

import indexer.structures.DocumentWithInfo;
import indexer.structures.SimpleTerm;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Defines the persisting and load strategy for the inverted index
 *  of a frequency indexer
 */
public class FrequencyPersister extends CSV<SimpleTerm, DocumentWithInfo<Integer>> {

    /**
     * Pattern to split the fields in each document
     */
    private static final Pattern twoPoints = Pattern.compile(":");

    /**
     * Method that defines the format of a single document
     *
     * @param document to format
     * @return a formatted string representing the document
     */
    @Override
    public String handleDocument(DocumentWithInfo<Integer> document) {
        return String.format("%d:%d", document.getDocId(), document.getExtraInfo());
    }

    /**
     * Dictates the output format of the terms
     *
     * @param term term object
     * @return a formatted string representing the document
     */
    @Override
    public String handleTerm(SimpleTerm term) {
        return term.getTerm();
    }

    /**
     * Creates an entry with SimpleTerm as term
     *  and DocumentWithInfo&lt;Integer&gt; as the documents
     *  stored in the posting list
     *
     * @param entry entry's content
     * @return an entry with term and its posting list
     */
    @Override
    public Entry<SimpleTerm, DocumentWithInfo<Integer>> createEntry(String[] entry) {
        SimpleTerm term = new SimpleTerm(entry[0]);

        List<DocumentWithInfo<Integer>> documents = new LinkedList<>();

        for (int i = 1; i < entry.length; i++) {
            String[] fields = twoPoints.split(entry[i]);

            documents.add(new DocumentWithInfo<>(
                Integer.parseInt(fields[0]),
                Integer.parseInt(fields[1])
            ));
        }

        return new Entry<>(term, documents);
    }

}
