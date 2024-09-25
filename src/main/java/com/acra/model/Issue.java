package com.acra.model;

import lombok.Data;

@Data
public class Issue {
    private Long id;
    private IssueType type;
    private IssueSeverity severity;
    private String file;
    private Integer line;
    private String message;
}

