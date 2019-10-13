package parsers.corpus;

import parsers.files.FileParser;

import java.nio.file.Path;

public interface ResolveFileParser {
    Class<? extends FileParser> resolveFileParser(Path file);
}
