
package indexer.structures;

public class SimpleTerm extends Block implements BaseTerm {

    public SimpleTerm(String term) {
        super(term);
    }

    public String getTerm() {
        return key;
    }

}
