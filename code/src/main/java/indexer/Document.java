package indexer;

public class Document<T> extends Block<T> {

    public Document(String docId) {
        super(docId);
        this.key = docId;
    }

    public Document(String docId, T extraInfo) {
        super(docId, extraInfo);
        this.key = docId;
        this.extraInfo = extraInfo;
    }

    public String getDocId() {
        return key;
    }

}
