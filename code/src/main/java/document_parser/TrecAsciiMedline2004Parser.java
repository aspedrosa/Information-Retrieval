package document_parser;

import tokenizer.BaseTokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

public class TrecAsciiMedline2004Parser extends DocumentParser {

    private static Set<String> fieldsToSave = new TreeSet<>();

    public TrecAsciiMedline2004Parser(InputStream input, BaseTokenizer tokenizer) {
        super(input, tokenizer);
    }

    public static void addFieldToSave(String fieldToSave) {
        fieldsToSave.add(fieldToSave);
    }

    public static void setFieldsToSave(Set<String> newFieldsToSave) {
        fieldsToSave = newFieldsToSave;
    }

    public void parse() throws IOException {
        BufferedReader buffer =
            new BufferedReader(
                new InputStreamReader(
                    new GZIPInputStream(
                        input
                    )
                )
            );

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
                        //we could also tokenize now instead of
                        // doing it all at the end

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
