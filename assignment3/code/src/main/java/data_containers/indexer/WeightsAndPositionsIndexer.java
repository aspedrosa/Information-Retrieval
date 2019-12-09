package data_containers.indexer;

import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermWithInfo;
import data_containers.indexer.structures.aux_structs.DocumentWeightAndPositions;
import data_containers.indexer.weights_calculation.indexing.CalculationsBase;

import java.util.ArrayList;
import java.util.HashMap;
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

    protected WeightsAndPositionsIndexer(
        CalculationsBase calculations,
        Map<TermWithInfo<Float>, List<DocumentWithInfo<DocumentWeightAndPositions>>> loadedIndex
        ) {
        super(calculations, loadedIndex);
        dummyTerm = new TermWithInfo<>();
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

    @Override
    public BaseIndexer<TermWithInfo<Float>, DocumentWithInfo<DocumentWeightAndPositions>> createIndexer(
        Map<TermWithInfo<Float>, List<DocumentWithInfo<DocumentWeightAndPositions>>> loadedIndex) {
        return new WeightsAndPositionsIndexer(this.calculations, loadedIndex);
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
