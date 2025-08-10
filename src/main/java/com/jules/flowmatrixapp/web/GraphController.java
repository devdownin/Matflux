package com.jules.flowmatrixapp.web;

import com.jules.flowmatrixapp.model.FlowRecord;
import com.jules.flowmatrixapp.model.NodeType;
import com.jules.flowmatrixapp.model.analysis.RuleViolation;
import com.jules.flowmatrixapp.model.dto.GraphData;
import com.jules.flowmatrixapp.service.AnalysisService;
import com.jules.flowmatrixapp.service.DataStoreService;
import com.jules.flowmatrixapp.service.GraphBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class GraphController {

    private final DataStoreService dataStoreService;
    private final GraphBuilderService graphBuilderService;
    private final AnalysisService analysisService;

    @GetMapping("/graph")
    public String graphPage(Model model,
                            @RequestParam(required = false) String env,
                            @RequestParam(required = false) List<String> types) {
        if (!dataStoreService.hasData()) return "redirect:/?error=No data loaded";

        Set<String> environments = dataStoreService.getFlowRecords().stream()
                .map(FlowRecord::getEnvironment).collect(Collectors.toSet());

        model.addAttribute("environments", environments);
        model.addAttribute("nodeTypes", NodeType.values());
        model.addAttribute("selectedEnv", env);
        model.addAttribute("selectedTypes", types != null ? types : Collections.emptyList());
        return "graph";
    }

    @GetMapping("/graph/data")
    @ResponseBody
    public GraphData getGraphData(@RequestParam(required = false) String env,
                                  @RequestParam(required = false) List<String> types) {
        List<FlowRecord> filteredRecords = filterRecords(env, types);
        List<RuleViolation> violations = analysisService.evaluateRules(filteredRecords);
        Map<String, RuleViolation> violationMap = violations.stream()
            .collect(Collectors.toMap(RuleViolation::getRecordKey, Function.identity(), (v1, v2) -> v1)); // Keep first violation if multiple
        return graphBuilderService.buildGraph(filteredRecords, violationMap);
    }

    @GetMapping("/graph/node")
    @ResponseBody
    public Map<String, Object> getNodeDetails(@RequestParam String key) {
        Map<String, Object> details = new LinkedHashMap<>();

        List<FlowRecord> incoming = dataStoreService.getFlowRecords().stream()
            .filter(r -> r.getDstKey().equals(key))
            .collect(Collectors.toList());

        List<FlowRecord> outgoing = dataStoreService.getFlowRecords().stream()
            .filter(r -> r.getSrcKey().equals(key))
            .collect(Collectors.toList());

        details.put("incoming", incoming);
        details.put("outgoing", outgoing);
        return details;
    }

    // Duplicated filtering logic, could be refactored into a shared service
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
