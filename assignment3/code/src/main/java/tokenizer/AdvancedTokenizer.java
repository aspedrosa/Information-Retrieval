package tokenizer;

import tokenizer.linguistic_rules.LinguisticRule;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedTokenizer extends BaseTokenizer {

    /**
     * Matcher to remove all punctuation except slashes. Insert spaces
     */
    private final static Matcher punctWithoutSome = Pattern.compile("[[\\p{Punct}]&&[^-]]").matcher("");

    /**
     * Matcher of slash to remove it from words, to make it an entire word
     */
    private final static Matcher slash = Pattern.compile("-").matcher("");

    /**
     * Matcher to remove all terms with just digits
     */
    private final static Matcher allNum = Pattern.compile("^\\p{Digit}+$").matcher("");

    /**
     * Default constructor. With no linguistic rules
     */
    public AdvancedTokenizer() {
        super();
    }

    /**
     * Constructor to define linguistic rules to apply
     *
     * @param rules to apply to the terms
     */
    public AdvancedTokenizer(List<LinguisticRule> rules) {
        super(rules);
    }

    /**
     * Tokenizes some document's content
     *
     * @param toTokenize line to tokenize
     * @return parsed terms
     */
    @Override
    public List<String> tokenizeString(String toTokenize) {
        String lowerCase = toTokenize.toLowerCase();
        String withoutPunct = punctWithoutSome.reset(lowerCase).replaceAll(" ");
        String mergedSlash = slash.reset(withoutPunct).replaceAll("");
        String toSplit = mergedSlash.trim();

        List<String> terms = Arrays.asList(WHITE_SPACES.split(toSplit));
        for (LinguisticRule rule : rules) {
            terms = rule.apply(terms);
        }

        for (int i = terms.size() - 1; i >= 0; i--) {
            String term = terms.get(i);
            if (allNum.reset(term).matches()) {
                terms.remove(i);
            }
        }

        return terms;
    }

}
