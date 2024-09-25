package com.acra.model;

import lombok.Data;
import java.time.Instant;

@Data
public class PullRequest {
    private int number;
    private String title;
    private String body;
    private String user;
    private Instant createdAt;
    private Instant updatedAt;
    private String state;
    private String repoOwner;
    private String repoName;
}