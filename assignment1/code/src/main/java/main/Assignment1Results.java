package main;

import indexer.structures.DocumentWithFrequency;
import indexer.structures.SimpleTerm;
import indexer.structures.TermWithFrequency;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Answers the questions proposed on assignment 1
 */
public class Assignment1Results {

    /**
     * Inverted index present on the indexer
     */
    private static Map<SimpleTerm, List<DocumentWithFrequency>> invertedIndex;

    /**
     * Executes the functions defined bellow
     *
     * @param invertedIndex present on the indexer
     */
    public static void results(Map<SimpleTerm, List<DocumentWithFrequency>> invertedIndex) {
        Assignment1Results.invertedIndex = invertedIndex;

        for (Method method : Assignment1Results.class.getDeclaredMethods()) {

            // filter out some functions such as lambdas and this function itself
            if (method.getDeclaringClass().equals(Assignment1Results.class)
                &&
                !method.getName().contains("lambda")
                &&
                !method.getName().equals("results")
            ) {
                try {
                    method.invoke(Assignment1Results.class);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    //
                }
            }
        }
    }

    /**
     * What is your vocabulary size?
     */
    private static void vocabularySize() {
        System.out.println("\nVocabulary size");
        System.out.println(invertedIndex.keySet().size());
    }

    /**
     * List the ten first terms (in alphabetic order) that appear in only one document (document
     * frequency = 1).
     */
    private static void tenTermsOneDocumentAlphabetically() {
        System.out.println("\nTen first terms (in alphabetic order) that appear in only one document");
        invertedIndex.keySet().stream()
            .filter((term) -> invertedIndex.get(term).size() == 1)
            .sorted(Comparator.naturalOrder())
            .limit(10)
            .map(SimpleTerm::getTerm)
            .forEach(System.out::println);
    }

    /**
     * List the ten terms with highest document frequency.
     */
    private static void tenTermsByFrequency() {
        System.out.println("\nTen terms with highest document frequency");
        invertedIndex.keySet().stream()
            .map((term) -> {
                int termFrequency = 0;
                for (DocumentWithFrequency doc : invertedIndex.get(term)) {
                    termFrequency += doc.getFrequency();
                }

                return new TermWithFrequency(term.getTerm(), termFrequency);
            })
            .sorted((term1, term2) -> {
                if (term1.getFrequency() < term2.getFrequency()) {
                    return 1;
                }
                else if (term1.getFrequency() > term2.getFrequency()) {
                    return -1;
                }
                return 0;
            })
            .limit(10)
            .forEach((term) -> {
                System.out.println(String.format("%10s : %d", term.getTerm(), term.getFrequency()));
            });
    }
}
