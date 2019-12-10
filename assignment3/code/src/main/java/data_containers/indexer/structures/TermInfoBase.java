package data_containers.indexer.structures;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to hold information relative to a term
 * This class will be used as value for the inverted index
 *
 * @param <W> type of the term
 * @param <D> type of the document
 */
public class TermInfoBase<W extends Number, D extends Document<W>> implements Serializable {

    private List<D> postingList;

    public TermInfoBase() {
        postingList = new LinkedList<>();
    }

    public TermInfoBase(List<D> postingList) {
        this.postingList = postingList;
    }

    public List<D> getPostingList() {
        return postingList;
    }

    public void setPostingList(List<D> postingList) {
        this.postingList = postingList;
    }

    public void addToPostingList(D document) {
        postingList.add(document);
    }

}
