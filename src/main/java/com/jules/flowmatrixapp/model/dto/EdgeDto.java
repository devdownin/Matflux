package com.jules.flowmatrixapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EdgeDto {
    private String from;
    private String to;
    private String label;
    private String title;
    private Map<String, String> color; // For highlighting
}
