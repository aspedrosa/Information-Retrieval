package data_containers.indexer;

import data_containers.indexer.post_indexing_actions.CalculateIDFAction;
import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermWithInfo;
import data_containers.indexer.structures.aux_structs.DocumentWeight;
import data_containers.indexer.weights_calculation.CalculationsBase;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Indexes terms associating weights to terms
 *  for weight ranking of documents
 *
 * @param <V> type of the extra info of the
 *  documents, which has a term associated
 */
public abstract class WeightsIndexerBase <V extends DocumentWeight> extends BaseIndexer<TermWithInfo<Float>, DocumentWithInfo<V>> {

    protected CalculationsBase calculations;

    /**
     * Main constructor
     */
    public WeightsIndexerBase(CalculationsBase calculations) {
        super(new CalculateIDFAction<>());
        this.calculations = calculations;
    }

    protected WeightsIndexerBase(
        CalculationsBase calculations,
        Map<TermWithInfo<Float>, List<DocumentWithInfo<V>>> loadedIndex) {
        super(new CalculateIDFAction<>(), loadedIndex);
        this.calculations = calculations;
    }

    @Override
    protected final void insertDocument(int documentId, Map<String, Integer> frequencies) {
        Map<String, Float> weights = calculations.preNormalization(frequencies);

        weights.forEach((term, weight) -> {
            dummyTerm.setTerm(term);

            List<DocumentWithInfo<V>> postingList = invertedIndex.get(dummyTerm);

            if (postingList == null) {
                postingList = new LinkedList<>();
                invertedIndex.put(new TermWithInfo<>(term, .0f), postingList);
            }

            postingList.add(
                createDocument(
                    documentId,
                    createDocumentWeight(
                        term,
                        calculations.applyNormalization(weight)
                    )
                )
            );
        });
    }

    public abstract DocumentWithInfo<V> createDocument(int documentId, V weight);

    public abstract V createDocumentWeight(String term, float weight);

}
