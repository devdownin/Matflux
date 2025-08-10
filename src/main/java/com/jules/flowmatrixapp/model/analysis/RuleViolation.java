package com.jules.flowmatrixapp.model.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleViolation {
    public enum Level { WARN, ERROR }
    private Level level;
    private String reason;
    private String recordKey;
}
