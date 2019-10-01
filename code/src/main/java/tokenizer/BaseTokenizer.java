package tokenizer;

import java.util.LinkedList;
import java.util.List;

import indexer.BaseIndexer;
import tokenizer.linguistic_rules.LinguisticRule;

public abstract class BaseTokenizer {

    protected List<LinguisticRule> rules;

    protected BaseIndexer indexer;

    public BaseTokenizer(List<LinguisticRule> rules, BaseIndexer indexer) {
        this.rules = rules;
        this.indexer = indexer;
    }

    public BaseTokenizer(BaseIndexer indexer) {
        this.rules = new LinkedList<>();
        this.indexer = indexer;
    }

    public void addRule(LinguisticRule newRule) {
        rules.add(newRule);
    }

    public void tokenizeDocument(String docId, List<String> toTokenize) {
        for (String strToTokenize : toTokenize) {
            tokenizeString(docId, strToTokenize);
        }
    }

    public abstract void tokenizeString(String docId, String toTokenize);

}
