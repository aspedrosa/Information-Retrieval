package tokenizer.linguistic_rules;

import org.tartarus.snowball.SnowballStemmer;

import java.util.List;

public class SnowballStemmerRule extends LinguisticRule {

    private SnowballStemmer stemStrategy;

    public SnowballStemmerRule(SnowballStemmer stemStrategy) {
        super();
        this.stemStrategy = stemStrategy;
    }

    public SnowballStemmerRule(SnowballStemmer stemStrategy, LinguisticRule next) {
        super(next);
        this.stemStrategy = stemStrategy;
    }

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
