package mains.search;

import io.loaders.BaseLoader;
import io.loaders.bulk_load.BulkLoader;
import io.loaders.bulk_load.WeightsAndPositionsIndexerLoader;
import io.loaders.bulk_load.WeightsIndexerLoader;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

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

        // create an iterator over the index folder content
        Iterator<Path> indexesFolder = null;
        try {
            indexesFolder = Files.list(Paths.get(parsedArgs.getString("indexesFolder"))).iterator();
        } catch (IOException e) {
            System.err.println("ERROR listing the indexes folder\n");
            e.printStackTrace();
            System.exit(2);
        }

        TreeSet<String> indexesFileNames = getIndexFileNames(indexesFolder);
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
            .addArgument("indexesFolder")
            .type(String.class)
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

    private static TreeSet<String> getIndexFileNames(Iterator<Path> indexesFolder) {
        List<String> filenames = new ArrayList<>();

        while (indexesFolder.hasNext()) {
            Path indexFile = indexesFolder.next();

            filenames.add(indexFile.toFile().getName());
        }

        return new TreeSet<>(filenames);
    }
}
