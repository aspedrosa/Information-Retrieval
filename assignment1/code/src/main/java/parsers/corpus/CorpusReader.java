package parsers.corpus;

import parsers.files.FileParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Stack;

/**
 * Class in charge of reading the files from the
 *  corpus folder recursively
 */
public final class CorpusReader implements Iterable<FileParser> {

    private Iterator<Path> corpusFolder;

    private ResolveFileParser fileParserResolver;

    public CorpusReader(Iterator<Path> corpusFolder, ResolveFileParser fileParserResolver) {
        this.corpusFolder = corpusFolder;
        this.fileParserResolver = fileParserResolver;
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

                    try {
                        if (Files.isDirectory(path)) {
                            Iterator<Path> tmpPath = currentFolder;

                            currentFolder = Files.list(path).iterator();

                            paths.push(tmpPath);
                        }
                        else {
                            currentFileParser = createFileParser(path);
                            if (currentFileParser != null) {
                                return true;
                            }
                        }
                    } catch (IOException e) {
                        if (Files.isDirectory(path)) {
                            System.err.println("ERROR listing directory " + path.toFile().getAbsolutePath() + "\n");
                        }
                        else {
                            System.err.println("ERROR opening file " + path.toFile().getAbsolutePath() + "\n");
                        }
                        e.printStackTrace();
                    }
                }

                paths.pop();
            }

            return false;
        }

        private FileParser createFileParser(Path path) throws IOException {
            Class<? extends FileParser>parserClass = fileParserResolver.resolveFileParser(path);
            if (parserClass == null) {
                System.err.println("ERROR no parser found for file " + path.toFile().getAbsolutePath() + "\n");
                return null;
            }

            // get the constructor of the file parser
            Constructor<? extends FileParser> constructor = null;
            try {
                constructor = parserClass.getConstructor(InputStream.class, String.class);
            } catch (NoSuchMethodException e) {
                System.err.println("ERROR getting constructor of " + parserClass.getSimpleName() + "\n");
                return null;
            }

            InputStream inputStream = new FileInputStream(path.toFile());

            // instantiate the file parser
            FileParser parser;
            try {
                parser = constructor.newInstance(inputStream, path.toFile().getAbsolutePath());
            } catch (InstantiationException | IllegalAccessException e) {
                System.err.println("ERROR instantiating " + parserClass.getSimpleName() + "\n");
                return null;
            } catch (InvocationTargetException e) {
                throw new IOException(e.getTargetException());
            }

            return parser;
        }

        @Override
        public FileParser next() {
            return currentFileParser;
        }
    }
}
