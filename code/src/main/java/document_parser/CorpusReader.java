package document_parser;

import indexer.BaseIndexer;
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
import java.util.zip.GZIPInputStream;

public class CorpusReader {

    private Map<String, Class<? extends DocumentParser>> parsers;

    private BaseTokenizer tokenizer;

    public CorpusReader(BaseTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.parsers = new HashMap<>();
    }

    public CorpusReader(BaseTokenizer tokenizer, Map<String, Class<? extends DocumentParser>> parsers) {
        this.tokenizer = tokenizer;
        this.parsers = parsers;
    }

    public void addParser(String extension, Class<? extends DocumentParser> parser) {
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

        String filename = path.getFileName().toString();
        String[] filenameParts = filename.split("\\.");
        String extension = filenameParts[filenameParts.length - 1];

        Class<? extends DocumentParser> parserClass = parsers.get(extension);

        if (parserClass == null) {
            System.err.println("No parser found for file " + filename);
            return;
        }

        Constructor<? extends DocumentParser> constructor;
        try {
            constructor = parserClass.getConstructor(InputStream.class, BaseTokenizer.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        DocumentParser parser;
        try {
            parser = constructor.newInstance(inputStream, tokenizer);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }

        parser.parse();

        inputStream.close();
    }

    public void readCorpus(String corpusFolderPath) throws IOException {
        processFolder(Paths.get(corpusFolderPath));
    }
}
