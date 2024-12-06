package com.acra.graphql;

import com.acra.model.CodeReview;
import com.acra.service.CodeReviewService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class CodeReviewResolver {
    private final CodeReviewService reviewService;

    public CodeReviewResolver(CodeReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @QueryMapping
    public CodeReview codeReview(@Argument String id) {
        return reviewService.getReview(UUID.fromString(id));
    }

    @QueryMapping
    public List<CodeReview> codeReviews(@Argument String repositoryUrl) {
        return reviewService.getReviews(repositoryUrl);
    }

    @MutationMapping
    public CodeReview initiateCodeReview(@Argument CodeReviewInput input) {
        return reviewService.initiateReview(input.repositoryUrl(), input.pullRequestNumber(), input.commitHash());
    }

    record CodeReviewInput(String repositoryUrl, String pullRequestNumber, String commitHash) {}
}