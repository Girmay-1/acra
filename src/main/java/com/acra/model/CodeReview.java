package com.acra.model;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class CodeReview {
    private Long id;
    private Integer pullRequestNumber;
    private ReviewStatus status;
    private Float codeQualityScore;
    private Float securityScore;
    private Float performanceScore;
    private List<Issue> issues;
    private Instant createdAt;
    private String repoOwner;
    private String repoName;
}