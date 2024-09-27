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
        // TODO: Implement actual code analysis logic
        // This might involve parsing the diff, analyzing each file, and applying coding rules
        // You may want to integrate with a code analysis tool or implement custom logic

        Issue sampleIssue = new Issue();
        sampleIssue.setType(IssueType.CODE_STYLE);
        sampleIssue.setSeverity(IssueSeverity.LOW);
        sampleIssue.setFile("file.java");
        sampleIssue.setLine(10);
        sampleIssue.setMessage("Sample issue: Incorrect indentation");
        issues.add(sampleIssue);

        return issues;
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