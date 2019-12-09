package io.data_containers.loaders.bulk_load;

import data_containers.indexer.structures.Document;
import data_containers.indexer.structures.TermInfoWithIDF;

import java.util.ArrayList;
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
    public Map<String, TermInfoWithIDF<Float, Document<Float>>> parseLines(Stream<String> lines) {
        return lines.map(line -> {
            String[] elements = separatorsRegex.split(line);

            Entry<String, TermInfoWithIDF<Float, Document<Float>>> entry = new Entry<>();

            entry.setKey(elements[0]);

            List<Document<Float>> postingList = new ArrayList<>((elements.length - 2) / 2);

            for (int i = 2; i < elements.length; i += 2) {
                postingList.add(
                    new Document<>(
                        Integer.parseInt(elements[i]), // docId
                        Float.parseFloat(elements[i + 1])
                    )
                );
            }

            entry.setValue(new TermInfoWithIDF<>(
                postingList,
                Float.parseFloat(elements[1]) // idf
            ));

            return entry;
        }).collect(Collectors.toMap(
            Entry::getKey,
           Entry::getValue
        ));
    }

}
