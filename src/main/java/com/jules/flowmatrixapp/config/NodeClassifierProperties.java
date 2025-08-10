package com.jules.flowmatrixapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "node.classifier")
@Data
public class NodeClassifierProperties {
    private Map<String, String> map = new HashMap<>();
    private Map<String, String> byProtocol = new HashMap<>();
}
