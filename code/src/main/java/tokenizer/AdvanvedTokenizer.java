package tokenizer;

import tokenizer.linguistic_rules.LinguisticRule;

import java.util.List;

public class AdvanvedTokenizer extends BaseTokenizer {

    public AdvanvedTokenizer() {
        super();
    }

    public AdvanvedTokenizer(List<LinguisticRule> rules) {
        super(rules);
    }

    @Override
    public List<String> tokenizeString(String toTokenize) {
        // TODO
        return null;
    }

}
