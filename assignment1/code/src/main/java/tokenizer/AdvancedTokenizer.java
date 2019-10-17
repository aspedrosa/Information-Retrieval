package tokenizer;

import tokenizer.linguistic_rules.LinguisticRule;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedTokenizer extends BaseTokenizer {

    private final static Matcher punctWithoutSome = Pattern.compile("[[\\p{Punct}]&&[^-]]").matcher("");
    private final static Matcher slash = Pattern.compile("-").matcher("");
    private final static Matcher allNum = Pattern.compile("^\\p{Digit}+$").matcher("");

    public AdvancedTokenizer(List<LinguisticRule> rules) {
        super(rules);
    }

    @Override
    public List<String> tokenizeString(String toTokenize) {
        String lowerCase = toTokenize.toLowerCase();
        String withoutPunct = punctWithoutSome.reset(lowerCase).replaceAll(" ");
        String mergedSlash = slash.reset(withoutPunct).replaceAll("");
        String toSplit = mergedSlash.trim();

        String[] example = WHITE_SPACES.split(toSplit);

        List<String> terms = Arrays.asList(example);
        for (LinguisticRule rule : rules) {
            terms = rule.apply(terms);
        }

        for (int i = terms.size() - 1; i >= 0; i--) {
            String term = terms.get(i);
            if (term.length() < 3 || allNum.reset(term).matches()) {
                terms.remove(i);
            }
        }

        return terms;
    }

}
