
package tokenizer.linguistic_rules;

import java.util.List;

/**
 * Allows reuse complex tokenization rules
 */
public interface LinguisticRule {

    /**
     * Applies the complex tokenization rule
     *
     * @param toApply original terms list
     * @return a new version of the terms
     *  list, with some terms filtered
     */
    List<String> apply(List<String> toApply);

}
