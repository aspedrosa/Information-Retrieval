
package tokenizer.linguistic_rules;

import java.util.List;

public abstract class LinguisticRule {

    protected LinguisticRule next;

    public LinguisticRule() {
        this.next = new ReturnRule();
    }

    public LinguisticRule(LinguisticRule next) {
        this.next = next;
    }

    public abstract List<String> apply(List<String> toApply);

}
