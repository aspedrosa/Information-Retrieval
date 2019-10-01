
package indexer;

public class Term<T> extends Block<T> {

    public Term(String term) {
        super(term);
        this.key = term;
    }

    public Term(String term, T extraInfo) {
        super(term, extraInfo);
        this.key = term;
        this.extraInfo = extraInfo;
    }

    public String getTerm() {
        return key;
    }

}
