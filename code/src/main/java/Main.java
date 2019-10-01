
import document_parser.CorpusReader;
import document_parser.TrecAsciiMedline2004Parser;
import indexer.PostingIndexer;
import tokenizer.SimpleTokenizer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        // TODO validate program arguments

        PostingIndexer indexer = new PostingIndexer();

        SimpleTokenizer tokenizer = new SimpleTokenizer(indexer);

        CorpusReader cr = new CorpusReader(tokenizer);

        TrecAsciiMedline2004Parser.addFieldToSave("TI");
        cr.addParser("gz", TrecAsciiMedline2004Parser.class);

        try {
            cr.readCorpus(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //indexer.persist();
    }

}
