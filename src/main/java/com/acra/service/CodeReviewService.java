package com.acra.service;

import com.acra.exception.ReviewNotFoundException;
import com.acra.model.*;
import com.acra.repository.CodeReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CodeReviewService {
    private static final Logger logger = LoggerFactory.getLogger(CodeReviewService.class);

    private final CodeReviewRepository repository;
    private final GitHubService gitHubService;

    public CodeReviewService(CodeReviewRepository repository, GitHubService gitHubService) {
        this.repository = repository;
        this.gitHubService = gitHubService;
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

            List<Issue> issues = performCodeAnalysis(files, diff);

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

    private List<Issue> performCodeAnalysis(List<String> files, String diff) {
        List<Issue> issues = new ArrayList<>();

        for (String file : files) {
            if (file.endsWith(".java")) {
                // Simple analysis for Java files
                List<String> lines = Arrays.asList(diff.split("\n"));
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line.startsWith("+")) { // New line added
                        if (line.length() > 120) {
                            issues.add(createIssue(IssueType.CODE_STYLE, IssueSeverity.LOW, file, i + 1, "Line length exceeds 120 characters"));
                        }
                        if (line.contains("System.out.println")) {
                            issues.add(createIssue(IssueType.CODE_STYLE, IssueSeverity.LOW, file, i + 1, "Avoid using System.out.println in production code"));
                        }
                        if (line.contains("catch (Exception e)")) {
                            issues.add(createIssue(IssueType.CODE_STYLE, IssueSeverity.MEDIUM, file, i + 1, "Catch specific exceptions instead of generic Exception"));
                        }
                    }
                }
            }
        }

        return issues;
    }
    private Issue createIssue(IssueType type, IssueSeverity severity, String file, int line, String message) {
        Issue issue = new Issue();
        issue.setType(type);
        issue.setSeverity(severity);
        issue.setFile(file);
        issue.setLine(line);
        issue.setMessage(message);
        return issue;
    }

    private float calculateCodeQualityScore(List<Issue> issues) {
        // TODO: Implement a more sophisticated scoring algorithm
        // This could involve weighing different types of issues, considering their severity, etc.
        return 100 - (issues.size() * 5); // Deduct 5 points for each issue
    }

    private float calculateSecurityScore(List<Issue> issues) {
        // TODO: Implement a more nuanced security scoring system
        // This could involve categorizing security issues by severity and impact
        return 100 - (issues.stream().filter(i -> i.getType() == IssueType.SECURITY_VULNERABILITY).count() * 10);
    }

    private float calculatePerformanceScore(List<Issue> issues) {
        // TODO: Implement a more comprehensive performance scoring system
        // This could involve analyzing complexity, potential bottlenecks, etc.
        return 100 - (issues.stream().filter(i -> i.getType() == IssueType.PERFORMANCE_ISSUE).count() * 7);
    }

    public CodeReview getReviewForPullRequest(String repoOwner, String repoName, int pullRequestNumber) {
        return repository.findByRepoOwnerAndRepoName(repoOwner, repoName).stream()
                .filter(review -> review.getPullRequestNumber() == pullRequestNumber)
                .findFirst()
                .orElseThrow(() -> new ReviewNotFoundException("Review not found for the given pull request"));
    }

    public List<CodeReview> getReviewHistory(String repoOwner, String repoName) {
        return repository.findByRepoOwnerAndRepoName(repoOwner, repoName);
    }
}