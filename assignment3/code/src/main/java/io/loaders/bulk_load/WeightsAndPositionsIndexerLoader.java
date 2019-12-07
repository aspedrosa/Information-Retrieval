package io.loaders.bulk_load;

import data_containers.indexer.structures.DocumentWithInfo;
import data_containers.indexer.structures.TermWithInfo;
import data_containers.indexer.structures.aux_structs.DocumentWeightAndPositions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WeightsAndPositionsIndexerLoader extends LinesLoader<TermWithInfo<Float>, List<DocumentWithInfo<DocumentWeightAndPositions>>> {

    private static final Pattern separatorsRegex = Pattern.compile("[:;]");

    private static final Pattern positionsSeparatorsRegex = Pattern.compile(",");

    @Override
    public Map<TermWithInfo<Float>, List<DocumentWithInfo<DocumentWeightAndPositions>>> parseLines(Stream<String> lines) {
        return lines.map(line -> {
            String[] elements = separatorsRegex.split(line);

            Entry<TermWithInfo<Float>, List<DocumentWithInfo<DocumentWeightAndPositions>>> entry = new Entry<>();

            entry.setKey(
                new TermWithInfo<>(
                    elements[0],
                    Float.parseFloat(elements[1])
                )
            );

            List<DocumentWithInfo<DocumentWeightAndPositions>> postingList = new ArrayList<>((elements.length - 2) / 3);

            for (int i = 2; i < elements.length; i += 3) {
                String[] positionsStrings = positionsSeparatorsRegex.split(elements[i + 2]);

                List<Integer> positions = new ArrayList<>(positionsStrings.length);
                for (String positionsString : positionsStrings) {
                    positions.add(Integer.parseInt(positionsString));
                }

                postingList.add(
                    new DocumentWithInfo<>(
                        Integer.parseInt(elements[i]),
                        new DocumentWeightAndPositions(
                            Float.parseFloat(elements[i + 1]),
                            positions
                        )
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
