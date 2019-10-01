package tokenizer;

import indexer.BaseIndexer;
import tokenizer.linguistic_rules.LinguisticRule;

import java.util.List;

public class SimpleTokenizer extends BaseTokenizer {

    public SimpleTokenizer(BaseIndexer indexer) {
        super(indexer);
    }

    public SimpleTokenizer(List<LinguisticRule> rules, BaseIndexer indexer) {
        super(rules, indexer);
    }

    @Override
    public void tokenizeString(String docId, String toTokenize) {
        // TODO
    }

}
