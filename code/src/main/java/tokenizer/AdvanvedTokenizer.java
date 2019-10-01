package tokenizer;

import indexer.BaseIndexer;
import tokenizer.linguistic_rules.LinguisticRule;

import java.util.List;

public class AdvanvedTokenizer extends BaseTokenizer {

    public AdvanvedTokenizer(BaseIndexer indexer) {
        super(indexer);
    }

    public AdvanvedTokenizer(List<LinguisticRule> rules, BaseIndexer indexer) {
        super(rules, indexer);
    }

    @Override
    public void tokenizeString(String docId, String toTokenize) {
        // TODO
    }

}
