package data_containers.indexer.structures;

import java.util.List;

/**
 * Class to hold information relative to a term that
 *  also hold the idf associated to a term
 *
 * @param <W> type of the weight
 * @param <D> type of the document
 */
public class TermInfoWithIDF<W extends Number, D extends Document<W>> extends TermInfoBase<W, D> {

    private float idf;

    public TermInfoWithIDF() {
        super();
        idf = 0;
    }

    public TermInfoWithIDF(List<D> postingList) {
        super(postingList);
        idf = 0;
    }

    public TermInfoWithIDF(List<D> postingList, float idf) {
        super(postingList);
        this.idf = idf;
    }

    public float getIdf() {
        return idf;
    }

    public void setIdf(float idf) {
        this.idf = idf;
    }
}
