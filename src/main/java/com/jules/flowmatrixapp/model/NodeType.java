package com.jules.flowmatrixapp.model;

import lombok.Getter;

@Getter
public enum NodeType {
    WEB("public", "#0ea5e9"),
    APP("developer_board", "#8b5cf6"),
    DATABASE("database", "#22c55e"),
    DATABASE_ORACLE("storage", "#f97316"),
    CACHE("bolt", "#06b6d4"),
    QUEUE("sync_alt", "#f59e0b"),
    KAFKA("hub", "#7c3aed"),
    ELK("search", "#10b981"),
    UNKNOWN("device_unknown", "#64748b");

    private final String icon;
    private final String color;

    NodeType(String icon, String color) {
        this.icon = icon;
        this.color = color;
    }
}
