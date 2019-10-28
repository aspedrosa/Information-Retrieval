
package tokenizer.linguistic_rules;

import java.util.List;

public interface LinguisticRule {

    List<String> apply(List<String> toApply);

}
