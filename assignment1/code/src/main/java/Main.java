
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import parsers.CorpusReader;
import parsers.documents.Document;
import parsers.documents.TrecAsciiMedline2004DocParser;
import parsers.files.FileParser;
import parsers.files.TrecAsciiMedline2004FileParser;
import indexer.FrequencyIndexer;
import indexer.structures.DocumentWithFrequency;
import indexer.structures.SimpleTerm;
import tokenizer.AdvanvedTokenizer;
import tokenizer.BaseTokenizer;
import tokenizer.SimpleTokenizer;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Namespace parsedArgs = parseProgramArguments(args);

        FileParser.setReaderBufferSize(parsedArgs.getInt("inputBufferSize"));

        FrequencyIndexer indexer = new FrequencyIndexer();
        System.out.println("Created the indexer");

        BaseTokenizer tokenizer;
        if (parsedArgs.getBoolean("useAdvancedTokenizer")) {
            tokenizer = new AdvanvedTokenizer();
            System.out.println("Created the Advanced tokenizer");
        }
        else {
            tokenizer = new SimpleTokenizer();
            System.out.println("Created the Simple tokenizer");
        }

        CorpusReader corpusReader = null;
        try {
            corpusReader = new CorpusReader(parsedArgs.getString("corpusFolder"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        TrecAsciiMedline2004DocParser.addFieldToSave("TI");
        TrecAsciiMedline2004DocParser.addFieldToSave("PMID");
        corpusReader.addParser("gz", TrecAsciiMedline2004FileParser.class);

        System.out.println("Started parsing the corpus");
        long begin = System.currentTimeMillis();

        int count = 0;
        for (FileParser fileParser : corpusReader) {
            for (Document document : fileParser) {
                /**if (count == 1000){
                    break;
                }
                count++;**/
                if (document == null) {
                    // in case some error occurs while reading some file
                    break;
                }

                List<String> terms = tokenizer.tokenizeDocument(document.getToTokenize());

                int docId = document.getId();
                indexer.registerDocument(docId, document.getIdentifier());

                if (!terms.isEmpty()){
                    indexer.indexTerms(docId, terms);
                }
            }
        }

        System.out.println("Finished parsing the corpus in " + (System.currentTimeMillis() - begin));

        BufferedOutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(parsedArgs.getString("indexOutputFilename")));
        } catch (FileNotFoundException e) {
            System.err.println("Output file not found");
            System.exit(2);
        }

        System.out.println("Started writing index to disk");
        begin = System.currentTimeMillis();
        try {
            indexer.persist(output, (out, index) -> {
                System.out.println("Sorting terms");

                List<SimpleTerm> sortedTerms = new ArrayList<>(index.keySet());
                sortedTerms.sort(SimpleTerm::compareTo);

                System.out.println("Finished sorting terms");

                System.out.println("Writing index to disk");

                for (SimpleTerm term : sortedTerms) {
                    out.write(term.getTerm().getBytes());
                    out.write(',');

                    Iterator<DocumentWithFrequency> it = index.get(term).iterator();
                    while (it.hasNext()) {
                        DocumentWithFrequency doc = it.next();

                        String docStr = String.format(
                            "%s:%d",
                            doc.getDocId(),
                            doc.getFrequency()
                        );

                        out.write(docStr.getBytes());

                        if (it.hasNext()) {
                            out.write(',');
                        }
                    }

                    out.write('\n');
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        System.out.println("Finished storing index to disk in " + (System.currentTimeMillis() - begin));
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

        return parsedArgs;
    }
}
