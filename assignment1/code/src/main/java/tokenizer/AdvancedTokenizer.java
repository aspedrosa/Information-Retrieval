package tokenizer;

import tokenizer.linguistic_rules.LinguisticRule;
import java.util.Arrays;
import java.util.List;

public class AdvancedTokenizer extends BaseTokenizer {

    public AdvancedTokenizer(List<LinguisticRule> rules) {
        super(rules);
    }

    @Override
    public List<String> tokenizeString(String toTokenize) {
        String toSplit = toTokenize
                .toLowerCase()
                .replaceAll("[^\\p{Alpha}]", " ")
                .replaceAll("\\b\\p{Alpha}{1,2}\\b", "")
                .trim();

        String[] example = WHITE_SPACES.split(toSplit);

        List<String> terms = Arrays.asList(example);
        for (LinguisticRule rule : rules) {
            terms = rule.apply(terms);
        }

        return terms;
    }

}
