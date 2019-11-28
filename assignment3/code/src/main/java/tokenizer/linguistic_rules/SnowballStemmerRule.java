package tokenizer.linguistic_rules;

import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.SnowballStemmer;

import java.util.List;

/**
 * Applies stemming to the terms list
 */
public class SnowballStemmerRule implements LinguisticRule {

    /**
     * Steaming strategy
     */
    private SnowballStemmer stemStrategy;

    /**
     * Default Constructor. Uses the english stemmer by
     *  default.
     */
    public SnowballStemmerRule() {
        this.stemStrategy = new englishStemmer();
    }

    /**
     * Constructor that allows the definition
     *  of steaming strategy
     *
     * @param stemStrategy steaming strategy to apply to the terms
     */
    public SnowballStemmerRule(SnowballStemmer stemStrategy) {
        this.stemStrategy = stemStrategy;
    }

    /**
     * Applies the complex tokenization rule
     *
     * @param terms original terms list
     * @return a new version of the terms
     *  list, with the terms steamed
     */
    @Override
    public List<String> apply(List<String> terms) {
        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i);

            stemStrategy.setCurrent(term);
            stemStrategy.stem();

            terms.set(i, stemStrategy.getCurrent());
        }

        return terms;
    }

}
