package main;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import indexer.FrequencyIndexer;
import indexer.io.persisters.ObjectStreamPersister;
import indexer.io.persisters.OutputStreamPersister;
import indexer.io.persisters.strategies.FrequencyStrategy;
import indexer.io.persisters.strategies.WeightsAndPositionStrategy;
import indexer.io.persisters.strategies.WeightStrategy;
import indexer.post_indexing_actions.LNC_LTC_Weighting;
import indexer.structures.aux_structs.DocumentWeight;
import indexer.WeightsAndPositionsIndexer;
import indexer.WeightsIndexer;
import main.pipelines.Pipeline;
import main.pipelines.SPIMIPipeline;
import parsers.corpus.CorpusReader;
import parsers.corpus.ResolveByExtension;
import parsers.documents.TrecAsciiMedline2004DocParser;
import parsers.files.TrecAsciiMedline2004FileParser;
import tokenizer.AdvancedTokenizer;
import tokenizer.BaseTokenizer;
import tokenizer.linguistic_rules.LinguisticRule;
import tokenizer.linguistic_rules.MinLengthRule;
import tokenizer.linguistic_rules.SnowballStemmerRule;
import tokenizer.linguistic_rules.StopWordsRule;

import java.io.BufferedReader;
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
        TrecAsciiMedline2004DocParser.addFieldToSave("PMID");

        int entriesPerDocRegFile = parsedArgs.getInt("entriesPerDocRegFile");
        int termsPerFinalIndexFile = parsedArgs.getInt("termsPerFinalIndexFile");

        // create an advanced tokenizer
        Set<String> stopWords = readStopWordsFile(parsedArgs.getString("stopWordsFilename"));

        List<LinguisticRule> rules = new ArrayList<>(3);
        rules.add(new StopWordsRule(stopWords));
        rules.add(new SnowballStemmerRule());
        rules.add(new MinLengthRule(3));

        BaseTokenizer tokenizer = new AdvancedTokenizer(rules);

        System.out.println("Created the Advanced tokenizer");

        Pipeline pipeline;
        if (parsedArgs.getBoolean("useWeightsIndexer")) {
            WeightsIndexer<DocumentWeight> indexer = new WeightsIndexer<>(
                new LNC_LTC_Weighting<>()
            );
            System.out.println("Created weights indexer");

            pipeline = new SPIMIPipeline<>(
                tokenizer,
                indexer,
                corpusReader,
                new ObjectStreamPersister<>("documentRegistry", entriesPerDocRegFile),
                new OutputStreamPersister<>(
                    parsedArgs.getString("indexOutputFilename"),
                    termsPerFinalIndexFile,
                    new WeightStrategy()
                ),
                parsedArgs.getFloat("maxLoadFactor")
            );
        }
        else if (parsedArgs.getBoolean("useWeightsAndPositionsIndexer")) {
            WeightsAndPositionsIndexer indexer = new WeightsAndPositionsIndexer(
                new LNC_LTC_Weighting<>()
            );
            System.out.println("Created weights and positions indexer");

            pipeline = new SPIMIPipeline<>(
                tokenizer,
                indexer,
                corpusReader,
                new ObjectStreamPersister<>("documentRegistry", entriesPerDocRegFile),
                new OutputStreamPersister<>(
                    parsedArgs.getString("indexOutputFilename"),
                    termsPerFinalIndexFile,
                    new WeightsAndPositionStrategy()
                ),
                parsedArgs.getFloat("maxLoadFactor")
            );
        }
        else {
            FrequencyIndexer indexer = new FrequencyIndexer();
            System.out.println("Created frequency indexer");

            pipeline = new SPIMIPipeline<>(
                tokenizer,
                indexer,
                corpusReader,
                new ObjectStreamPersister<>("documentRegistry", entriesPerDocRegFile),
                new OutputStreamPersister<>(
                    parsedArgs.getString("indexOutputFilename"),
                    termsPerFinalIndexFile,
                    new FrequencyStrategy()
                ),
                parsedArgs.getFloat("maxLoadFactor")
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
    private static Set<String> readStopWordsFile(String filePath) {
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
                " using a frequency indexer by default and stores the index to a file"
            );

        argsParser
            .addArgument("stopWordsFilename")
            .type(String.class)
            .action(Arguments.store())
            .help("path to file containing the stop words");

        argsParser
            .addArgument("corpusFolder")
            .type(String.class)
            .help("Path to folder containing files with documents to index");

        argsParser
            .addArgument("indexOutputFilename")
            .type(String.class)
            .help("Name of the file to which the index will be stored");

        argsParser
            .addArgument("-w")
            .dest("useWeightsIndexer")
            .action(Arguments.storeTrue())
            .help("Uses the indexer that calculates weight terms");

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
            .addArgument("--terms-per-final-index-file")
            .dest("termsPerFinalIndexFile")
            .type(Integer.class)
            .action(Arguments.store())
            .setDefault(100000)
            .help("number of terms per final index file. If" +
                " a number lower than 1 is received, all the index" +
                " term will be stored on the same file. Default 100000");

        argsParser
            .addArgument("--entries-per-doc-reg-file")
            .dest("entriesPerDocRegFile")
            .type(Integer.class)
            .action(Arguments.store())
            .setDefault(1000000)
            .help("number of entries per document registry file. If" +
                " a number lower than 1 is received, all the entries" +
                " will be stored on the same file. Default 1000000");

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

        if (parsedArgs.getBoolean("useWeightsIndexer") &&
            parsedArgs.getBoolean("useWeightsAndPositionsIndexer")) {
            System.err.println("Define only one or none type of indexer (Default FrequencyIndexer)");
            System.exit(1);
        }

        return parsedArgs;
    }
}
