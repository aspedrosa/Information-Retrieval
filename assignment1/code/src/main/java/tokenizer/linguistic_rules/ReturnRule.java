package tokenizer.linguistic_rules;

import java.util.List;

public class ReturnRule extends LinguisticRule {

    @Override
    public List<String> apply(List<String> toApply) {
        return toApply;
    }

}
