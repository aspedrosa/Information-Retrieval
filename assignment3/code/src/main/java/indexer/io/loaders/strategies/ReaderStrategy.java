package indexer.io.loaders.strategies;

import java.util.Map;

/**
 * Strategy to parse the content written by
 *  persisters that write each entry
 *  in one text line
 */
public interface ReaderStrategy {

    Map.Entry createEntry(String line);

}
