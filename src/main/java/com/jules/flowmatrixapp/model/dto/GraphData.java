package com.jules.flowmatrixapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphData {
    private List<NodeDto> nodes;
    private List<EdgeDto> edges;
}
