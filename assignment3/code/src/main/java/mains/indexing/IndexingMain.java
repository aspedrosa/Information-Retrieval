package mains.indexing;

import io.data_containers.persisters.strategies.DocumentRegistryStrategy;
import io.metadata.BinaryMetadataManager;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import io.data_containers.persisters.OutputStreamPersister;
import io.data_containers.persisters.strategies.WeightsAndPositionStrategy;
import io.data_containers.persisters.strategies.WeightStrategy;
import data_containers.indexer.WeightsAndPositionsIndexer;
import data_containers.indexer.WeightsIndexer;
import data_containers.indexer.weights_calculation.indexing.LNC;
import mains.indexing.pipelines.Pipeline;
import mains.indexing.pipelines.SPIMIPipeline;
import parsers.corpus.CorpusReader;
import parsers.corpus.ResolveByExtension;
import parsers.documents.TrecAsciiMedline2004DocParser;
import parsers.files.TrecAsciiMedline2004FileParser;
import tokenizer.BaseTokenizer;
import tokenizer.AdvancedTokenizer;
import tokenizer.linguistic_rules.LinguisticRule;
import tokenizer.linguistic_rules.MinLengthRule;
import tokenizer.linguistic_rules.SnowballStemmerRule;
import tokenizer.linguistic_rules.StopWordsRule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class containing the main method for the indexing pipeline
 */
public class IndexingMain {

    /**
     * Application starting point
     *
     * Execution flow:
     * <ul>
     *  <li>1. Parse program options and arguments</li>
     *  <li>2. Instantiate both an indexer and tokenizer</li>
     *  <li>3. Create a CorpusReader</li>
     *  <li>4. Create data folders</li>
     *  <li>5. Create and execute a pipeline</li>
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

        // create an iterator over the corpus folder content
        Iterator<Path> corpusFolder = null;
        try {
            corpusFolder = Files.list(Paths.get(parsedArgs.getString("corpusFolder"))).iterator();
        } catch (IOException e) {
            System.err.println("ERROR listing the corpus folder\n");
            e.printStackTrace();
            System.exit(2);
        }

        // define strategy to choose the right file parser for each file
        ResolveByExtension fileParserResolver = new ResolveByExtension();
        fileParserResolver.addParser("gz", TrecAsciiMedline2004FileParser.class);

        CorpusReader corpusReader = new CorpusReader(corpusFolder, fileParserResolver);

        // set relevant fields to parse from the TrecAsciiMedline2004's documents
        TrecAsciiMedline2004DocParser.addFieldToSave("TI");
        TrecAsciiMedline2004DocParser.addFieldToSave("AB");
        TrecAsciiMedline2004DocParser.addFieldToSave("PMID");

        int maxDocRegSize = parsedArgs.getInt("maxDocRegsSize") * 1024 * 1024;
        int maxIndexSize = parsedArgs.getInt("maxIndexersSize") * 1024 * 1024;
        float maxLoadFactor = parsedArgs.getFloat("maxLoadFactor");

        // create an advanced tokenizer
        Set<String> stopWords = readStopWordsFile(parsedArgs.getString("stopWordsFilename"));

        List<LinguisticRule> rules = new ArrayList<>(3);
        rules.add(new StopWordsRule(stopWords));
        rules.add(new SnowballStemmerRule());
        rules.add(new MinLengthRule(3));

        BaseTokenizer tokenizer = new AdvancedTokenizer(rules);

        System.out.println("Created the Advanced tokenizer");

        // create folders to save the data
        String dataFolder = parsedArgs.getString("dataFolder") + "/";
        String metadataFile = dataFolder + "METADATA";
        String documentRegistryFolder = dataFolder + "documentRegistry/";
        String indexerFolder = dataFolder + "indexer/";
        String tmpFolder = dataFolder + "tmp/";
        String[] dataFoldersName = new String[] {documentRegistryFolder, indexerFolder, tmpFolder};
        for (String folder : dataFoldersName) {
            File subDataFolder = new File(folder);
            subDataFolder.mkdirs();
        }

        // warn if data folders aren't empty
        for (String folder : dataFoldersName) {
            if (folder.contains("tmp")){
                continue;
            }

            try {
                if (Files.list(Paths.get(folder)).count() > 0) {
                    System.err.println("WARNING data folder " + folder + " not empty");
                }
            } catch (IOException e) {
                // on the previously code block this are created so this exception can't happen
            }
        }

        Pipeline pipeline;
        if (parsedArgs.getBoolean("useWeightsAndPositionsIndexer")) {
            WeightsAndPositionsIndexer indexer = new WeightsAndPositionsIndexer(
                new LNC()
            );
            System.out.println("Created weights and positions indexer");

            pipeline = new SPIMIPipeline<>(
                tokenizer,
                indexer,
                corpusReader,
                tmpFolder,
                new OutputStreamPersister<>(
                    documentRegistryFolder,
                    maxDocRegSize,
                    new DocumentRegistryStrategy()
                ),
                new OutputStreamPersister<>(
                    indexerFolder,
                    maxIndexSize,
                    new WeightsAndPositionStrategy()
                ),
                new BinaryMetadataManager(metadataFile),
                maxLoadFactor
            );

        }
        else {
            WeightsIndexer indexer = new WeightsIndexer(
                new LNC()
            );
            System.out.println("Created weights indexer");

            pipeline = new SPIMIPipeline<>(
                tokenizer,
                indexer,
                corpusReader,
                tmpFolder,
                new OutputStreamPersister<>(
                    documentRegistryFolder,
                    maxDocRegSize,
                    true,
                    new DocumentRegistryStrategy()
                ),
                new OutputStreamPersister<>(
                    indexerFolder,
                    maxIndexSize,
                    new WeightStrategy()
                ),
                new BinaryMetadataManager(metadataFile),
                maxLoadFactor
            );
        }

        pipeline.execute();
    }

    /**
     * Reads a file containing stop words and builds a set with them
     *
     * @param filePath path to file containing the stop words
     * @return stop words
     */
    public static Set<String> readStopWordsFile(String filePath) {
        InputStreamReader input = null;
        try {
            input = new InputStreamReader(
                new FileInputStream(filePath)
            );
        }
        catch (FileNotFoundException e) {
            System.err.println("ERROR while opening the stop words file");
            e.printStackTrace();
            System.exit(2);
        }

        BufferedReader reader = new BufferedReader(input);

        Set<String> stopWords = new HashSet<>();
        try {
            while (reader.ready()) {
                stopWords.add(reader.readLine());
            }
        }
        catch (IOException e) {
            System.err.println("ERROR while reading from the stop words file\n");
            System.exit(2);
        }

        return stopWords;
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
                "Parses a set of files that contain documents, indexes those documents" +
                " using a indexer with weights by default and stores the index to a file"
            );

