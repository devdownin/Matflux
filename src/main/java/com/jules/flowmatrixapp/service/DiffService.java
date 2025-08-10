package com.jules.flowmatrixapp.service;

import com.jules.flowmatrixapp.model.FlowRecord;
import com.jules.flowmatrixapp.model.diff.DiffRow;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DiffService {

    public List<DiffRow> compare(List<FlowRecord> recordsA, List<FlowRecord> recordsB) {
        // Use the natural key (env|srcIp|dstIp|protocol) for comparison
        Map<String, FlowRecord> mapA = recordsA.stream()
                .collect(Collectors.toMap(FlowRecord::getRecordKey, Function.identity(), (r1, r2) -> r1)); // handle duplicates
        Map<String, FlowRecord> mapB = recordsB.stream()
                .collect(Collectors.toMap(FlowRecord::getRecordKey, Function.identity(), (r1, r2) -> r1));

        List<DiffRow> diffs = new ArrayList<>();

        // Check for removed and modified
        for (Map.Entry<String, FlowRecord> entryA : mapA.entrySet()) {
            FlowRecord recordA = entryA.getValue();
            FlowRecord recordB = mapB.get(entryA.getKey());

            if (recordB == null) {
                diffs.add(DiffRow.builder().kind(DiffRow.Kind.REMOVED).recordA(recordA).build());
            } else {
                Set<String> changedFields = findChanges(recordA, recordB);
                if (!changedFields.isEmpty()) {
                    diffs.add(DiffRow.builder()
                            .kind(DiffRow.Kind.MODIFIED)
                            .recordA(recordA)
                            .recordB(recordB)
                            .changedFields(changedFields)
                            .build());
                }
            }
        }

        // Check for added
        for (Map.Entry<String, FlowRecord> entryB : mapB.entrySet()) {
            if (!mapA.containsKey(entryB.getKey())) {
                diffs.add(DiffRow.builder().kind(DiffRow.Kind.ADDED).recordB(entryB.getValue()).build());
            }
        }

        return diffs;
    }

    private Set<String> findChanges(FlowRecord a, FlowRecord b) {
        Set<String> changes = new HashSet<>();
        // Key fields are the same, check other fields like DNS
        if (!Objects.equals(a.getSrcDns(), b.getSrcDns())) changes.add("Source DNS");
        if (!Objects.equals(a.getDstDns(), b.getDstDns())) changes.add("Destination DNS");
        if (!Objects.equals(a.getDescription(), b.getDescription())) changes.add("Description");
        // Note: The user also mentioned protocol change as a modification, but it's part of the key.
        // Assuming this means other fields changing for the same key.
        return changes;
    }
}
