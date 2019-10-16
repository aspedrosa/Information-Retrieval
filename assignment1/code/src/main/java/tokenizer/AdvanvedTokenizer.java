package tokenizer;

import tokenizer.linguistic_rules.LinguisticRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdvanvedTokenizer extends BaseTokenizer {

    public AdvanvedTokenizer() {
        super();
    }


    public AdvanvedTokenizer(LinguisticRule ruleChain) {
        super(ruleChain);
    }

    @Override
    public List<String> tokenizeString(String toTokenize) {
        // TODO

        String toSplit = toTokenize
                .toLowerCase()
                .replaceAll("[^\\p{Alpha}]", " ")
                .replaceAll("\\b\\p{Alpha}{1,2}\\b", "")
                .trim();

        // in some cases the resulting string to split is an empty
        //  string. Doing a split will result in an array of
        //  one element (empty string). In this case return an
        //  empty list
        if (toSplit.equals("")) {
            return Collections.emptyList();
        }

        return ruleChain.apply(Arrays.asList(toSplit.split("\\s+")));
    }

}
