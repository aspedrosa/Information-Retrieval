package parsers.documents;

import java.util.LinkedList;
import java.util.List;

public class Document {

    private int id;

    private static int CURRENT_ID = 1;

    private String identifier;

    private List<String> toTokenize;

    public Document() {
        id = CURRENT_ID++;
        toTokenize = new LinkedList<>();
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void addStringToTokenize(String toTokenize) {
        this.toTokenize.add(toTokenize);
    }

    public List<String> getToTokenize() {
        return toTokenize;
    }

    public int getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }
}
