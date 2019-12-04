package indexer;

import indexer.post_indexing_actions.CalculateIDFAction;
import indexer.structures.DocumentWithInfo;
import indexer.structures.TermWithInfo;
import indexer.structures.aux_structs.DocumentWeightAndPositions;
import indexer.weights_calculation.CalculationsBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Specific type a indexer with weights that stores
 *  the positions that a certain term appears in a
 *  certain document
 */
public class WeightsAndPositionsIndexer extends WeightsIndexerBase<DocumentWeightAndPositions> {

    /**
     * Auxiliary structure to store the positions for each
     *  term for the document currently indexing
     */
    private Map<String, List<Integer>> auxTermsPositions;

    /**
     * Main constructor. Since is a specific
     * indexer, on the indexer it creates the necessary
     * classes specific to it
     */
    public WeightsAndPositionsIndexer(CalculationsBase calculations) {
        super(calculations);
        dummyTerm = new TermWithInfo<>();
        auxTermsPositions = new HashMap<>();
    }

    @Override
    public DocumentWithInfo<DocumentWeightAndPositions> createDocument(int documentId, DocumentWeightAndPositions weight) {
        return new DocumentWithInfo<>(
            documentId,
            weight
        );
    }

    @Override
    public DocumentWeightAndPositions createDocumentWeight(String term, float weight) {
        return new DocumentWeightAndPositions(
            weight,
            auxTermsPositions.get(term)
        );
    }

    /**
     * Since the base indexTerms method only calculates the
     *  frequency for each term on a document, it is overwritten
     *  here to first calculate the position of each term
     *  in the document and then calculate the frequency,
     *  by calling the base method
     *
     * @param documentId to which the terms where extracted
     * @param terms sequence of terms to index
     */
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

        // calculate term frequencies
        super.indexTerms(documentId, terms);

        // clear auxiliary positions structure to index other documents
        auxTermsPositions.clear();
    }

}
