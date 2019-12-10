package io.data_containers.loaders.bulk_load;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoWithIDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WeightsIndexerLoader extends LinesLoader<
    String,
    TermInfoWithIDF<Float, Document<Float>>
    > {

    private static final Pattern separatorsRegex = Pattern.compile("[:;]");

    public WeightsIndexerLoader(String folder) {
        super(folder);
    }

    @Override
    public Map<String, TermInfoWithIDF<Float, Document<Float>>> parseLines(List<String> lines) {
        Map<String, TermInfoWithIDF<Float, Document<Float>>> invertedIndex = new HashMap<>(lines.size());

        for (String line : lines) {
            String[] elements = separatorsRegex.split(line);

            Entry<String, TermInfoWithIDF<Float, Document<Float>>> entry = new Entry<>();

            String term = elements[0];

            List<Document<Float>> postingList = new ArrayList<>((elements.length - 2) / 2);

            for (int i = 2; i < elements.length; i += 2) {
                postingList.add(
                    new Document<>(
                        Integer.parseInt(elements[i]), // docId
                        Float.parseFloat(elements[i + 1])
                    )
                );
            }

            invertedIndex.put(
                term,
                new TermInfoWithIDF<>(
                    postingList,
                    Float.parseFloat(elements[1]) // idf
                )
            );
        };

        return invertedIndex;
    }

}
