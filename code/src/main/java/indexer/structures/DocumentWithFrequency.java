package indexer.structures;

public class DocumentWithFrequency extends BlockWithInfo<Integer> implements BaseDocument {

    public DocumentWithFrequency(String docId) {
        super(docId, 0);
    }

    public DocumentWithFrequency(String docId, int frequency) {
        super(docId, frequency);
    }

    public String getDocId() {
        return key;
    }

    public void increseFrequency() {
        extraInfo++;
    }

    public int getFrequency() {
        return extraInfo;
    }

}
