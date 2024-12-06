package com.acra.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record CodeReview(
    UUID id,
    String repositoryUrl,
    String pullRequestNumber,
    String commitHash,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Double overallScore
) {}