package parsers.documents;

import java.util.LinkedList;
import java.util.List;

/**
 * Class to containing document's content and
 *  other associated information such as its id
 *  and identifier
 * This class is used to pass the information from the
 *  file parsers to the tokenizer
 */
public class Document {

    /**
     * Id of this document
     */
    private int id;

    /**
     * Current document id. Works as a
     *  serial primary key
     */
    private static int CURRENT_ID = 0;

    /**
     * Unique identifier of the document to be associated
     *  to its id
     */
    private String identifier;

    /**
     * Document content to be fed to the tokenizer
     */
    private List<String> toTokenize;

    /**
     * Setter of the identifier field
     *
     * @param identifier new identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Getter of the document's content to tokenize
     *
     * @return lines to tokenize
     */
    public List<String> getToTokenize() {
        return toTokenize;
    }

    /**
     * Getter of the id field
     *
     * @return document id
     */
    public int getId() {
        return id;
    }

    /**
     * Getter of the identifier field
     *
     * @return document field
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Default constructor
     */
    public Document() {
        id = CURRENT_ID++;
        toTokenize = new LinkedList<>();
    }

    /**
     * Add a line to document's content to tokenize
     *
     * @param toTokenize new line to tokenize
     */
    public void addStringToTokenize(String toTokenize) {
        this.toTokenize.add(toTokenize);
    }

}
