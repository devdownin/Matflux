package com.jules.flowmatrixapp.service;

import com.jules.flowmatrixapp.config.RuleProperties;
import com.jules.flowmatrixapp.model.FlowRecord;
import com.jules.flowmatrixapp.model.NodeType;
import com.jules.flowmatrixapp.model.analysis.RuleViolation;
import com.jules.flowmatrixapp.model.dto.GraphData;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AnalysisService {

    private final RuleProperties ruleProperties;
    private final Pattern unencryptedPattern;

    public AnalysisService(RuleProperties ruleProperties) {
        this.ruleProperties = ruleProperties;
        if (ruleProperties.getWarnUnencryptedRegex() != null && !ruleProperties.getWarnUnencryptedRegex().isBlank()) {
            this.unencryptedPattern = Pattern.compile(ruleProperties.getWarnUnencryptedRegex(), Pattern.CASE_INSENSITIVE);
        } else {
            this.unencryptedPattern = null;
        }
    }

    public List<RuleViolation> evaluateRules(List<FlowRecord> records) {
        List<RuleViolation> violations = new ArrayList<>();
        for (FlowRecord record : records) {
            // Rule: Forbid Oracle in Dev
            if (ruleProperties.isForbidOracleInDev() &&
                "DEV".equalsIgnoreCase(record.getEnvironment()) &&
                (record.getSrcType() == NodeType.DATABASE_ORACLE || record.getDstType() == NodeType.DATABASE_ORACLE)) {
                violations.add(new RuleViolation(RuleViolation.Level.ERROR, "Oracle database is forbidden in DEV environment", record.getRecordKey()));
            }

            // Rule: Warn on unencrypted protocols
            if (unencryptedPattern != null &&
                unencryptedPattern.matcher(record.getProtocol()).matches()) {
                violations.add(new RuleViolation(RuleViolation.Level.WARN, "Unencrypted protocol detected: " + record.getProtocol(), record.getRecordKey()));
            }

            // Rule: Warn on missing DNS
            if (ruleProperties.isWarnMissingDns()) {
                if (record.getSrcDns() == null || record.getSrcDns().isBlank()) {
                    violations.add(new RuleViolation(RuleViolation.Level.WARN, "Source node " + record.getSrcIp() + " has no DNS name", record.getRecordKey()));
                }
                if (record.getDstDns() == null || record.getDstDns().isBlank()) {
                    violations.add(new RuleViolation(RuleViolation.Level.WARN, "Destination node " + record.getDstIp() + " has no DNS name", record.getRecordKey()));
                }
            }
        }
        return violations;
    }

    public Map<String, Object> calculateKpis(List<FlowRecord> records, GraphData graphData) {
        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("Total Flows", records.size());
        kpis.put("Unique Nodes", graphData.getNodes().size());

        kpis.put("Top 3 Protocols", getTopN(records, FlowRecord::getProtocol, 3));

        List<NodeType> nodeTypes = Stream.concat(
            records.stream().map(FlowRecord::getSrcType),
            records.stream().map(FlowRecord::getDstType)
        ).collect(Collectors.toList());
        kpis.put("Top 3 Node Types", getTopN(nodeTypes, NodeType::name, 3));

        return kpis;
    }

    public List<Map.Entry<String, Long>> calculateHubs(GraphData graphData, int topN) {
        Map<String, Long> degrees = new HashMap<>();
        graphData.getEdges().forEach(edge -> {
            degrees.merge(edge.getFrom(), 1L, Long::sum);
            degrees.merge(edge.getTo(), 1L, Long::sum);
        });

        return degrees.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    private <T> List<Map.Entry<String, Long>> getTopN(List<T> items, Function<T, String> keyExtractor, int n) {
        return items.stream()
                .map(keyExtractor)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toList());
    }
}
