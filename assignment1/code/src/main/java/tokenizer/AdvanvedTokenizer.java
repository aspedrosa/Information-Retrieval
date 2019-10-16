package tokenizer;

import tokenizer.linguistic_rules.LinguisticRule;
import org.tartarus.snowball.*;
import org.tartarus.snowball.ext.*;
import tokenizer.linguistic_rules.StopWords;

import java.util.ArrayList;
import java.util.List;

public class AdvanvedTokenizer extends BaseTokenizer {

    public AdvanvedTokenizer() {
        super();
    }

    private SnowballStemmer stemmer = new englishStemmer();

    private LinguisticRule rules = new StopWords();

    public AdvanvedTokenizer(List<LinguisticRule> rules) {
        super(rules);
    }

    @Override
    public List<String> tokenizeString(String toTokenize) {
        // TODO

        ArrayList<String> token = new ArrayList<>();
        String toSplit = toTokenize
                .toLowerCase()
                .replaceAll("[^\\p{Alpha}]", " ")
                .replaceAll("\\b\\p{Alpha}{1,2}\\b", "")
                .trim();

        String[] example = toSplit.split("\\s+");

        for (String ex: example) {
            if (!this.rules.readStopWords().contains(ex)){
                this.stemmer.setCurrent(ex);
                if (this.stemmer.stem()){
                    token.add(this.stemmer.getCurrent());
                }
            }
        }

        return token;
        //return null;
    }

}
