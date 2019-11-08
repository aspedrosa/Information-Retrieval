package tokenizer.linguistic_rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Removes from the terms list the ones
 *  present on a given stop words list
 */
public class StopWordsRule implements LinguisticRule {

    /**
     * The words to filter
     */
    private Set<String> stopWords;

    /**
     * Main constructor
     *
     * @param stopWords the words to filter out
     *  from the terms list
     */
    public StopWordsRule(Set<String> stopWords) {
        this.stopWords = stopWords;
    }

    /**
     * Applies the complex tokenization rule
     *
     * @param terms original terms list
     * @return a new version of the terms
     *  list, without stop words
     */
    @Override
    public List<String> apply(List<String> terms) {
        List<String> result = new ArrayList<>(terms.size());

        for (String term : terms) {
            if (!stopWords.contains(term)) {
                result.add(term);
            }
        }

        return result;
    }
}
