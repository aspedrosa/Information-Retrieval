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
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Class in charge of reading the files from the
 *  corpus folder recursively
 */
public final class CorpusReader implements Iterable<FileParser> {

    /**
     * Iterator to iterate over the
     *  files/folders on the corpus folder
     */
    private Iterator<Path> corpusFolder;

    /**
     * FileParser resolver
     */
    private ResolveFileParser fileParserResolver;

    /**
     * Main constructor
     *
     * @param corpusFolder Iterator to iterator over the folder/files on the corpus folder
     * @param fileParserResolver FileParser resolver
     */
    public CorpusReader(Iterator<Path> corpusFolder, ResolveFileParser fileParserResolver) {
        this.corpusFolder = corpusFolder;
        this.fileParserResolver = fileParserResolver;
    }

    /**
     * Creates an iterator to iterate over the
     *  several file parsers of each file.
     *
     * If you want to iterate over the corpus folder
     *  several times one have to instantiate a new
     *  CorpusReader class because the iterator over
     *  the corpus folder received on the constructor
     *  is used
     *
     * @return iterator of FileParsers
     */
    @Override
    public Iterator<FileParser> iterator() {
        return new InternalIterator(corpusFolder);
    }

    /**
     * Iterator to iterate over the files/folder of
     *  the corpus folder and return the right
     *  FileParser for each file
     * This iterator was implemented assuming that the user
     *  will call hasNext() before a next() call
     */
    private class InternalIterator implements Iterator<FileParser> {

        /**
         * Stack to achieve recursion over the
         *  corpus folder
         */
        private Stack<Iterator<Path>> paths;

        /**
         * The Fi = nullleParser to return on the next next() call
         */
        private FileParser currentFileParser;

        /**
         * Main constructor
         *
         * @param corpusFolder Iterator to iterate over the
         *  files/folders on the corpus folder
         */
        private InternalIterator(Iterator<Path> corpusFolder) {
            paths = new Stack<>();
            paths.push(corpusFolder);
        }

        /**
         * Fetches the directories until it is able to create
         *  a valid FileParser
         *
         * @return true if a file was found and was able to create
         *  a FileParser. If it fails to create a FileParser continues
         *  to iterate over the directories. Returns false if there is no
         *  more files to parse
         */
        public boolean hasNext() {

            if (currentFileParser != null) {
                return true;
            }

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

        /**
         * Creates a FileParser for a specific file
         *
         * @param path Path object of a file
         * @return FileParser object to be used
         * @throws IOException if some error occurs creating the InputStream
         *  or the BufferedReader (FileParser constructor)
         */
        private FileParser createFileParser(Path path) throws IOException {
            // resolve which FileParser to use
            Class<? extends FileParser>parserClass = fileParserResolver.resolveFileParser(path);
            if (parserClass == null) {
                System.err.println("ERROR no parser found for file " + path.toFile().getAbsolutePath() + "\n");
                return null;
            }

            // get the constructor of the FileParser
            Constructor<? extends FileParser> constructor;
            try {
                constructor = parserClass.getConstructor(InputStream.class, String.class);
            } catch (NoSuchMethodException e) {
                System.err.println("ERROR getting constructor of " + parserClass.getSimpleName() + "\n");
                return null;
            }

            InputStream inputStream = new FileInputStream(path.toFile());

            // instantiate the FileParser
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

        /**
         * Gets the fetched FileParser on the hasNext() call
         *
         * @return the FileParser fetched
         * @throws NoSuchElementException if the current FileParser
         *  is null
         */
        @Override
        public FileParser next() {
            if (currentFileParser == null) {
                throw new NoSuchElementException();
            }

            FileParser tmp = currentFileParser;

            currentFileParser = null;

            return tmp;
        }
    }
}
