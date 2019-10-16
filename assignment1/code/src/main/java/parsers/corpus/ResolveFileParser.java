package parsers.corpus;

import parsers.files.FileParser;

import java.nio.file.Path;

/**
 * In charge of deciding which FileParser
 *  should be used for each file
 */
public interface ResolveFileParser {

    /**
     * Chooses the right FileParser for each file
     *
     * @param file Path object of the file
     * @return FileParser to be used to parse
     */
    Class<? extends FileParser> resolveFileParser(Path file);

}
