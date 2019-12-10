package data_containers.indexer.structures;

import java.io.Serializable;

public class Document<W extends Number & Serializable> implements Serializable {

    protected int docId;

    protected W weight;

    public Document(int docId, W weight) {
        this.docId = docId;
        this.weight = weight;
    }

    public int getDocId() {
        return docId;
    }

    public W getWeight() {
        return weight;
    }
}
