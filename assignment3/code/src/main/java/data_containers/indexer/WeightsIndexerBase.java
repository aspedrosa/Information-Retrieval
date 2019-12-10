package data_containers.indexer;

import data_containers.indexer.post_indexing_actions.CalculateIDFAction;
import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoWithIDF;
import data_containers.indexer.weights_calculation.indexing.CalculationsBase;

import java.util.Map;

/**
 * Specific type of indexer where documents
 *  hold ranking weights
 *
 * @param <D> type of the document
 */
public abstract class WeightsIndexerBase<D extends Document<Float>>
    extends BaseIndexer<
    String, Float, D, TermInfoWithIDF<Float, D>> {

    protected CalculationsBase calculations;

    /**
     * Main constructor
     */
    public WeightsIndexerBase(CalculationsBase calculations) {
        super(new CalculateIDFAction<>());
        this.calculations = calculations;
    }

    /**
     * Constructor used to create new indexer while searching
     *
     * @param loadedIndex inverted index loaded
     */
    protected WeightsIndexerBase(
        CalculationsBase calculations,
        Map<String, TermInfoWithIDF<Float, D>> loadedIndex) {
        super(new CalculateIDFAction<>(), loadedIndex);
        this.calculations = calculations;
    }

    @Override
    protected final void insertDocument(int documentId, Map<String, Integer> frequencies) {
        Map<String, Float> weights = calculations.calculateWeights(frequencies);

        weights.forEach((term, weight) -> {
            TermInfoWithIDF<Float, D> termInfo = invertedIndex.get(term);

            if (termInfo == null) {
                termInfo = new TermInfoWithIDF<>();
                invertedIndex.put(term, termInfo);
            }

            termInfo.addToPostingList(
                createDocument(
                    documentId,
                    calculations.applyNormalization(weight),
                    term
                )
            );
        });
    }

    /**
     * Indexers implementations of this class should
     *  implement this method to create their specific document
     *
     * @param weight final document weight (with normalization)
     * @param term some indexer might need to know to which term
     *  is the document associated to create the object
     */
    public abstract D createDocument(int documentId, float weight, String term);

}
