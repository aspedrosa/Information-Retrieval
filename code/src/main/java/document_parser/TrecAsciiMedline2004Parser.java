package document_parser;

import tokenizer.BaseTokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class TrecAsciiMedline2004Parser extends DocumentParser {

    private Set<String> fieldsToSave;

    public TrecAsciiMedline2004Parser(GZIPInputStream input, BaseTokenizer tokenizer, Set<String> fieldsToSave) {
        super(input, tokenizer);
        this.fieldsToSave = fieldsToSave;
    }

    private GZIPInputStream getInputStream() {
        return (GZIPInputStream) input;
    }

    public void parse() throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(getInputStream()));

        List<String> toTokenize = new LinkedList<>();
        String docId = "";
        StringBuilder sb = new StringBuilder();

        while (buffer.ready()) {
            String line = buffer.readLine();

            if (line.equals("")) {
                if (sb.length() > 0) {
                    toTokenize.add(sb.toString());

                    sb = new StringBuilder();
                }

                tokenizer.tokenizeDocument(docId, toTokenize);
                toTokenize.clear();
            }
            else {
                String content = line.substring(6);
                if (line.charAt(4) == '-') {
                    if (sb.length() > 0) {
                        toTokenize.add(sb.toString());

                        sb = new StringBuilder();
                    }

                    String label = line.substring(0, 4).trim();

                    if (label.equals("PMID")) {
                        docId = label;
                    }
                    else if (fieldsToSave.contains(label)) {
                        sb.append(content);
                    }
                }
                else if (sb.length() > 0) {
                    /*
                    if (sb.charAt(sb.length() - 1) != '-') {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    else { */
                    sb.append(" ");
                    //}

                    sb.append(content);
                }
            }
        }
    }
}
