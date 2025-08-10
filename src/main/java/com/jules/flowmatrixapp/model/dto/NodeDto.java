package com.jules.flowmatrixapp.model.dto;

import com.jules.flowmatrixapp.model.NodeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeDto {
    private String id;
    private String label;
    private String group; // Corresponds to NodeType for vis.js grouping/styling
    private String title;
    private String icon;
    private String ip;
    private String dns;
    private NodeType type;
}
