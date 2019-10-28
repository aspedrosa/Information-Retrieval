package tokenizer.linguistic_rules;

import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.SnowballStemmer;

import java.util.List;

public class SnowballStemmerRule implements LinguisticRule {

    private SnowballStemmer stemStrategy;

    public SnowballStemmerRule() {
        this.stemStrategy = new englishStemmer();
    }

    public SnowballStemmerRule(SnowballStemmer stemStrategy) {
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
