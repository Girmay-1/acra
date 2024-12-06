package com.acra.service;

import com.acra.dao.CodeReviewDao;
import com.acra.dao.ReviewCommentDao;
import com.acra.exception.ReviewException;
import com.acra.model.CodeReview;
import com.acra.model.ReviewComment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class CodeReviewService {
    private final CodeReviewDao reviewDao;
    private final ReviewCommentDao commentDao;
    private final GitHubService gitHubService;
    private final SonarQubeService sonarQubeService;

    public CodeReviewService(
            CodeReviewDao reviewDao,
            ReviewCommentDao commentDao,
            GitHubService gitHubService,
            SonarQubeService sonarQubeService) {
        this.reviewDao = reviewDao;
        this.commentDao = commentDao;
        this.gitHubService = gitHubService;
        this.sonarQubeService = sonarQubeService;
    }

    public CodeReview initiateReview(String repositoryUrl, String pullRequestNumber, String commitHash) {
        CodeReview review = new CodeReview(
            UUID.randomUUID(),
            repositoryUrl,
            pullRequestNumber,
            commitHash,
            "PENDING",
            LocalDateTime.now(),
            LocalDateTime.now(),
            null
        );
        
        review = reviewDao.create(review);
        startAnalysis(review);
        return review;
    }

    private void startAnalysis(CodeReview review) {
        CompletableFuture.runAsync(() -> {
            try {
                reviewDao.updateStatus(review.id(), "IN_PROGRESS", LocalDateTime.now());
                
                var codeBase = gitHubService.checkoutPullRequest(review.repositoryUrl(), review.pullRequestNumber());
                var comments = sonarQubeService.analyze(codeBase);
                
                for (ReviewComment comment : comments) {
                    commentDao.create(comment);
                }
                
                gitHubService.postComments(review.repositoryUrl(), review.pullRequestNumber(), comments);
                reviewDao.updateStatus(review.id(), "COMPLETED", LocalDateTime.now());
                
            } catch (Exception e) {
                reviewDao.updateStatus(review.id(), "FAILED", LocalDateTime.now());
                throw new ReviewException.AnalysisFailed("Code analysis failed", e);
            }
        });
    }

    public CodeReview getReview(UUID id) {
        return reviewDao.findById(id)
            .orElseThrow(() -> new ReviewException.NotFound("Review not found: " + id));
    }

    public List<CodeReview> getReviews(String repositoryUrl) {
        return reviewDao.findByRepository(repositoryUrl);
    }
}