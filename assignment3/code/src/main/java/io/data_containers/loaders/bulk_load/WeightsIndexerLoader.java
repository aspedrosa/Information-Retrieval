package io.data_containers.loaders.bulk_load;

import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermWithInfo;
import data_containers.indexer.structures.aux_structs.DocumentWeight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WeightsIndexerLoader extends LinesLoader<TermWithInfo<Float>, List<DocumentWithInfo<DocumentWeight>>> {

    private static final Pattern separatorsRegex = Pattern.compile("[:;]");

    public WeightsIndexerLoader(String folder) {
        super(folder);
    }

    @Override
    public Map<TermWithInfo<Float>, List<DocumentWithInfo<DocumentWeight>>> parseLines(Stream<String> lines) {
        return lines.map(line -> {
            String[] elements = separatorsRegex.split(line);

            Entry<TermWithInfo<Float>, List<DocumentWithInfo<DocumentWeight>>> entry = new Entry<>();

            entry.setKey(
                new TermWithInfo<>(
                    elements[0],
                    Float.parseFloat(elements[1])
                    )
            );

            List<DocumentWithInfo<DocumentWeight>> postingList = new ArrayList<>((elements.length - 2) / 2);

            for (int i = 2; i < elements.length; i += 2) {
                postingList.add(
                    new DocumentWithInfo<>(
                        Integer.parseInt(elements[i]),
                        new DocumentWeight(Float.parseFloat(elements[i + 1]))
                    )
                );
            }

            entry.setValue(postingList);

            return entry;
        }).collect(Collectors.toMap(
            Entry::getKey,
           Entry::getValue
        ));
    }

}
