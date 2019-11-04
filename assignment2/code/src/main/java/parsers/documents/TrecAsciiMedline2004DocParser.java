package parsers.documents;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Specific implementation of the DocumentParser
 */
public class TrecAsciiMedline2004DocParser implements DocumentParser {

    /**
     * Which fields of this specific file should this parser save
     */
    private static Set<String> fieldsToSave = new HashSet<>();

    /**
     * Inserts a new field to save
     *
     * @param fieldToSave new field
     */
    public static void addFieldToSave(String fieldToSave) {
        fieldsToSave.add(fieldToSave);
    }

    /**
     * Setters for the fieldToSave field
     *
     * @param newFieldsToSave new set with fields to save
     */
    public static void setFieldsToSave(Set<String> newFieldsToSave) {
        fieldsToSave = newFieldsToSave;
    }

    /**
     * Extracts the content from the fields in fieldsToSave
     *  field and gets the document identifier
     *
     * @param documentContent to parse
     * @return Document object with the content to tokenize
     */

    /**
     * Field to avoid some extra String Objects initialization
     */
    private final static String ID_FIELD = "PMID";

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
                content.append(line);
            }
            else {
                // if it doesn't start with a space
                //  then the field that was being parsed ended

                // if the parsed field is one of fields to be indexed
                if (fieldsToSave.contains(label)) {
                    // if the label of the field is PMID save it's content to
                    //  the identifier
                    if (label.equals(ID_FIELD)) {
                        document.setIdentifier(content.toString());
                    }
                    else {
                        // else add the content to the toTokenize list
                        document.addStringToTokenize(content.toString());
                    }
                }

                // start the parsing of a new field
                label = line.substring(0, 4).trim();
                content = new StringBuilder(line.substring(6));
            }
        }

        // needed in case the last field parsed needs to be tokenized
        if (fieldsToSave.contains(label)) {
            document.addStringToTokenize(content.toString());
        }
        // or is the identifier
        else if (label.endsWith(ID_FIELD)) {
            document.setIdentifier(content.toString());
        }

        return document;
    }
}
