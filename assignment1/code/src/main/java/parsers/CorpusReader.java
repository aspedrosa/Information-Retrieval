package parsers;

import parsers.files.FileParser;
import tokenizer.BaseTokenizer;

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

/**
 * Class in charge of reading the files from the
 *  corpus folder recursively and to choose the
 *  right file parser for each file
 *
 * TODO create base class, child ones have different
 *  strategies to choose the right file parser
 */
public class CorpusReader {

    private Map<String, Class<? extends FileParser>> parsers;

    private BaseTokenizer tokenizer;

    public CorpusReader(BaseTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.parsers = new HashMap<>();
    }

    public CorpusReader(BaseTokenizer tokenizer, Map<String, Class<? extends FileParser>> parsers) {
        this.tokenizer = tokenizer;
        this.parsers = parsers;
    }

    public void addParser(String extension, Class<? extends FileParser> parser) {
        parsers.put(extension, parser);
    }

    private void processFolder(Path folder) throws IOException {
        Iterator<Path> it = Files.list(folder).iterator();

        while(it.hasNext()) {
            Path path = it.next();

            if (Files.isDirectory(path)) {
                processFolder(path);
            }
            else {
                processFile(path);
            }
        }
    }

    private void processFile(Path path) throws IOException {
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
            return;
        }

        // get the constructor of the file parser
        Constructor<? extends FileParser> constructor;
        try {
            constructor = parserClass.getConstructor(InputStream.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        // instantiate the file parser
        FileParser parser;
        try {
            parser = constructor.newInstance(inputStream);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Indexing file " + filename);

        parser.parse();

        inputStream.close();

        System.out.println("Finished indexing file " + filename);
    }

    public void readCorpus(String corpusFolderPath) throws IOException {
        processFolder(Paths.get(corpusFolderPath));
    }
}
