package mains.search;

import data_containers.indexer.weights_calculation.searching.LTC;
import io.data_containers.loaders.bulk_load.document_registry.LinesLoader;
import io.data_containers.loaders.bulk_load.indexer.WeightsAndPositionsIndexerLoader;
import io.data_containers.loaders.bulk_load.indexer.WeightsIndexerLoader;
import io.metadata.BinaryMetadataManager;
import io.metadata.MetadataManager;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import searcher.Evaluation;
import searcher.Searcher;
import tokenizer.AdvancedTokenizer;
import tokenizer.BaseTokenizer;
import tokenizer.linguistic_rules.LinguisticRule;
import tokenizer.linguistic_rules.MinLengthRule;
import tokenizer.linguistic_rules.SnowballStemmerRule;
import tokenizer.linguistic_rules.StopWordsRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class containing the main method for the search pipeline
 */
public class SearchingMain {

    /**
     * Application starting point
     *
     * Execution flow:
     * <ul>
     *  <li>1. Parse program options and arguments</li>
     *  <li>2. Loads the metadata file</li>
     *  <li>3. Instantiate the tokenizer</li>
     *  <li>4. Calculate the amount of indexers and document registries possible to hold in memory</li>
     *  <li>5. Instantiate the Searcher class</li>
     *  <li>6. Responds the queries present on the queries file</li>
     *  <li>7. Print average of several metrics</li>
     * </ul>
     *
     * Exit codes:
     * <ul>
     *  <li>0: program executed normally without errors</li>
     *  <li>1: errors occurred related to program options or arguments</li>
     *  <li>2: errors occurred related to IO</li>
     * </ul>
     *
     * @param args program options and arguments
     */
    public static void main(String[] args) {
        Namespace parsedArgs = parseProgramArguments(args);

        String dataFolder = parsedArgs.getString("dataFolder") + "/";
        String docRegsFolder = dataFolder + "documentRegistry/";
        String indexersFolder = dataFolder + "indexer/";
        MetadataManager metadataManager = new BinaryMetadataManager(dataFolder + "METADATA");

        TreeMap<Integer, String> docRegMetadata = new TreeMap<>();
        TreeMap<String, String> indexerMetadata = new TreeMap<>();

        // load metadata
        try {
            metadataManager.loadMetadata(docRegMetadata, indexerMetadata);
        } catch (IOException e) {
            System.err.println("ERROR while loading metadata");
            e.printStackTrace();
            System.exit(2);
        }

        // create an advanced tokenizer
        Set<String> stopWords = mains.indexing.IndexingMain.readStopWordsFile(parsedArgs.getString("stopWordsFilename"));

        List<LinguisticRule> rules = new ArrayList<>(3);
        rules.add(new StopWordsRule(stopWords));
        rules.add(new SnowballStemmerRule());
        rules.add(new MinLengthRule(3));

        BaseTokenizer tokenizer = new AdvancedTokenizer(rules);

        // calculate the maximum the amount of indexers and document registries to hold in memory
        int maxIndexersInMemory, maxDocRegsInMemory;
        {System.gc();
        Runtime runtime = Runtime.getRuntime();
        long availableMem = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
        long usableMem = (long) Math.floor(availableMem * parsedArgs.getFloat("maxLoadFactor"));
        long memForIndexers = (long) Math.floor(usableMem * parsedArgs.getFloat("factorForIndexers"));
        long memForDocRegs = (long) Math.floor(usableMem - memForIndexers);
        long memPerIndexer = parsedArgs.getInt("indexersSize") * 1024 * 1024;
        long memPerDocReg = parsedArgs.getInt("docRegsSize") * 1024 * 1024;
        maxIndexersInMemory = (int) Math.floorDiv(memForIndexers, memPerIndexer) - 1;
        maxDocRegsInMemory = (int) Math.floorDiv(memForDocRegs, memPerDocReg) - 1;}

        int K = parsedArgs.getInt("K");

        // instantiate the searcher class
        Searcher searcher;
        if (parsedArgs.getBoolean("useWeightsAndPositionsIndexer")) {
            searcher = new Searcher(
                tokenizer,
                new LTC(),
                docRegMetadata,
                indexerMetadata,
                new LinesLoader(docRegsFolder),
                new WeightsAndPositionsIndexerLoader(indexersFolder),
                maxDocRegsInMemory,
                maxIndexersInMemory,
                K
            );
        }
        else {
            searcher = new Searcher(
                tokenizer,
                new LTC(),
                docRegMetadata,
                indexerMetadata,
                new LinesLoader(docRegsFolder),
                new WeightsIndexerLoader(indexersFolder),
                maxDocRegsInMemory,
                maxIndexersInMemory,
                K
            );
        }

        Map<Integer, Map<String, Integer>> relevances = new HashMap<>();

        try {
            Files.lines(Paths.get(parsedArgs.getString("queriesRelevanceFile"))).forEach(line -> {
                String[] fields = line.split("\\s+");
                int queryId = Integer.parseInt(fields[0]);
                String identifier = fields[1];
                int relevance = Integer.parseInt(fields[2]);

                if (!relevances.containsKey(queryId)) {
                    relevances.put(queryId, new HashMap<>());
                }

                relevances.get(queryId).put(identifier, relevance);
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        Evaluation evaluation = new Evaluation(relevances);

        long begin = System.currentTimeMillis();

        // process queries file
        try {
            Files.lines(Paths.get(parsedArgs.getString("queriesFile"))).forEach(line -> {
                int queryId = Integer.parseInt(line.substring(0, 2).trim());
                String query = line.substring(2);

                System.out.println(queryId);
                evaluation.evaluate(queryId, searcher.queryIndex(query));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        double elapsedTime = (double) (System.currentTimeMillis() - begin);

        System.out.println();
        evaluation.printMetricsAverage();

        System.out.println();
        System.out.printf("Query latency: %f\n", elapsedTime / 1000 / evaluation.queryCount);
        System.out.printf("Query throughput: %f\n", evaluation.queryCount / (elapsedTime / 1000));
    }

    /**
     * Defines program's arguments, options, help messages and
     *  parses the received arguments
     *
     * @param args main arguments
     * @return a namespace with the parsed arguments
     */
    private static Namespace parseProgramArguments(String[] args) {
        ArgumentParser argsParser = ArgumentParsers
            .newFor("Main")
            .build()
            .description(
                "Responds to a set of queries, present on a \"queries.txt\" files," +
                " using a ranked retrieval searcher"
            );

        argsParser
            .addArgument("stopWordsFilename")
            .type(String.class)
            .action(Arguments.store())
            .help("path to file containing the stop words");

        argsParser
            .addArgument("dataFolder")
            .type(String.class)
            .help("path to folder where the it is stored" +
                  " the index, document Registry and METADATA files");

        argsParser
            .addArgument("queriesFile")
            .type(String.class)
            .help("Path to the file containing the queries to respond to." +
                  " Format <queryId>\\t<query>");

        argsParser
            .addArgument("queriesRelevanceFile")
            .type(String.class)
            .help("Path to the file containing the queries relevance of the queries file." +
                " Format <queryId>\\t<identifier> <relevance>");

        argsParser
            .addArgument("-k")
            .dest("K")
            .type(Integer.class)
            .action(Arguments.store())
            .setDefault(10)
            .help("Maximum amount of documents to be returned by the Searcher." +
                  " Default 10");

        argsParser
            .addArgument("-p")
            .dest("useWeightsAndPositionsIndexer")
            .action(Arguments.storeTrue())
            .help("Uses the indexer that calculates weight terms and stores positions");

        argsParser
            .addArgument("--max-load-factor")
            .dest("maxLoadFactor")
            .type(Float.class)
            .action(Arguments.store())
            .setDefault(0.8f)
            .help("Max factor of memory usage during the execution of the" +
                  " program. Default 0.8");

        argsParser
            .addArgument("--memory-factor-to-indexers")
            .dest("factorForIndexers")
            .type(Float.class)
            .action(Arguments.store())
            .setDefault(0.8f)
            .help("Max factor of usable memory, after applied the" +
                  " maxLoadFactor, to be used to hold in memory indexers." +
                  " Default 0.8");

        argsParser
            .addArgument("--indexers-size")
            .dest("indexersSize")
            .type(Integer.class)
            .action(Arguments.store())
            .setDefault(25)
            .help("Estimated size of a indexer size in memory in MB" +
                  " Should be the same value used during indexing. Default 50");

        argsParser
            .addArgument("--doc-regs-size")
            .dest("docRegsSize")
            .type(Integer.class)
            .action(Arguments.store())
            .setDefault(20)
            .help("Estimated size of a document registry file in memory in MB." +
                  " Should be the same value used during indexing. Default 50");

        Namespace parsedArgs = null;
        try {
            parsedArgs = argsParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argsParser.handleError(e);
            System.exit(1);
        }

        for (String varName : new String[] {"maxLoadFactor", "factorForIndexers"}) {
            Float varValue = parsedArgs.getFloat(varName);
            if (varValue != null && (varValue < 0 || varValue > 1)) {
                System.err.println("ERROR " + varName + " should be a floating point" +
                    " between 0 and 1");
                System.exit(1);
            }
        }

        for (String varName : new String[] {"K", "indexersSize", "docRegsSize"}) {
            Integer varValue = parsedArgs.getInt(varName);
            if (varValue != null && varValue <= 0) {
                System.err.println("ERROR " + varName + " should be an integer" +
                    " greater than 0");
                System.exit(1);
            }
        }

        return parsedArgs;
    }

}
