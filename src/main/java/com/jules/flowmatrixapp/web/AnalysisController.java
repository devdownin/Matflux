package com.jules.flowmatrixapp.web;

import com.jules.flowmatrixapp.model.FlowRecord;
import com.jules.flowmatrixapp.model.NodeType;
import com.jules.flowmatrixapp.model.analysis.RuleViolation;
import com.jules.flowmatrixapp.model.dto.GraphData;
import com.jules.flowmatrixapp.service.AnalysisService;
import com.jules.flowmatrixapp.service.DataStoreService;
import com.jules.flowmatrixapp.service.GraphBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
public class AnalysisController {

    private final DataStoreService dataStoreService;
    private final AnalysisService analysisService;
    private final GraphBuilderService graphBuilderService;

    @GetMapping("/analysis")
    public Map<String, Object> getAnalysis(@RequestParam(required = false) String env,
                                           @RequestParam(required = false) List<String> types) {
        List<FlowRecord> filteredRecords = filterRecords(env, types);
        GraphData graphData = graphBuilderService.buildGraph(filteredRecords, new HashMap<>()); // Don't need violations in graph for KPIs
        List<RuleViolation> violations = analysisService.evaluateRules(filteredRecords);

        Map<String, Object> response = new HashMap<>();
        response.put("kpis", analysisService.calculateKpis(filteredRecords, graphData));
        response.put("hubs", analysisService.calculateHubs(graphData, 5));
        response.put("violations", violations);
        return response;
    }

    private List<FlowRecord> filterRecords(String env, List<String> types) {
        Stream<FlowRecord> stream = dataStoreService.getFlowRecords().stream();
        if (env != null && !env.isBlank()) {
            stream = stream.filter(r -> env.equalsIgnoreCase(r.getEnvironment()));
        }
        if (types != null && !types.isEmpty()) {
            Set<NodeType> selectedTypes = types.stream().map(t -> NodeType.valueOf(t.toUpperCase())).collect(Collectors.toSet());
            stream = stream.filter(r -> selectedTypes.contains(r.getSrcType()) || selectedTypes.contains(r.getDstType()));
        }
        return stream.collect(Collectors.toList());
    }
}
