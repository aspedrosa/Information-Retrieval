package tokenizer.linguistic_rules;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters all the terms with lower length than the one
 *  defined on the constructor
 */
public class MinLengthRule implements LinguisticRule {

    /**
     * Minimum length to filter terms
     */
    private int minLength;

    /**
     * Main constructor
     *
     * @param minLength minimum length of a term
     */
    public MinLengthRule(int minLength) {
        this.minLength = minLength;
    }

    /**
     * Applies the complex tokenization rule
     *
     * @param toApply original terms list
     * @return a new version of the terms
     *  list, without terms with
     *  less than three characters
     */
    @Override
    public List<String> apply(List<String> toApply) {
        List<String> result = new ArrayList<>(toApply.size());

        for (String term : toApply) {
            if (term.length() >= minLength) {
                result.add(term);
            }
        }

        return result;
    }

}
