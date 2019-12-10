package data_containers.indexer.structures;

/**
 * Document that hold some extra information beyond
 *  it's weight and id
 *
 * @param <W> type of the weight
 * @param <I> type of the extra term information
 */
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
