package parsers.documents;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class TrecAsciiMedline2004DocParser implements DocumentParser {

    private static Set<String> fieldsToSave = new HashSet<>();

    public static void addFieldToSave(String fieldToSave) {
        fieldsToSave.add(fieldToSave);
    }

    public static void setFieldsToSave(Set<String> newFieldsToSave) {
        fieldsToSave = newFieldsToSave;
    }

    @Override
    public Document parse(List<String> documentContent) {
        // name of a field
        String label = "";
        // content of a field
        StringBuilder content = new StringBuilder();

        Document document = new Document();

        for (String line : documentContent) {
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
                        document.setIdentifier(content.toString());
                    }
                    else {
                        // TODO update
                        // else tokenize the conent and add the resulting terms to
                        //  the list of terms
                        document.addStringToTokenize(content.toString());
                    }
                }

                // start the parsing of a new field
                label = line.substring(0, 4).trim();
                content = new StringBuilder(line.substring(6).trim());
            }
        }

        // TODO update. also check if field is PMID
        // needed in case the last field parsed is to be tokenized
        if (fieldsToSave.contains(label)) {
            document.addStringToTokenize(content.toString());
        }

        return document;
    }
}
