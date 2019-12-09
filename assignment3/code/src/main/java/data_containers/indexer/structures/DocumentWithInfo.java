package data_containers.indexer.structures;

public class DocumentWithInfo<W extends Number, I> extends Document<W> {

    private I extraInfo;

    public DocumentWithInfo(int docId, W weight, I extraInfo) {
        super(docId, weight);
        this.extraInfo = extraInfo;
    }

    public I getExtraInfo() {
        return extraInfo;
    }
}
