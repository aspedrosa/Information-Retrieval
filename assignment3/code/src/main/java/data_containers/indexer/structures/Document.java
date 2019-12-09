package data_containers.indexer.structures;

public class Document<W extends Number> {

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
