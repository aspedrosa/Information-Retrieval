package tokenizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleTokenizer extends BaseTokenizer {

    public SimpleTokenizer() {
        super();
    }

    @Override
    public List<String> tokenizeString(String toTokenize) {
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

        return Arrays.asList(
            toSplit.split("\\s+")
        );
    }

}
