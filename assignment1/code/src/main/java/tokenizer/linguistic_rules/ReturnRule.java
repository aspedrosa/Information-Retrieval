package tokenizer.linguistic_rules;

import java.util.List;

public class ReturnRule extends LinguisticRule {

    public ReturnRule() {
        super(null);
    }

    @Override
    public List<String> apply(List<String> toApply) {
        return toApply;
    }

}
