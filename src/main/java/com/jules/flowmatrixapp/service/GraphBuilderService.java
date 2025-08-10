package com.jules.flowmatrixapp.service;

import com.jules.flowmatrixapp.model.FlowRecord;
import com.jules.flowmatrixapp.model.NodeType;
import com.jules.flowmatrixapp.model.analysis.RuleViolation;
import com.jules.flowmatrixapp.model.dto.EdgeDto;
import com.jules.flowmatrixapp.model.dto.GraphData;
import com.jules.flowmatrixapp.model.dto.NodeDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GraphBuilderService {

    public GraphData buildGraph(List<FlowRecord> records, Map<String, RuleViolation> violationsByRecordKey) {
        Map<String, NodeDto> nodes = new HashMap<>();

        for (FlowRecord record : records) {
            nodes.computeIfAbsent(record.getSrcKey(), key -> createNode(key, record.getSrcIp(), record.getSrcDns(), record.getSrcType()));
            nodes.computeIfAbsent(record.getDstKey(), key -> createNode(key, record.getDstIp(), record.getDstDns(), record.getDstType()));
        }

        List<EdgeDto> edges = records.stream()
                .map(record -> {
                    EdgeDto edge = new EdgeDto();
                    edge.setFrom(record.getSrcKey());
                    edge.setTo(record.getDstKey());
                    edge.setLabel(record.getProtocol());

                    RuleViolation violation = violationsByRecordKey.get(record.getRecordKey());
                    String title = String.format("%s -> %s (%s)", record.getSrcKey(), record.getDstKey(), record.getProtocol());
                    if (violation != null) {
                        title += String.format("\n%s: %s", violation.getLevel(), violation.getReason());
                        edge.setColor(Map.of("color", violation.getLevel() == RuleViolation.Level.ERROR ? "#ef4444" : "#f97316"));
                    }
                    edge.setTitle(title);
                    return edge;
                })
                .collect(Collectors.toList());

        return new GraphData(new ArrayList<>(nodes.values()), edges);
    }

    private NodeDto createNode(String key, String ip, String dns, NodeType type) {
        String label = (dns != null && !dns.isBlank()) ? dns + "\n" + ip : ip;
        String title = String.format("Type: %s\nIP: %s\nDNS: %s", type.name(), ip, (dns != null ? dns : "N/A"));

        return new NodeDto(
                key,
                label,
                type.name(),
                title,
                type.getIcon(),
                ip,
                dns,
                type
        );
    }
}
