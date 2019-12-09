package data_containers.indexer.structures;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

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
