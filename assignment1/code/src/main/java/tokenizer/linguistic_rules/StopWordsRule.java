package tokenizer.linguistic_rules;

import java.util.List;

public class StopWordsRule extends LinguisticRule {

    private List<String> stopWords;

    public StopWordsRule(List<String> stopWords) {
        super();
        this.stopWords = stopWords;
    }

    public StopWordsRule(List<String> stopWords, LinguisticRule next) {
        super(next);
        this.stopWords = stopWords;
    }

    @Override
    public List<String> apply(List<String> terms) {
        for (int i = terms.size() - 1; i >= 0; i--) {
            String term = terms.get(i);

            if (stopWords.contains(term)) {
                terms.remove(i);
            }
        }

        return next.apply(terms);
    }
}
