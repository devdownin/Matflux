package com.jules.flowmatrixapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rule")
@Data
public class RuleProperties {
    private boolean forbidOracleInDev = true;
    private String warnUnencryptedRegex = "";
    private boolean warnMissingDns = true;
}
