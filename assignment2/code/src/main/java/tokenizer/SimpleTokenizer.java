package tokenizer;

import tokenizer.linguistic_rules.LinguisticRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple implementation of a tokenizer
 */
public class SimpleTokenizer extends BaseTokenizer {

    /**
     * Matcher to remove all non alphabetic characters
     */
    private static final Matcher nonAlpha = Pattern.compile("[^\\p{Alpha}]").matcher("");

    /**
     * Matcher to remove all words with less than three words
     */
    private static final Matcher lessThanThree = Pattern.compile("\\b\\p{Alpha}{1,2}\\b").matcher("");

    /**
     * Default constructor
     */
    public SimpleTokenizer() {
        super();
    }

    /**
     * Allows the definition of rules to apply
     *  to terms
     *
     * @param rules to apply to the terms
     */
    public SimpleTokenizer(List<LinguisticRule> rules) {
        super(rules);
    }

    /**
     * <ul>
     *     <li>1. switches case to lower case</li>
     *     <li>2. replaces non alphabetic letters by a space</li>
     *     <li>3. removes terms with less then three characters</li>
     *     <li>4. splits on spaces</li>
     * </ul>
     *
     * @param toTokenize line to tokenize
     * @return list of terms
     */
    @Override
    public List<String> tokenizeString(String toTokenize) {
         String toSplit =
             lessThanThree  // 3. remove words with less than 3 characters
             .reset(
                 nonAlpha // 2. remove non alpha characters
                 .reset(toTokenize.toLowerCase()) // 1. lower case
                 .replaceAll(" ")
             )
             .replaceAll("")
             .trim(); // 4. remove trailing spaces

        // in some cases the resulting string to split is an empty
        //  string. Doing a split will result in an array of
        //  one element (empty string). In this case return an
        //  empty list
        if (toSplit.equals("")) {
            return Collections.emptyList();
        }

        List<String> terms = Arrays.asList(WHITE_SPACES.split(toSplit));

        for (LinguisticRule rule : rules) {
            terms = rule.apply(terms);
        }

        return terms;
    }

}
