package parsers;

import parsers.files.FileParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Class in charge of reading the files from the
 *  corpus folder recursively and to choose the
 *  right file parser for each file
 *
 * TODO create base class, child ones have different
 *  strategies to choose the right file parser
 */
public class CorpusReader implements Iterable<FileParser> {

    private Map<String, Class<? extends FileParser>> parsers;
    private Iterator<Path> corpusFolder;

    public CorpusReader(String corpusFolder) throws IOException {
        this.corpusFolder = Files.list(Paths.get(corpusFolder)).iterator();
        this.parsers = new HashMap<>();
    }

    public CorpusReader(String corpusFolder, Map<String, Class<? extends FileParser>> parsers) throws IOException {
        this.corpusFolder = Files.list(Paths.get(corpusFolder)).iterator();
        this.parsers = parsers;
    }

    public void addParser(String extension, Class<? extends FileParser> parser) {
        parsers.put(extension, parser);
    }

    @Override
    public Iterator<FileParser> iterator() {
        return new InternalIterator(corpusFolder);
    }

    private class InternalIterator implements Iterator<FileParser> {

        private Stack<Iterator<Path>> paths;
        private FileParser currentFileParser;

        private InternalIterator(Iterator<Path> corpusFolder) {
            paths = new Stack<>();
            paths.push(corpusFolder);
        }

        public boolean hasNext() {
            while (!paths.empty()) {
                Iterator<Path> currentFolder = paths.peek();

                while (currentFolder.hasNext()) {
                    Path path = currentFolder.next();

                    if (Files.isDirectory(path)) {
                        paths.push(currentFolder);
                        try {
                            currentFolder = Files.list(path).iterator();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.exit(2);
                        }
                    }
                    else {
                        currentFileParser = createFileParser(path);
                        if (currentFileParser != null) {
                            return true;
                        }
                    }
                }

                paths.pop();
            }

            return false;
        }

        private FileParser createFileParser(Path path) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(path.toFile());
            } catch (FileNotFoundException e) {
                // Impossible to happen since this file was
                //  found recursively
            }

            // getting file extension
            String filename = path.getFileName().toString();
            String[] filenameParts = filename.split("\\.");
            String extension = filenameParts[filenameParts.length - 1];

            // get the class of the file parser
            Class<? extends FileParser> parserClass = parsers.get(extension);
            if (parserClass == null) {
                System.err.println("No parser found for file " + filename);
                return null;
            }

            // get the constructor of the file parser
            Constructor<? extends FileParser> constructor = null;
            try {
                constructor = parserClass.getConstructor(InputStream.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                System.exit(3);
            }

            // instantiate the file parser
            FileParser parser = null;
            try {
                parser = constructor.newInstance(inputStream);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                System.exit(3);
            }

            return parser;
        }

        @Override
        public FileParser next() {
            return currentFileParser;
        }
    }
}
