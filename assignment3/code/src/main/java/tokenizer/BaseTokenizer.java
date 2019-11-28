package tokenizer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import tokenizer.linguistic_rules.LinguisticRule;

/**
 * All tokenizers extends this class
 * Base classes should implement the tokenizeString method
 *  where it applies all text processing rules/steps (e.g.
 *  tokenization, stemming, ...)
 */
public abstract class BaseTokenizer {

    protected static final Pattern WHITE_SPACES = Pattern.compile("\\s+");

    /**
     * Rules to be applied to the lines to be tokenized
     */
    protected List<LinguisticRule> rules;

    /**
     * Default constructor
     */
    public BaseTokenizer() {
        rules = Collections.emptyList();
    }

    /**
     * Alternative constructor
     *
     * @param rules to be applied to the
     *  lines to be tokenized
     */
    public BaseTokenizer(List<LinguisticRule> rules) {
        this.rules = rules;
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
     * Tokenizes some document's content
     *
     * @param toTokenize line to tokenize
     * @return list of terms
     */
    public abstract List<String> tokenizeString(String toTokenize);

}
