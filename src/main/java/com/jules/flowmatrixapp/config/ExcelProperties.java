package com.jules.flowmatrixapp.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "excel")
@Data
@Validated
public class ExcelProperties {
    private String sheetName;
    private int startRowIndex = 1;

    @NotNull
    private Col col = new Col();

    @Data
    public static class Col {
        private int environment = 0;
        private int srcIp = 1;
        private int srcDns = 2;
        private int protocol = 3;
        private int dstIp = 4;
        private int dstDns = 5;
        private int description = -1;
    }
}
