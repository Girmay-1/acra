package com.acra.service;

import com.acra.exception.ReviewNotFoundException;
import com.acra.model.*;
import com.acra.repository.CodeReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CodeReviewService {
    private static final Logger logger = LoggerFactory.getLogger(CodeReviewService.class);

    private final CodeReviewRepository repository;
    private final GitHubService gitHubService;

    private final CheckStyleService checkStyleService;

    private final SonarQubeService sonarQubeService;

    @Autowired
    public CodeReviewService(CodeReviewRepository repository, GitHubService gitHubService, CheckStyleService checkStyleService, SonarQubeService sonarQubeService) {
        this.repository = repository;
        this.gitHubService = gitHubService;
        this.checkStyleService = checkStyleService;
        this.sonarQubeService = sonarQubeService;
    }

    public CodeReview initiateReview(String repoOwner, String repoName, int pullRequestNumber) {
        PullRequest pullRequest = gitHubService.getPullRequest(repoOwner, repoName, pullRequestNumber);

        CodeReview review = new CodeReview();
        review.setPullRequestNumber(pullRequestNumber);
        review.setStatus(ReviewStatus.IN_PROGRESS);
        review.setCreatedAt(Instant.now());
        review.setRepoOwner(repoOwner);
        review.setRepoName(repoName);

        review = repository.save(review);

        asyncReviewProcess(review, pullRequest);

        return review;
    }

    @Async
    public void asyncReviewProcess(CodeReview review, PullRequest pullRequest) {
        try {
            List<String> files = gitHubService.getPullRequestFiles(review.getRepoOwner(), review.getRepoName(), review.getPullRequestNumber());
            String diff = gitHubService.getPullRequestDiff(review.getRepoOwner(), review.getRepoName(), review.getPullRequestNumber());

            List<CompletableFuture<List<Issue>>> futureIssues = files.parallelStream()
                    .map(file -> CompletableFuture.supplyAsync(() ->
                            analyzeFile(review.getRepoOwner(), review.getRepoName(), file, diff)))
                    .collect(Collectors.toList());

            List<Issue> issues = futureIssues.stream()
                    .flatMap(future -> future.join().stream())
                    .collect(Collectors.toList());

            float codeQualityScore = calculateCodeQualityScore(issues);
            float securityScore = calculateSecurityScore(issues);
            float performanceScore = calculatePerformanceScore(issues);

            review.setIssues(issues);
            review.setCodeQualityScore(codeQualityScore);
            review.setSecurityScore(securityScore);
            review.setPerformanceScore(performanceScore);
            review.setStatus(ReviewStatus.COMPLETED);

            repository.update(review);
        } catch (Exception e) {
            logger.error("Error during async review process", e);
            review.setStatus(ReviewStatus.FAILED);
            repository.update(review);
        }
    }

    private List<Issue> analyzeFile(String repoOwner, String repoName, String file, String diff) {

        List<Issue> issues = new ArrayList<>();
        String projectKey  = repoOwner + "_" + repoName;
        if (file.endsWith(".java")) {
            issues.addAll(checkStyleService.analyzeJavaFile(file, diff));
            issues.addAll(sonarQubeService.analyzeJavaFile(projectKey, file));
        } else if (file.endsWith(".py")) {
            issues.addAll(sonarQubeService.analyzePythonFile(projectKey, file));
        } else if (file.endsWith(".js")) {
            issues.addAll(sonarQubeService.analyzeJavaScriptFile(projectKey, file));
        }
        // Add more file type checks as needed
        return issues;
    }

    private float calculateCodeQualityScore(List<Issue> issues) {
        int weightedIssues = issues.stream()
                .mapToInt(issue -> getWeightForSeverity(issue.getSeverity()))
                .sum();

        return Math.max(0, 100 - (weightedIssues * 2));
    }

    private int getWeightForSeverity(IssueSeverity severity) {
        switch (severity) {
            case LOW:
                return 1;
            case MEDIUM:
                return 3;
            case HIGH:
                return 5;
            case CRITICAL:
                return 10;
            default:
                return 0;
        }
    }


    private float calculateSecurityScore(List<Issue> issues) {
        long securityIssues = issues.stream()
                .filter(i -> i.getType() == IssueType.SECURITY_VULNERABILITY)
                .count();

        return Math.max(0, 100 - (securityIssues * 20));
    }

    private float calculatePerformanceScore(List<Issue> issues) {
        long performanceIssues = issues.stream()
                .filter(i -> i.getType() == IssueType.PERFORMANCE_ISSUE)
                .count();

        return Math.max(0, 100 - (performanceIssues * 15));
    }

    @Cacheable(value = "reviewCache", key = "{#repoOwner, #repoName, #pullRequestNumber}")
    public CodeReview getReviewForPullRequest(String repoOwner, String repoName, int pullRequestNumber) {
        return repository.findByRepoOwnerAndRepoName(repoOwner, repoName).stream()
                .filter(review -> review.getPullRequestNumber() == pullRequestNumber)
                .findFirst()
                .orElseThrow(() -> new ReviewNotFoundException("Review not found for the given pull request"));
    }

    @Cacheable(value = "reviewHistoryCache", key = "{#repoOwner, #repoName}")
    public List<CodeReview> getReviewHistory(String repoOwner, String repoName) {
        return repository.findByRepoOwnerAndRepoName(repoOwner, repoName);
    }
}