package com.jules.flowmatrixapp.web;

import com.jules.flowmatrixapp.model.FlowRecord;
import com.jules.flowmatrixapp.model.diff.DiffRow;
import com.jules.flowmatrixapp.service.DataStoreService;
import com.jules.flowmatrixapp.service.DiffService;
import com.jules.flowmatrixapp.service.ExcelImportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DiffController {

    private final ExcelImportService excelImportService;
    private final DiffService diffService;
    private final DataStoreService dataStoreService;

    @GetMapping("/diff")
    public String diffPage(Model model) {
        model.addAttribute("hasDiffData", dataStoreService.hasDiffData());
        return "diff";
    }

    @PostMapping("/diff/upload")
    public String handleDiffUpload(@RequestParam("fileA") MultipartFile fileA,
                                   @RequestParam("fileB") MultipartFile fileB,
                                   RedirectAttributes redirectAttributes) {
        if (fileA.isEmpty() || fileB.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select both files for comparison.");
            return "redirect:/diff";
        }
        try {
            List<FlowRecord> recordsA = excelImportService.importFromExcel(fileA.getInputStream());
            List<FlowRecord> recordsB = excelImportService.importFromExcel(fileB.getInputStream());
            List<DiffRow> diffs = diffService.compare(recordsA, recordsB);
            dataStoreService.setDiffRows(diffs);
            redirectAttributes.addFlashAttribute("success", "Comparison complete. Found " + diffs.size() + " differences.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process files: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/diff";
    }

    @GetMapping("/diff/data")
    @ResponseBody
    public List<DiffRow> getDiffData(@RequestParam(defaultValue = "all") String only) {
        if (!dataStoreService.hasDiffData()) {
            return Collections.emptyList();
        }
        if ("all".equalsIgnoreCase(only)) {
            return dataStoreService.getDiffRows();
        }
        try {
            DiffRow.Kind filterKind = DiffRow.Kind.valueOf(only.toUpperCase());
            return dataStoreService.getDiffRows().stream()
                    .filter(d -> d.getKind() == filterKind)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return Collections.emptyList(); // Invalid filter
        }
    }

    @GetMapping("/diff/export/csv")
    public void exportDiffToCsv(@RequestParam(defaultValue = "all") String only, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"diff.csv\"");

        List<DiffRow> diffs = getDiffData(only); // Reuse the filtering logic

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Kind,ChangedFields,Env_A,Src_A,Dst_A,Proto_A,Env_B,Src_B,Dst_B,Proto_B");
            for (DiffRow diff : diffs) {
                FlowRecord a = diff.getRecordA() != null ? diff.getRecordA() : new FlowRecord();
                FlowRecord b = diff.getRecordB() != null ? diff.getRecordB() : new FlowRecord();
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s->%s\",\"%s->%s\",\"%s\",\"%s\",\"%s->%s\",\"%s->%s\",\"%s\"\n",
                        diff.getKind(),
                        diff.getChangedFields() != null ? String.join(";", diff.getChangedFields()) : "",
                        a.getEnvironment() != null ? a.getEnvironment() : "", a.getSrcKey(), a.getDstKey(), a.getProtocol() != null ? a.getProtocol() : "",
                        b.getEnvironment() != null ? b.getEnvironment() : "", b.getSrcKey(), b.getDstKey(), b.getProtocol() != null ? b.getProtocol() : ""
                );
            }
        }
    }
}
