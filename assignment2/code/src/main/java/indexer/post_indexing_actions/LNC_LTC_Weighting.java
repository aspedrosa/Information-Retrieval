package indexer.post_indexing_actions;

import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeight;
import parsers.documents.Document;

import java.util.List;

/**
 * Applies the lnc.ltc tf-idf weighting variant
 *
 * @param <V> type of the extra info of the
 *  documents, which has a term associated
 */
public class LNC_LTC_Weighting<V extends DocumentWeight> implements CalculateWeightsPostIndexingAction<V>  {

    @Override
    public void apply(TermWithInfo<Float> term, List<DocumentWithInfo<V>> postingList) {
        int tf = 0;

        double cosineNormalization = 0;
        for (DocumentWithInfo<V> document : postingList) {
            float weight = document.getExtraInfo().getWeight();
            weight = (float) (1 + Math.log10(weight));
            tf += weight;
            cosineNormalization += Math.pow(weight, 2);
            document.getExtraInfo().setWeight(weight);
        }

        // calculate idf
        term.setExtraInfo((float)
            Math.log10((double)
                (Document.getGlobalId() + 1)
                /
                postingList.size()
            )
        );

        cosineNormalization = Math.sqrt(cosineNormalization);

        for (DocumentWithInfo<V> document : postingList) {
            float weight = document.getExtraInfo().getWeight();
            document.getExtraInfo().setWeight(
                (float) (weight / cosineNormalization)
            );
        }
    }

}
