
import parser.CorpusReader;
import parser.document.DocumentParser;
import parser.document.TrecAsciiMedline2004DocParser;
import parser.file.TrecAsciiMedline2004FileParser;
import indexer.FrequencyIndexer;
import indexer.structures.DocumentWithFrequency;
import indexer.structures.SimpleTerm;
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
        // TODO validate program arguments
        // arg 1 -> corpus folder
        // arg 2 -> index output file

        FrequencyIndexer indexer = new FrequencyIndexer();
        DocumentParser.setIndexer(indexer);

        System.out.println("Created the indexer");

        SimpleTokenizer tokenizer = new SimpleTokenizer();
        DocumentParser.setTokenizer(tokenizer);

        System.out.println("Created the tokenizer");

        CorpusReader cr = new CorpusReader(tokenizer);

        TrecAsciiMedline2004DocParser.addFieldToSave("TI");
        TrecAsciiMedline2004DocParser.addFieldToSave("PMID");
        cr.addParser("gz", TrecAsciiMedline2004FileParser.class);

        System.out.println("Started parsing the corpus");
        long begin = System.currentTimeMillis();

        try {
            cr.readCorpus(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        System.out.println("Finished parsing the corpus in " + (System.currentTimeMillis() - begin));

        BufferedOutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(args[1]));
        } catch (FileNotFoundException e) {
            System.err.println("Output file not found");
            System.exit(3);
        }

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
        }
    }

}
