package indexer.structures;

public class DocumentWithFrequency extends BlockWithInfo<Integer, Integer> implements BaseDocument {

    public DocumentWithFrequency(int docId) {
        super(docId, 0);
    }

    public DocumentWithFrequency(int docId, int frequency) {
        super(docId, frequency);
    }

    public int getDocId() {
        return key;
    }

    public void increseFrequency() {
        extraInfo++;
    }

    public int getFrequency() {
        return extraInfo;
    }

}
