package tokenizer;

import java.util.LinkedList;
import java.util.List;

import tokenizer.linguistic_rules.LinguisticRule;

/**
 * All tokenizers extends this class
 * Base classes should implement the tokenizeString method
 *  where it applies all text processing rules/steps (e.g.
 *  tokenization, stemming, ...)
 */
public abstract class BaseTokenizer {

    protected List<LinguisticRule> rules;

    public BaseTokenizer(List<LinguisticRule> rules) {
        this.rules = rules;
    }

    public BaseTokenizer() {
        this.rules = new LinkedList<>();
    }

    public void addRule(LinguisticRule newRule) {
        rules.add(newRule);
    }

    public final List<String> tokenizeDocument(List<String> toTokenize) {
        List<String> terms = new LinkedList<>();

        for (String strToTokenize : toTokenize) {
            terms.addAll(tokenizeString(strToTokenize));
        }

        return terms;
    }

    public abstract List<String> tokenizeString(String toTokenize);

}
