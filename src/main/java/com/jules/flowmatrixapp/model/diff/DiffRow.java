package com.jules.flowmatrixapp.model.diff;

import com.jules.flowmatrixapp.model.FlowRecord;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class DiffRow {
    public enum Kind { ADDED, REMOVED, MODIFIED }

    private Kind kind;
    private FlowRecord recordA;
    private FlowRecord recordB;
    private Set<String> changedFields;
}
