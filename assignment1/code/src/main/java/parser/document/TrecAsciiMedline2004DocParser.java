package parser.document;

import java.util.*;

public class TrecAsciiMedline2004DocParser extends DocumentParser {

    private static Set<String> fieldsToSave = new HashSet<>();

    public TrecAsciiMedline2004DocParser(List<String> document) {
        super(document);
    }

    public static void addFieldToSave(String fieldToSave) {
        fieldsToSave.add(fieldToSave);
    }

    public static void setFieldsToSave(Set<String> newFieldsToSave) {
        fieldsToSave = newFieldsToSave;
    }

    @Override
    public void parse() {
        // name of a field
        String label = "";
        // content of the PMID field
        String identifier = "";
        // content of a field
        StringBuilder content = new StringBuilder();

        // terms received from the tokenizer to index
        List<String> terms = new LinkedList<>();

        for (String line : document) {
            // if the current lines starts with a space
            //  then append to the field's content the current line
            if (line.charAt(0) == ' ') {
                content.append(' ');
                content.append(line.trim());
            }
            else {
                // if it doesn't start with a space
                //  then the field that was being parsed ended

                // if the parsed field is one of fields to be indexed
                if (fieldsToSave.contains(label)) {
                    // if the label of the field is PMID save it's content to
                    //  the identifier
                    if (label.equals("PMID")) {
                        identifier = content.toString();
                    }
                    else {
                        // else tokenize the conent and add the resulting terms to
                        //  the list of terms
                        terms.addAll(tokenizer.tokenizeString(content.toString()));
                    }
                }

                // start the parsing of a new field
                label = line.substring(0, 4).trim();
                content = new StringBuilder(line.substring(6).trim());
            }
        }

        // needed in case the last field parsed is to be tokenized
        if (fieldsToSave.contains(label)) {
            terms.addAll(tokenizer.tokenizeString(content.toString()));
        }

        indexer.registerDocument(documentId ,identifier);

        if (!terms.isEmpty()) {
            indexer.indexTerms(documentId, terms);
        }
    }
}
