package com.acra.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewComment(
    UUID id,
    UUID reviewId,
    String filePath,
    int lineNumber,
    String message,
    String severity,
    String ruleId,
    LocalDateTime createdAt
) {}