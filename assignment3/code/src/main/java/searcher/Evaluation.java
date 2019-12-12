package searcher;

import java.io.File;
import java.util.*;
import java.lang.Math;

/**
 * Class calculate the following
 * evaluation and efficiency metrics
 */
public class Evaluation {

    /**
     *
     * Obtains the ids of queries listed
     *
     * @param filename
     * @return ids of queries
     */
    public List<String> docs(String filename){
        List<String> idQueries = new ArrayList<>();
        Scanner input = new Scanner(new File(filename));
        while (input.hasNextLine()){
            String[] line = input.nextLine.split("\t");
            if (!idQueries.contains(line[0])){
                idQueries.add(line[0])
            }
        }
        return idQueries;
    }

    /**
     *
     * @param filename
     * @return list of revelance scores
     */
    public Map<String, ArrayList<String>> loadTruth(String filename){
        Map<String, Array<ArrayList<String>>> queriesTruth = new HashMap<>();
        Scanner input = new Scanner(new File(filename));
        while (input.hasNextLine()){
            String[] line = input.nextLine.split("\t");
            ArrayList<String> info = new ArrayList<>();
            info.add(line[1]); info.add(line[2]);

            ArrayList<ArrayList<String>> infos = new ArrayList<>();
            if (!queriesTruth.contains(line[0])){
                infos.add(info);
                queriesTruth.put(line[0], infos);
            }
            else {
                infos = queriesTruth.get(line[0]);
                infos.add(info);
                queriesTruth.put(line[0], infos);
            }
        }
        return queriesTruth;
    }

    /**
     *
     * @param docs
     * @param results
     * @return mean precision, recall and fmeasure
     */
    private ArrayList<Double> calculateMeanPrecision_Recall_FMeasure(ArrayList<String> docs, ArrayList<String> results) {
        double tp = 0, fp = 0, fn = 0;

        for (String p: results) {
            if (docs.contains(p)) tp += 1;
            else fp += 1;
        }

        for (String t: docs){
            if (!results.contains(t)) fn += 1;
        }

        double P = (tp+fp) != 0 ? P = tp/(tp+fp) : 0.0;
        double R = (tp+fn) != 0 ? R = tp/(tp+fn) : 0.0;
        double Fmeasure = (R+P) != 0 ? (2*R*P)/(R+P) : 0.0;

        ArrayList<Double> calc = new ArrayList<>();
        calc.add(P); calc.add(R); calc.add(Fmeasure);

        return calc;
    }

    /**
     *
     * @param docs
     * @param results
     * @param rank
     * @return mean precision with rank 10
     */
    private double calculateMeanPrecisionAtRank10(ArrayList<String> docs, ArrayList<String> results, int rank) {
        double pSum = 0;
        double count = 0;

        for (int i = 0; i < rank; i++){
            if (docs.contains(results.get(i))){
                count += 1;
                ArrayList<Double> calc = calculateMeanPrecision_Recall_FMeasure(docs, results.subList(0, i));
                pSum += calc.get(0);
            }
        }

        return count != 0 ? pSum/count : 0.0;
    }

    /**
     *
     * @param truth
     * @param results
     * @return ndcg
     */
    private double calculateNDCG(ArrayList<ArrayList<String>> truth, ArrayList<String> results) {
        double ideal = 0;
        double actual = 0;
        ArrayList<Double> resultsRevelance = new ArrayList<>();
        for(String r: results){
            boolean cond = false;
            for (ArrayList<String> t: truth){
                if (r.equals(t.get(0))){
                    cond = true;
                    resultsRevelance.add(t.get(1));
                }
            }
            if (!cond) resultsRevelance.add(0);
        }
        ArrayList<Double> idealRe = new ArrayList<>();
        idealRe.addAll(resultsRevelance);
        Collections.reverse(idealRe);
        for (int j = 0; j < idealRe.size(); j++){
            double denominator = j+1 > 1 ? Math.log(j+1)/Math.log(2) : 1;
            ideal += idealRe[j]/denominator;
            actual += resultsRevelance[j]/denominator;

        }
        return ideal != 0 ? actual/ideal : 0.0;
    }

    /**
     *
     * @param numbers
     * @return sum of list doubles
     */
    private double sum(ArrayList<Double> numbers){
        double result = 0;
        for (double d: numbers){
            result += d;
        }
        return d;
    }

    /**
     *
     * Calculate the metrics of documents
     *
     * @param PMIDs
     */
    public void results(List<String> PMIDs){
        List<String> queries = docs("queries.txt");
        Map<String, ArrayList<ArrayList<String>>> queriesTruth = loadTruth("queries.revelance.txt");
        ArrayList<Double> precisions = new ArrayList<>();
        ArrayList<Double> recalls = new ArrayList<>();
        ArrayList<Double> fmeasures = new ArrayList<>();
        ArrayList<Double> meanprecisons10 = new ArrayList<>();
        ArrayList<Double> ndcg = new ArrayList<>();

        for(String query: queries){
            ArrayList<ArrayList<String>> truth = queriesTruth.get(query);
            ArrayList<String> docsTrue = new ArrayList<>();
            for(ArrayList<String> x: truth){
                docsTrue.add(x.get(0));
            }

            ArrayList<Double> calc = calculateMeanPrecision_Recall_FMeasure(docsTrue, PMIDs);

            double mprecision10 = calculateMeanPrecisionAtRank10(docsTrue, PMIDs, 10);
            double calcNDCG = calculateNDCG(truth, PMIDs);

            precisions.add(calc.get(0));
            recalls.add(calc.get(1));
            fmeasures.add(calc.get(2));
            meanprecisons10.add(meanprecisons10);
            ndcg.add(calcNDCG);
        }
        avgPrecisions = sum(precisions)/(precisions.size())
        avgRecalls = sum(recalls)/(recalls.size())
        avgFmeasures = sum(fmeasures)/(fmeasures.size())
        avgPrecisions = sum(precisions)/(precisions.size())
        avgMeanPrecisions10 = sum(meanprecisons10)/(meanprecisons10.size())
        avgNDCG = sum(ndcgs)/(ndcgs.size())
    }

}
