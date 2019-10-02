
import document_parser.CorpusReader;
import document_parser.TrecAsciiMedline2004Parser;
import indexer.FrequencyIndexer;
import indexer.structures.DocumentWithFrequency;
import indexer.structures.SimpleTerm;
import tokenizer.SimpleTokenizer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Main {

    public static void main(String[] args) {
        // TODO validate program arguments
        // arg 1 -> corpus folder
        // arg 2 -> index output file

        FrequencyIndexer indexer = new FrequencyIndexer();

        SimpleTokenizer tokenizer = new SimpleTokenizer(indexer);

        CorpusReader cr = new CorpusReader(tokenizer);

        TrecAsciiMedline2004Parser.addFieldToSave("TI");
        cr.addParser("gz", TrecAsciiMedline2004Parser.class);

        try {
            cr.readCorpus(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        OutputStream output = null;
        try {
            output = new FileOutputStream(args[1]);
        } catch (FileNotFoundException e) {
            System.err.println("Output file not found");
            System.exit(3);
        }

        try {
            indexer.persist(output, (out, index) -> {
                for (SimpleTerm term : index.keySet()) {
                    out.write(term.getTerm().getBytes());
                    out.write(',');

                    for (DocumentWithFrequency doc : index.get(term)) {
                        String docStr = String.format(
                            "%s:%d",
                            doc.getDocId(),
                            doc.getFrequency()
                        );

                        out.write(docStr.getBytes());
                        out.write(',');
                    }

                    out.write('\n');
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
