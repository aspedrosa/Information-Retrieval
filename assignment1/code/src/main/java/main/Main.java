package main;

import indexer.persisters.IndexThenDocIdent;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import parsers.corpus.CorpusReader;
import parsers.corpus.ResolveByExtension;
import parsers.documents.Document;
import parsers.documents.TrecAsciiMedline2004DocParser;
import parsers.files.FileParser;
import parsers.files.TrecAsciiMedline2004FileParser;
import indexer.FrequencyIndexer;
import indexer.structures.DocumentWithFrequency;
import indexer.structures.SimpleTerm;
import tokenizer.AdvancedTokenizer;
import tokenizer.BaseTokenizer;
import tokenizer.SimpleTokenizer;
import tokenizer.linguistic_rules.LinguisticRule;
import tokenizer.linguistic_rules.SnowballStemmerRule;
import tokenizer.linguistic_rules.StopWordsRule;

import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
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
     *  <li>4. Create the index while iterating over the corpus</li>
     *  <li>5. Write the index to disk</li>
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

        FileParser.setReaderBufferSize(parsedArgs.getInt("inputBufferSize"));

        FrequencyIndexer indexer = new FrequencyIndexer();
        System.out.println("Created the indexer");

        BaseTokenizer tokenizer;
        if (parsedArgs.getBoolean("useAdvancedTokenizer")) {
            Set<String> stopWords = readStopWordsFile(
                parsedArgs.getString("stopWordsFile"),
                parsedArgs.getInt("inputBufferSize")
            );

            List<LinguisticRule> rules = new LinkedList<>();
            rules.add(new SnowballStemmerRule());
            rules.add(new StopWordsRule(stopWords));

            tokenizer = new AdvancedTokenizer(rules);

            System.out.println("Created the Advanced tokenizer");
        }
        else {
            tokenizer = new SimpleTokenizer();
            System.out.println("Created the Simple tokenizer");
        }

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

        System.out.println("Started parsing the corpus");
        long begin = System.currentTimeMillis();

        for (FileParser fileParser : corpusReader) {
            for (Document document : fileParser) {
                List<String> terms = tokenizer.tokenizeDocument(document.getToTokenize());

                int docId = document.getId();
                indexer.registerDocument(docId, document.getIdentifier());

                if (!terms.isEmpty()) {
                    indexer.indexTerms(docId, terms);
                }
            }

            try {
                fileParser.close();
            } catch (IOException e) {
                System.err.println("ERROR closing file " + fileParser.getFilename() + "\n");
                e.printStackTrace();
            }
        }

        System.out.println("Finished parsing the corpus in " + (System.currentTimeMillis() - begin));

        BufferedOutputStream output = null;
        try {
            output = new BufferedOutputStream(
                new FileOutputStream(
                    parsedArgs.getString("indexOutputFilename")
                )
            );
        } catch (FileNotFoundException e) {
            System.err.println("ERROR error while opening file to write the index\n");
            System.exit(2);
        }

        System.out.println("Started writing index to disk");
        begin = System.currentTimeMillis();
        try {
            indexer.persist(output, new IndexThenDocIdent<>(
                new indexer.persisters.inverted_index.CSV<SimpleTerm, DocumentWithFrequency>() {
                    @Override
                    public String handleDocument(DocumentWithFrequency document) {
                        return String.format("%d:%d", document.getDocId(), document.getFrequency());
                    }
                },
                new indexer.persisters.document_identification.CSV(),
                "--\n"
            ));

            output.close();
        } catch (IOException e) {
            System.err.println("ERROR while writing the index to disk\n");
            e.printStackTrace();
            System.exit(2);
        }

        System.out.println("Finished storing index to disk in " + (System.currentTimeMillis() - begin));

        Assignment1Results.results(indexer.getInvertedIndex());
    }

    /**
     * Reads a file containing stop words and builds a set with them
     *
     * @param filePath path to file containing the stop words
     * @param inputBufferSize size for the buffer of the BufferedReader. Can be null
     * @return stop words
     */
    private static Set<String> readStopWordsFile(String filePath, Integer inputBufferSize) {
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

        BufferedReader reader;
        if (inputBufferSize == null) {
            reader = new BufferedReader(input);
        }
        else {
            reader = new BufferedReader(input, inputBufferSize);
        }

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
                " using an inverted index and store that index to a file"
            );

        argsParser
            .addArgument("corpusFolder")
            .type(String.class)
            .help("Path to folder containing files with documents to index");

        argsParser
            .addArgument("indexOutputFilename")
            .type(String.class)
            .help("Name of the file to which the index will be stored");

        argsParser
            .addArgument("-a")
            .dest("useAdvancedTokenizer")
            .action(Arguments.storeTrue())
            .help("Use the advanced tokenizer. If not defined" +
                  " the simple one is used");

        argsParser
            .addArgument("--stop-words-file")
            .dest("stopWordsFile")
            .type(String.class)
            .action(Arguments.store())
            .help("path to file containing the stop words");

        argsParser
            .addArgument("--input-buffer-size")
            .dest("inputBufferSize")
            .type(Integer.class)
            .action(Arguments.store())
            .help("size in characters of the buffer for BufferedReader");

        Namespace parsedArgs = null;
        try {
            parsedArgs = argsParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argsParser.handleError(e);
            System.exit(1);
        }

        if (parsedArgs.getBoolean("useAdvancedTokenizer")
            &&
            parsedArgs.getString("stopWordsFile") == null) {
            System.err.println(
                "ERROR Stop words file must be defined when" +
                " using the advanced tokenizer");
            System.exit(1);
        }

        Integer inputBufferSize = parsedArgs.getInt("inputBufferSize");
        if (inputBufferSize != null && inputBufferSize <= 0) {
            System.err.println("ERROR input buffer size must be greater than 0");
            System.exit(1);
        }

        return parsedArgs;
    }
}
