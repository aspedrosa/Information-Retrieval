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
    protected LinguisticRule ruleChain;

    /**
     * Default constructor
     */
    public BaseTokenizer() {}

    /**
     * Alternative constructor
     *
     * @param ruleChain rules to be applied to the
     *  lines to be tokenized
     */
    public BaseTokenizer(LinguisticRule ruleChain) {
        this.ruleChain = ruleChain;
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