        argsParser
            .addArgument("stopWordsFilename")
            .type(String.class)
            .action(Arguments.store())
            .help("Path to file containing the stop words");

        argsParser
            .addArgument("corpusFolder")
            .type(String.class)
            .help("Path to folder containing files with documents to index");

        argsParser
            .addArgument("dataFolder")
            .type(String.class)
            .help("Path to folder to store indexers, document registries and" +
                  "the METADATA file");

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
            .help("defines the maximum load factor that the memory can reach while" +
                " indexing or merging during the SPIMI algorithm. " +
                "Should be a number between 0 and 1. Default 0.80");

        argsParser
            .addArgument("--max-indexers-size")
            .dest("maxIndexersSize")
            .type(Integer.class)
            .action(Arguments.store())
            .setDefault(50)
            .help("Maximum size in memory for a indexer in MB. Default 50");

        argsParser
            .addArgument("--max-doc-regs-size")
            .dest("maxDocRegsSize")
            .type(Integer.class)
            .action(Arguments.store())
            .setDefault(50)
            .help("Maximum size in memory for a document registry in MB. Default 50");

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

        for (String varName : new String[] {"maxIndexersSize", "maxDocRegsSize"}) {
            Integer varValue = parsedArgs.getInt(varName);
            if (varValue != null && varValue <= 0) {
                System.err.println("ERROR " + varName + " should be a integer" +
                    " greater than 0");
            }
        }

        return parsedArgs;
    }
}
