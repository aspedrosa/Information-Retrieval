package parsers.corpus;

import parsers.files.FileParser;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ResolveByExtension implements ResolveFileParser {

    private Map<String, Class<? extends FileParser>> parsers;

    public ResolveByExtension() {
        this.parsers = new HashMap<>();
    }

    public ResolveByExtension(Map<String, Class<? extends FileParser>> parsers) {
        this.parsers = parsers;
    }

    public void addParser(String extension, Class<? extends FileParser> parser) {
        parsers.put(extension, parser);
    }

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
