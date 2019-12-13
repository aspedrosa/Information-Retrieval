package searcher;

import data_containers.DocumentRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to calculate the following
 * evaluation metrics
 */
public class Evaluation {

    /**
     * {<queryId> : {<identifier> : <relevance>}}
     */
    private Map<Integer, Map<Integer, Integer>> queryRelevance;

    public int queryCount;

    double cumulativePrecision;

    double cumulativeRecall;

    double cumulativeFMeasure;

    double cumulativeAveragePrecision;

    double cumulativeNDCG;

    public Evaluation(String queriesRelevanceFile) throws IOException {
        queryRelevance = new HashMap<>();

        Files.lines(Paths.get(queriesRelevanceFile)).forEach(line -> {
            String[] fields = line.split("\\s+");

            int queryId = Integer.parseInt(fields[0]);
            int identifier = Integer.parseInt(fields[1]);
            int relevance = -Integer.parseInt(fields[2]) + 3; // assuming file has a 1-3 inverted scale, convert to 0-2

            if (!queryRelevance.containsKey(queryId)) {
                queryRelevance.put(queryId, new HashMap<>());
            }

            queryRelevance.get(queryId).put(identifier, relevance);
        });

        queryCount = 0;
        cumulativePrecision = cumulativeRecall = cumulativeFMeasure = cumulativeAveragePrecision = cumulativeNDCG = 0;
    }

    /**
     * Calculate metrics of the results according to the queries relevance information
     */
    public void evaluate(int queryId, List<Integer> queryResults) {
        evaluate(queryId, queryResults, false);
    }

    /**
     * Same as previous method but can print the metrics results for the query
     */
    public void evaluate(int queryId, List<Integer> queryResults, boolean printMetrics) {
        queryCount++;

        double precision, recall, ndcg;
        if (queryResults.isEmpty()) {
            precision = 0;
            recall = 0;
            ndcg = 0;
        }
        else {
            int[] TP_FP_TN_FN = calculateTP_FP_TN_FN(queryResults, queryId);
            int tp = TP_FP_TN_FN[0];
            int fp = TP_FP_TN_FN[1];
            int tn = TP_FP_TN_FN[2];
            int fn = TP_FP_TN_FN[3];

            precision = calculatePrecision(tp, fp);
            cumulativePrecision += precision;

            recall = calculateRecall(tp, fn);
            cumulativeRecall += recall;

            ndcg = calculateNDCG(queryId, queryResults);
            cumulativeNDCG += ndcg;
        }

        double fmeasure = calculateFMeasure(precision, recall);
        cumulativeFMeasure += fmeasure;

        double averagePrecision = calculateAveragePrecision(queryResults, queryId);
        cumulativeAveragePrecision += averagePrecision;

        if (printMetrics) {
            System.out.printf("Precision:         %f\n", precision);
            System.out.printf("Recall:            %f\n", recall);
            System.out.printf("F Measure:         %f\n", fmeasure);
            System.out.printf("Average Precision: %f\n", averagePrecision);
            System.out.printf("NGDC:              %f\n", ndcg);
        }
    }

    /**
     * Calculate the average of the several calculated metrics
     */
    public void printMetricsAverage() {
        System.out.printf("Mean Precision:         %f\n", cumulativePrecision / queryCount);
        System.out.printf("Mean Recall:            %f\n", cumulativeRecall / queryCount);
        System.out.printf("Mean F Measure:         %f\n", cumulativeFMeasure / queryCount);
        System.out.printf("Mean Average Precision: %f\n", cumulativeAveragePrecision / queryCount);
        System.out.printf("Mean NGDC:              %f\n", cumulativeNDCG / queryCount);
    }

    private int[] calculateTP_FP_TN_FN(List<Integer> queryResults, int queryId) {
        int truePositives = 0;
        int falseNegatives = 0;
        for (int relevantDocument : queryRelevance.get(queryId).keySet()) {
            if (queryResults.contains(relevantDocument)) {
                truePositives++;
            }
            else {
                falseNegatives++;
            }
        }

        int falsePositives = queryResults.size() - truePositives;
        int trueNegatives =
            DocumentRegistry.getNumberOfDocuments()
                - falseNegatives - falsePositives
                + truePositives;

        return new int[] {truePositives, falsePositives, trueNegatives, falseNegatives};
    }

    private double calculatePrecision(int tp, int fp) {
        if (tp + fp == 0) {
            return 0;
        }

        return (double) tp / (tp + fp);
    }

    private double calculateRecall(int tp, int fn) {
        if (tp + fn == 0) {
            return 0;
        }

        return (double) tp / (tp + fn);
    }

    private double calculateFMeasure(double precision, double recall) {
        if (precision == 0 && recall == 0) {
            return 0;
        }

        return (2 * recall * precision) / (recall + precision);
    }

    private double calculateAveragePrecision(List<Integer> queryResults, int queryId) {
        int docsCount = 0;
        int relevantDocsCount = 0;
        double cumulativePrecision = 0;
        for (Integer identifier : queryResults) {
            docsCount++;

            if (queryRelevance.get(queryId).containsKey(identifier)) {
                relevantDocsCount++;

                cumulativePrecision += (double) relevantDocsCount / docsCount;
            }
        }

        if (relevantDocsCount == 0) {
            return 0;
        }

        return cumulativePrecision / relevantDocsCount;
    }

    private double calculateNDCG(int queryId, List<Integer> queryResults) {
        List<Integer> relevances = queryResults
            .stream()
            .map(identifier -> {
                Integer relevance = queryRelevance.get(queryId).get(identifier);

                return relevance == null ? 0 : relevance;
            })
            .collect(Collectors.toList());

        List<Integer> idealOrder = relevances
            .stream()
            .sorted((rel1, rel2) -> -rel1.compareTo(rel2))
            .collect(Collectors.toList());

        double idealDCG = idealOrder.get(0);
        for (int i = 2; i < idealOrder.size() + 1; i++) {
            idealDCG += (double) idealOrder.get(i - 1) / (Math.log(i) / Math.log(2));
        }

        if (idealDCG == 0) {
            return 0;
        }

        double realDCG = relevances.get(0);
        for (int i = 2; i < relevances.size() + 1; i++) {
            realDCG += (double) relevances.get(i - 1) / (Math.log(i) / Math.log(2));
        }

        return realDCG / idealDCG;
    }

}
