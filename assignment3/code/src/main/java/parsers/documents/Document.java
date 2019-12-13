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
     * Unique identifier of the document to be associated
     *  to its id
     */
    private Integer identifier;

    /**
     * Document content to be fed to the tokenizer
     */
    private List<String> toTokenize;

    /**
     * Setter of the identifier field
     *
     * @param identifier new identifier
     */
    public void setIdentifier(Integer identifier) {
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
     * Getter of the identifier field
     *
     * @return document field
     */
    public Integer getIdentifier() {
        return identifier;
    }

    /**
     * Default constructor
     */
    public Document() {
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
