package mains.search;

import data_containers.indexer.WeightsAndPositionsIndexer;
import data_containers.indexer.WeightsIndexer;
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
import tokenizer.BaseTokenizer;
import tokenizer.SimpleTokenizer;
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
     *  <li>2. Instantiate both an indexer and tokenizer</li>
     *  <li>3. Create a CorpusReader</li>
     *  <li>4. Create and execute a pipeline</li>
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

        BaseTokenizer tokenizer = new SimpleTokenizer(rules);

        int maxIndexersInMemory, maxDocRegsInMemory;
        {System.gc();
        Runtime runtime = Runtime.getRuntime();
        long availableMem = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
        long usableMem = (long) Math.floor(availableMem * parsedArgs.getFloat("maxLoadFactor"));
        long memForIndexers = (long) Math.floor(usableMem * parsedArgs.getFloat("factorForIndexers"));
        long memForDocRegs = (long) Math.floor(usableMem - memForIndexers);
        long memPerIndexerFile = parsedArgs.getInt("indexFileSize") * 1024 * 1024;
        long memPerDocRegFile = parsedArgs.getInt("docRegFileSize") * 1024 * 1024;
        maxIndexersInMemory = (int) Math.floorDiv(memForIndexers, memPerIndexerFile);
        maxDocRegsInMemory = (int) Math.floorDiv(memForDocRegs, memPerDocRegFile);
        }

        Searcher searcher;
        if (parsedArgs.getBoolean("useWeightsAndPositionsIndexer")) {
            WeightsAndPositionsIndexer tmp = new WeightsAndPositionsIndexer(null);

            searcher = new Searcher(
                tokenizer,
                docRegMetadata,
                indexerMetadata,
                new DocRegBinaryBulkLoader(docRegsFolder),
                new WeightsAndPositionsIndexerLoader(indexersFolder),
                tmp,
                maxDocRegsInMemory,
                maxIndexersInMemory
            );
        }
        else {
            WeightsIndexer tmp = new WeightsIndexer(null);

            searcher = new Searcher(
                tokenizer,
                docRegMetadata,
                indexerMetadata,
                new DocRegBinaryBulkLoader(docRegsFolder),
                new WeightsIndexerLoader(indexersFolder),
                tmp,
                maxDocRegsInMemory,
                maxIndexersInMemory
            );
        }

        long begin = System.currentTimeMillis();

        try {
            Files.lines(Paths.get("queries.txt")).forEach(query -> {
                searcher.queryIndex(query);
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
                "TODO"
            );

        argsParser
            .addArgument("stopWordsFilename")
            .type(String.class)
            .action(Arguments.store())
            .help("path to file containing the stop words");

        argsParser
            .addArgument("dataFolder")
            .type(String.class)
            .help("TODO");

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
            .help("TODO");

        argsParser
            .addArgument("--memory-factor-to-indexers")
            .dest("factorForIndexers")
            .type(Float.class)
            .action(Arguments.store())
            .setDefault(0.8f)
            .help("TODO");

        argsParser
            .addArgument("--index-file-size")
            .dest("indexFileSize")
            .type(Integer.class)
            .action(Arguments.store())
            .setDefault(100)
            .help("TODO");

        argsParser
            .addArgument("--doc-reg-file-size")
            .dest("docRegFileSize")
            .type(Integer.class)
            .action(Arguments.store())
            .setDefault(100)
            .help("TODO");

        Namespace parsedArgs = null;
        try {
            parsedArgs = argsParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argsParser.handleError(e);
            System.exit(1);
        }

        Float maxLoadFactor = parsedArgs.getFloat("maxLoadFactor");
        if (maxLoadFactor != null && (maxLoadFactor < 0 || maxLoadFactor > 1)) {
            System.err.println("ERROR maximum load factor should be a floating point" +
                " between 0 and 1");
            System.exit(1);
        }

        return parsedArgs;
    }

}
