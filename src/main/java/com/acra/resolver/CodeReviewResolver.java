package com.acra.resolver;


import com.acra.model.CodeReview;
import com.acra.model.RuleSet;
import com.acra.service.CodeReviewService;
import com.acra.service.RuleSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CodeReviewResolver {
    private final CodeReviewService codeReviewService;
    private final RuleSetService ruleSetService;
    @Autowired
    public CodeReviewResolver(CodeReviewService codeReviewService, RuleSetService ruleSetService) {
        this.codeReviewService = codeReviewService;
        this.ruleSetService = ruleSetService;
    }

    @QueryMapping
    public CodeReview getReviewForPullRequest(@Argument String repoOwner, @Argument String repoName, @Argument int pullRequestNumber) {
        return codeReviewService.getReviewForPullRequest(repoOwner, repoName, pullRequestNumber);
    }

    @QueryMapping
    public List<CodeReview> getReviewHistory(@Argument String repoOwner, @Argument String repoName) {
        return codeReviewService.getReviewHistory(repoOwner, repoName);
    }

    @MutationMapping
    public CodeReview triggerReview(@Argument String repoOwner, @Argument String repoName, @Argument int pullRequestNumber) {
        return codeReviewService.initiateReview(repoOwner, repoName, pullRequestNumber);
    }

    @MutationMapping
    public RuleSet updateRuleSet(@Argument RuleSet ruleSet) {
        return ruleSetService.updateRuleSet(ruleSet);
    }
}