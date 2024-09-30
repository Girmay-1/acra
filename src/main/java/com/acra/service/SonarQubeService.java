package com.acra.service;
import com.acra.model.Issue;
import com.acra.model.IssueSeverity;
import com.acra.model.IssueType;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.Issues.SearchWsResponse;
import org.sonarqube.ws.client.issues.SearchRequest;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SonarQubeService {
    private final WsClient wsClient;

    public SonarQubeService(@Value("${sonar.url}") String sonarUrl,
                            @Value("${sonar.token}") String sonarToken) {
        HttpConnector connector = HttpConnector.newBuilder()
                .url(sonarUrl)
                .token(sonarToken)
                .build();
        this.wsClient = WsClientFactories.getDefault().newClient(connector);
    }

    public List<Issue> analyzeJavaFile(String projectKey, String filePath) {
        return analyzeFile(projectKey, filePath, "java");
    }

    public List<Issue> analyzePythonFile(String projectKey, String filePath) {
        return analyzeFile(projectKey, filePath, "py");
    }

    public List<Issue> analyzeJavaScriptFile(String projectKey, String filePath) {
        return analyzeFile(projectKey, filePath, "js");
    }

    private List<Issue> analyzeFile(String projectKey, String filePath, String language) {
        SearchRequest request = new SearchRequest()
                .setProjects(Collections.singletonList(projectKey))
                .setComponentKeys(Collections.singletonList(filePath))
                .setLanguages(Collections.singletonList(language))
                .setSeverities(Arrays.asList("BLOCKER", "CRITICAL", "MAJOR", "MINOR", "INFO"));

        SearchWsResponse response = wsClient.issues().search(request);

        return response.getIssuesList().stream()
                .map(this::convertSonarIssueToAcraIssue)
                .collect(Collectors.toList());
    }

    private Issue convertSonarIssueToAcraIssue(org.sonarqube.ws.Issues.Issue sonarIssue) {
        Issue acraIssue = new Issue();
        acraIssue.setFile(sonarIssue.getComponent());
        acraIssue.setLine(sonarIssue.getLine());
        acraIssue.setMessage(sonarIssue.getMessage());
        acraIssue.setSeverity(mapSeverity(String.valueOf(sonarIssue.getSeverity())));
        acraIssue.setType(mapType(String.valueOf(sonarIssue.getType())));
        return acraIssue;
    }

    private IssueSeverity mapSeverity(String sonarSeverity) {
        switch (sonarSeverity) {
            case "BLOCKER":
            case "CRITICAL":
                return IssueSeverity.CRITICAL;
            case "MAJOR":
                return IssueSeverity.HIGH;
            case "MINOR":
                return IssueSeverity.MEDIUM;
            case "INFO":
                return IssueSeverity.LOW;
            default:
                return IssueSeverity.MEDIUM; // Default case
        }
    }

    private IssueType mapType(String sonarType) {
        switch (sonarType) {
            case "BUG":
                return IssueType.POTENTIAL_BUG;
            case "VULNERABILITY":
                return IssueType.SECURITY_VULNERABILITY;
            case "CODE_SMELL":
                return IssueType.CODE_STYLE;
            case "SECURITY_HOTSPOT":
                return IssueType.SECURITY_VULNERABILITY;
            default:
                return IssueType.CODE_STYLE; // Default case
        }
    }
}