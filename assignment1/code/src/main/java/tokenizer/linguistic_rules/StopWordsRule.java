package tokenizer.linguistic_rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StopWordsRule implements LinguisticRule {

    private Set<String> stopWords;

    public StopWordsRule(Set<String> stopWords) {
        this.stopWords = stopWords;
    }

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
