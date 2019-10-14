package parsers.corpus;

import parsers.files.FileParser;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Chooses which FileParser to use by file extension
 */
public class ResolveByExtension implements ResolveFileParser {

    /**
     * Has the association between extension and FileParser
     */
    private Map<String, Class<? extends FileParser>> parsers;

    /**
     * Default constructor. Has no association between
     *  extensions and FileParsers
     */
    public ResolveByExtension() {
        this.parsers = new HashMap<>();
    }

    /**
     * Alternative constructor.
     *
     * @param parsers association between extensions and FileParsers
     */
    public ResolveByExtension(Map<String, Class<? extends FileParser>> parsers) {
        this.parsers = parsers;
    }

    /**
     * Associates a FileParser to a file extension
     *
     * @param extension file extension
     * @param parser FileParser associated
     */
    public void addParser(String extension, Class<? extends FileParser> parser) {
        parsers.put(extension, parser);
    }

    /**
     * Gets the FileParser for the specific file based on
     *  its extension
     *
     * @param file Path object of the file
     * @return a Class object of the FileParser to user or
     *  null if no match is found
     */
    @Override
    public Class<? extends FileParser> resolveFileParser(Path file) {
        // getting file extension
        String filename = file.getFileName().toString();
        String[] filenameParts = filename.split("\\.");
        String extension = filenameParts[filenameParts.length - 1];

        // get the class of the file parser
        return parsers.get(extension);
    }
}
