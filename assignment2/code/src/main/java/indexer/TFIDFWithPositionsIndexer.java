package indexer;

import indexer.structures.aux_structs.DocumentWeightAndPositions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TFIDFWithPositionsIndexer extends TFIDFIndexerBase<DocumentWeightAndPositions> {

    private Map<String, List<Integer>> auxTermsPositions;

    protected void insertDocument(int documentId, Map<String, Integer> frequencies) {
        // TODO
        // consult auxTermsPositions to create the structures to insert
    }

    @Override
    public void indexTerms(int documentId, List<String> terms) {
        // calculate terms positions
        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i);

            List<Integer> positionsList = auxTermsPositions.get(term);

            if (positionsList == null) {
                positionsList = new ArrayList<>();
                auxTermsPositions.put(term, positionsList);
            }

            positionsList.add(i);
        }

        super.indexTerms(documentId, terms);

        auxTermsPositions.clear();
    }

}
