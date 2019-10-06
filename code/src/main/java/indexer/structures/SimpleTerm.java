
package indexer.structures;

public class SimpleTerm extends Block<String> implements BaseTerm {

    public SimpleTerm(String term) {
        super(term);
    }

    public String getTerm() {
        return key;
    }

    public void setTerm(String term) {
        this.key = term;
    }

}
