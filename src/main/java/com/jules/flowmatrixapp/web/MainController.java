package com.jules.flowmatrixapp.web;

import com.jules.flowmatrixapp.model.FlowRecord;
import com.jules.flowmatrixapp.model.NodeType;
import com.jules.flowmatrixapp.service.DataStoreService;
import com.jules.flowmatrixapp.service.ExcelImportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ExcelImportService excelImportService;
    private final DataStoreService dataStoreService;

    @GetMapping("/")
    public String mainPage(Model model, @RequestParam(required = false) String env) {
        if (dataStoreService.hasData()) {
            Set<String> environments = dataStoreService.getFlowRecords().stream()
                    .map(FlowRecord::getEnvironment)
                    .collect(Collectors.toSet());
            model.addAttribute("environments", environments);

            List<FlowRecord> filteredRecords = dataStoreService.getFlowRecords().stream()
                    .filter(r -> env == null || env.isBlank() || env.equalsIgnoreCase(r.getEnvironment()))
                    .collect(Collectors.toList());
            model.addAttribute("records", filteredRecords);
            model.addAttribute("selectedEnv", env);
        }
        model.addAttribute("hasData", dataStoreService.hasData());
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/";
        }
        try {
            List<FlowRecord> records = excelImportService.importFromExcel(file.getInputStream());
            dataStoreService.setFlowRecords(records);
            redirectAttributes.addFlashAttribute("success", "File uploaded and processed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process Excel file: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping("/flows")
    @ResponseBody
    public List<FlowRecord> getFlows(@RequestParam(required = false) String env,
                                     @RequestParam(required = false) List<String> types) {
        Stream<FlowRecord> stream = dataStoreService.getFlowRecords().stream();
        if (env != null && !env.isBlank()) {
            stream = stream.filter(r -> env.equalsIgnoreCase(r.getEnvironment()));
        }
        if (types != null && !types.isEmpty()) {
            Set<NodeType> selectedTypes = types.stream().map(NodeType::valueOf).collect(Collectors.toSet());
            stream = stream.filter(r -> selectedTypes.contains(r.getSrcType()) || selectedTypes.contains(r.getDstType()));
        }
        return stream.collect(Collectors.toList());
    }

    @GetMapping("/export/csv")
    public void exportToCsv(@RequestParam(required = false) String env, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"flows.csv\"");

        List<FlowRecord> recordsToExport = dataStoreService.getFlowRecords().stream()
                .filter(r -> env == null || env.isBlank() || env.equalsIgnoreCase(r.getEnvironment()))
                .collect(Collectors.toList());

        try (PrintWriter writer = response.getWriter()) {
            // Header
            writer.println("Environment,SrcIP,SrcDNS,SrcType,Protocol,DstIP,DstDNS,DstType,Description");
            // Data
            for (FlowRecord r : recordsToExport) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        r.getEnvironment(), r.getSrcIp(), r.getSrcDns(), r.getSrcType(), r.getProtocol(),
                        r.getDstIp(), r.getDstDns(), r.getDstType(), r.getDescription());
            }
        }
    }
}
