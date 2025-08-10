package com.jules.flowmatrixapp.service;

import com.jules.flowmatrixapp.config.ExcelProperties;
import com.jules.flowmatrixapp.model.FlowRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelImportService {

    private final ExcelProperties excelProperties;
    private final NodeClassifierService nodeClassifierService;

    public ExcelImportService(ExcelProperties excelProperties, NodeClassifierService nodeClassifierService) {
        this.excelProperties = excelProperties;
        this.nodeClassifierService = nodeClassifierService;
    }

    public List<FlowRecord> importFromExcel(InputStream inputStream) throws Exception {
        List<FlowRecord> records = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet;
        if (excelProperties.getSheetName() != null && !excelProperties.getSheetName().isBlank()) {
            sheet = workbook.getSheet(excelProperties.getSheetName());
        } else {
            sheet = workbook.getSheetAt(0);
        }

        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found!");
        }

        Iterator<Row> rowIterator = sheet.iterator();
        // Skip header rows
        for (int i = 0; i < excelProperties.getStartRowIndex() && rowIterator.hasNext(); i++) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String srcIp = getCellStringValue(row, excelProperties.getCol().getSrcIp());
            String dstIp = getCellStringValue(row, excelProperties.getCol().getDstIp());

            if (srcIp.isBlank() || dstIp.isBlank()) {
                continue; // Ignore rows without srcIp or dstIp
            }

            FlowRecord record = FlowRecord.builder()
                .environment(getCellStringValue(row, excelProperties.getCol().getEnvironment()))
                .srcIp(srcIp)
                .srcDns(getCellStringValue(row, excelProperties.getCol().getSrcDns()))
                .protocol(getCellStringValue(row, excelProperties.getCol().getProtocol()))
                .dstIp(dstIp)
                .dstDns(getCellStringValue(row, excelProperties.getCol().getDstDns()))
                .description(getCellStringValue(row, excelProperties.getCol().getDescription()))
                .build();

            // Classify nodes
            nodeClassifierService.classifyNodes(record);

            records.add(record);
        }

        workbook.close();
        return records;
    }

    private String getCellStringValue(Row row, int colIndex) {
        if (colIndex < 0) return "";
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    // Format as integer if it's a whole number
                    double num = cell.getNumericCellValue();
                    if (num == (long) num) {
                        yield String.valueOf((long) num);
                    } else {
                        yield String.valueOf(num);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                // Try to evaluate formula, fallback to cached value
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    yield cell.getCellFormula();
                }
            }
            default -> "";
        };
    }
}
