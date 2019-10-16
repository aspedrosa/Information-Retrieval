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

    /**
     * Rules to be applied to the lines to be tokenized
     */
    protected List<LinguisticRule> rules;

    /**
     * Default constructor
     */
    public BaseTokenizer() {
        this.rules = new LinkedList<>();
    }

    /**
     * Alternative constructor
     *
     * @param rules rules to be applied to the
     *  lines to be tokenized
     */
    public BaseTokenizer(List<LinguisticRule> rules) {
        this.rules = rules;
    }

    /**
     * Adds new a rule
     *
     * @param newRule the new rule
     */
    public void addRule(LinguisticRule newRule) {
        rules.add(newRule);
    }

    /**
     * Iterates over the several lines and tokenized
     *  line by line
     *
     * @param toTokenize a list of lines to tokenize
     * @return list of terms
     */
    public final List<String> tokenizeDocument(List<String> toTokenize) {
        List<String> terms = new LinkedList<>();

        for (String strToTokenize : toTokenize) {
            terms.addAll(tokenizeString(strToTokenize));
        }

        return terms;
    }

    /**
     * Tokenizes a line of the document's content
     *
     * @param toTokenize line to tokenize
     * @return list of terms
     */
    public abstract List<String> tokenizeString(String toTokenize);

}
