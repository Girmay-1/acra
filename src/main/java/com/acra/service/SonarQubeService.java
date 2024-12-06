package com.acra.service;

import com.acra.exception.ReviewException;
import com.acra.model.ReviewComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonarqube.ws.Issues.Issue;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.issues.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SonarQubeService {
    private static final Logger log = LoggerFactory.getLogger(SonarQubeService.class);
    
    private final WsClient client;
    private final String serverUrl;
    private final String token;
    private final String projectKey;

    public SonarQubeService(
            @Value("${sonarqube.url}") String serverUrl,
            @Value("${sonarqube.token}") String token,
            @Value("${sonarqube.projectKey:acra}") String projectKey) {
        this.serverUrl = serverUrl;
        this.token = token;
        this.projectKey = projectKey;
        
        HttpConnector connector = HttpConnector.newBuilder()
            .url(serverUrl)
            .token(token)
            .build();
        this.client = WsClientFactories.getDefault().newClient(connector);
        
        log.info("SonarQubeService initialized with server: {}", serverUrl);
    }

    public List<ReviewComment> analyze(Path codeBase) {
        try {
            log.info("Starting SonarQube analysis of {}", codeBase);
            
            String analysisId = runSonarScanner(codeBase);
            waitForAnalysisToComplete(analysisId);
            List<ReviewComment> comments = fetchIssues();
            
            log.info("Analysis completed. Found {} issues", comments.size());
            return comments;
            
        } catch (Exception e) {
            String message = "Failed to analyze code with SonarQube";
            log.error(message, e);
            throw new ReviewException.AnalysisFailed(message, e);
        }
    }

    private String runSonarScanner(Path codeBase) throws IOException, InterruptedException {
        log.debug("Running SonarQube scanner on {}", codeBase);
        
        ProcessBuilder pb = new ProcessBuilder(
            "sonar-scanner",
            "-Dsonar.projectKey=" + projectKey,
            "-Dsonar.sources=" + codeBase.toString(),
            "-Dsonar.host.url=" + serverUrl,
            "-Dsonar.login=" + token,
            "-Dsonar.java.binaries=" + codeBase.resolve("target/classes"),
            "-Dsonar.sourceEncoding=UTF-8"
        );
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            String error = "SonarQube scanner failed with exit code: " + exitCode;
            throw new ReviewException.AnalysisFailed(error, new RuntimeException(error));
        }
        
        return projectKey + "_" + System.currentTimeMillis();
    }

    private void waitForAnalysisToComplete(String analysisId) throws InterruptedException {
        log.debug("Waiting for analysis {} to complete", analysisId);
        TimeUnit.SECONDS.sleep(30);
    }

    private List<ReviewComment> fetchIssues() {
        log.debug("Fetching analysis results");
        List<ReviewComment> comments = new ArrayList<>();
        
        SearchRequest request = new SearchRequest()
            .setProjects(Arrays.asList(projectKey))
            .setTypes(Arrays.asList("BUG", "VULNERABILITY", "CODE_SMELL"));

        var response = client.issues().search(request);
        List<Issue> issues = response.getIssuesList();
        
        for (Issue issue : issues) {
            String severity = issue.getSeverity().name();
            comments.add(new ReviewComment(
                UUID.randomUUID(),
                null,
                issue.getComponent(), // This is the file path in SonarQube
                issue.getLine(),
                issue.getMessage(),
                mapSeverity(severity),
                "sonar:" + issue.getRule(),
                LocalDateTime.now()
            ));
        }
        
        return comments;
    }

    private String mapSeverity(String sonarSeverity) {
        if (sonarSeverity == null) {
            return "INFO";
        }
        
        switch (sonarSeverity.toUpperCase()) {
            case "BLOCKER":
            case "CRITICAL":
                return "ERROR";
            case "MAJOR":
                return "WARNING";
            default:
                return "INFO";
        }
    }
}