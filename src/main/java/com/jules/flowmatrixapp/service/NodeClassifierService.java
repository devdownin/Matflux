package com.jules.flowmatrixapp.service;

import com.jules.flowmatrixapp.config.NodeClassifierProperties;
import com.jules.flowmatrixapp.model.FlowRecord;
import com.jules.flowmatrixapp.model.NodeType;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NodeClassifierService {

    private final Map<NodeType, Pattern> dnsNamePatterns;
    private final Map<NodeType, Pattern> protocolPatterns;

    public NodeClassifierService(NodeClassifierProperties properties) {
        this.dnsNamePatterns = compilePatterns(properties.getMap());
        this.protocolPatterns = compilePatterns(properties.getByProtocol());
    }

    private Map<NodeType, Pattern> compilePatterns(Map<String, String> stringPatterns) {
        return stringPatterns.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> NodeType.valueOf(entry.getKey().toUpperCase()),
                        entry -> Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE)
                ));
    }

    public void classifyNodes(FlowRecord record) {
        record.setSrcType(classify(record.getSrcDns(), record.getProtocol(), record.getDescription()));
        record.setDstType(classify(record.getDstDns(), record.getProtocol(), record.getDescription()));
    }

    private NodeType classify(String dns, String protocol, String description) {
        // 1. By DNS name (priority)
        if (dns != null && !dns.isBlank()) {
            for (Map.Entry<NodeType, Pattern> entry : dnsNamePatterns.entrySet()) {
                if (entry.getValue().matcher(dns).matches()) {
                    return entry.getKey();
                }
            }
        }

        // 2. Fallback to protocol/description
        String fallbackText = (protocol != null ? protocol : "") + " " + (description != null ? description : "");
        if (!fallbackText.isBlank()) {
            for (Map.Entry<NodeType, Pattern> entry : protocolPatterns.entrySet()) {
                if (entry.getValue().matcher(fallbackText).matches()) {
                    return entry.getKey();
                }
            }
        }

        return NodeType.UNKNOWN;
    }
}
