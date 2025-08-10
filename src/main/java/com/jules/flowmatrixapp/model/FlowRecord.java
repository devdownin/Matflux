package com.jules.flowmatrixapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"environment", "srcIp", "dstIp", "protocol"})
public class FlowRecord {
    private String environment;
    private String srcIp;
    private String srcDns;
    private String protocol;
    private String dstIp;
    private String dstDns;
    private String description;

    // Populated by NodeClassifierService
    private NodeType srcType;
    private NodeType dstType;

    public String getSrcKey() {
        return srcDns != null && !srcDns.isBlank() ? srcDns : srcIp;
    }

    public String getDstKey() {
        return dstDns != null && !dstDns.isBlank() ? dstDns : dstIp;
    }

    public String getRecordKey() {
        return String.format("%s|%s|%s|%s", environment, srcIp, dstIp, protocol);
    }
}
