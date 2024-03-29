package tokenizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple implementation of a tokenizer
 */
public class SimpleTokenizer extends BaseTokenizer {

    private static final Matcher nonAlpha = Pattern.compile("[^\\p{Alpha}]").matcher("");
    private static final Matcher lessThanThree = Pattern.compile("\\b\\p{Alpha}{1,2}\\b").matcher("");

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
         String lowerCase = toTokenize.toLowerCase();
         String justAlpha = nonAlpha.reset(lowerCase).replaceAll(" ");
         String moreThanTwo = lessThanThree.reset(justAlpha).replaceAll("");
         String toSplit = moreThanTwo.trim();

        // in some cases the resulting string to split is an empty
        //  string. Doing a split will result in an array of
        //  one element (empty string). In this case return an
        //  empty list
        if (toSplit.equals("")) {
            return Collections.emptyList();
        }

        return Arrays.asList(
            WHITE_SPACES.split(toSplit)
        );
    }

}
