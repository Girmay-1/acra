package com.acra.acra.resolver;

import com.acra.model.CodeReview;
import com.acra.model.RuleSet;
import com.acra.resolver.CodeReviewResolver;
import com.acra.service.CodeReviewService;
import com.acra.service.RuleSetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CodeReviewResolverTest {

    @Mock
    private CodeReviewService codeReviewService;

    @Mock
    private RuleSetService ruleSetService;

    private CodeReviewResolver codeReviewResolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        codeReviewResolver = new CodeReviewResolver(codeReviewService, ruleSetService);
    }

    @Test
    void testGetReviewForPullRequest() {
        String repoOwner = "owner";
        String repoName = "repo";
        int pullRequestNumber = 1;
        CodeReview expectedReview = new CodeReview(); // Initialize with expected values

        when(codeReviewService.getReviewForPullRequest(repoOwner, repoName, pullRequestNumber))
                .thenReturn(expectedReview);

        CodeReview actualReview = codeReviewResolver.getReviewForPullRequest(repoOwner, repoName, pullRequestNumber);

        assertEquals(expectedReview, actualReview);
        verify(codeReviewService).getReviewForPullRequest(repoOwner, repoName, pullRequestNumber);
    }

    @Test
    void testGetReviewHistory() {
        String repoOwner = "owner";
        String repoName = "repo";
        List<CodeReview> expectedHistory = Arrays.asList(new CodeReview(), new CodeReview());

        when(codeReviewService.getReviewHistory(repoOwner, repoName)).thenReturn(expectedHistory);

        List<CodeReview> actualHistory = codeReviewResolver.getReviewHistory(repoOwner, repoName);

        assertEquals(expectedHistory, actualHistory);
        verify(codeReviewService).getReviewHistory(repoOwner, repoName);
    }

    @Test
    void testTriggerReview() {
        String repoOwner = "owner";
        String repoName = "repo";
        int pullRequestNumber = 1;
        CodeReview expectedReview = new CodeReview(); // Initialize with expected values

        when(codeReviewService.initiateReview(repoOwner, repoName, pullRequestNumber))
                .thenReturn(expectedReview);

        CodeReview actualReview = codeReviewResolver.triggerReview(repoOwner, repoName, pullRequestNumber);

        assertEquals(expectedReview, actualReview);
        verify(codeReviewService).initiateReview(repoOwner, repoName, pullRequestNumber);
    }

    @Test
    void testUpdateRuleSet() {
        RuleSet inputRuleSet = new RuleSet(); // Initialize with input values
        RuleSet expectedRuleSet = new RuleSet(); // Initialize with expected values

        when(ruleSetService.updateRuleSet(inputRuleSet)).thenReturn(expectedRuleSet);

        RuleSet actualRuleSet = codeReviewResolver.updateRuleSet(inputRuleSet);

        assertEquals(expectedRuleSet, actualRuleSet);
        verify(ruleSetService).updateRuleSet(inputRuleSet);
    }
}