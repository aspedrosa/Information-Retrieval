package mains.search;

import data_containers.DocumentRegistry;
import data_containers.indexer.WeightsAndPositionsIndexer;
import data_containers.indexer.WeightsIndexer;
import data_containers.indexer.weights_calculation.searching.LTC;
import io.data_containers.loaders.bulk_load.DocRegBinaryBulkLoader;
import io.data_containers.loaders.bulk_load.WeightsAndPositionsIndexerLoader;
import io.data_containers.loaders.bulk_load.WeightsIndexerLoader;
import io.metadata.BinaryMetadataManager;
import io.metadata.MetadataManager;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
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
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class containing the main method
 */
public class Main {

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
     *  <li>6. Responds the queries present on the "queries.txt" file</li>
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

        try {
            metadataManager.loadMetadata(docRegMetadata, indexerMetadata);
        } catch (IOException e) {
            System.err.println("ERROR while loading metadata");
            e.printStackTrace();
            System.exit(2);
        }

        // create an advanced tokenizer
        Set<String> stopWords = mains.indexing.Main.readStopWordsFile(parsedArgs.getString("stopWordsFilename"));

        List<LinguisticRule> rules = new ArrayList<>(3);
        rules.add(new StopWordsRule(stopWords));
        rules.add(new SnowballStemmerRule());
        rules.add(new MinLengthRule(3));

        BaseTokenizer tokenizer = new AdvancedTokenizer(rules);

        int maxIndexersInMemory, maxDocRegsInMemory;
        {System.gc();
        Runtime runtime = Runtime.getRuntime();
        long availableMem = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
        long usableMem = (long) Math.floor(availableMem * parsedArgs.getFloat("maxLoadFactor"));
        long memForIndexers = (long) Math.floor(usableMem * parsedArgs.getFloat("factorForIndexers"));
        long memForDocRegs = (long) Math.floor(usableMem - memForIndexers);
        long memPerIndexer = parsedArgs.getInt("indexersSize") * 1024 * 1024;
        long memPerDocReg = parsedArgs.getInt("docRegsSize") * 1024 * 1024;
        maxIndexersInMemory = (int) Math.floorDiv(memForIndexers, memPerIndexer);
        maxDocRegsInMemory = (int) Math.floorDiv(memForDocRegs, memPerDocReg);}

        int K = parsedArgs.getInt("K");

        Searcher searcher;
        if (parsedArgs.getBoolean("useWeightsAndPositionsIndexer")) {
            WeightsAndPositionsIndexer tmp = new WeightsAndPositionsIndexer(null);

            searcher = new Searcher(
                tokenizer,
                new LTC(),
                docRegMetadata,
                indexerMetadata,
                new DocRegBinaryBulkLoader(docRegsFolder),
                new WeightsAndPositionsIndexerLoader(indexersFolder),
                tmp,
                maxDocRegsInMemory,
                maxIndexersInMemory,
                K
            );
        }
        else {
            WeightsIndexer tmp = new WeightsIndexer(null);

            searcher = new Searcher(
                tokenizer,
                new LTC(),
                docRegMetadata,
                indexerMetadata,
                new DocRegBinaryBulkLoader(docRegsFolder),
                new WeightsIndexerLoader(indexersFolder),
                tmp,
                maxDocRegsInMemory,
                maxIndexersInMemory,
                K
            );
        }

        long begin = System.currentTimeMillis();

        try {
            Files.lines(Paths.get("queries.txt")).forEach(line -> {
                int queryId = Integer.parseInt(line.substring(0, 2).trim());
                String query = line.substring(2);

                System.out.printf("%2d - %d\n", queryId, searcher.queryIndex(query).size());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(System.currentTimeMillis() - begin);
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
            .setDefault(50)
            .help("Estimated size of a indexer size in memory in MB" +
                  " Should be the same value used during indexing. Default 50");

        argsParser
            .addArgument("--doc-regs-size")
            .dest("docRegsSize")
            .type(Integer.class)
            .action(Arguments.store())
            .setDefault(50)
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
