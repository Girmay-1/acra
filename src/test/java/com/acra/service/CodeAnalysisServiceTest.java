package com.acra.service;

import com.acra.model.CodeReview;
import com.acra.model.ReviewComment;
import com.acra.test.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

class CodeAnalysisServiceTest extends BaseIntegrationTest {

    @Autowired
    private CodeAnalysisService codeAnalysisService;

    @MockBean
    private GitHubService githubService;

    @MockBean
    private SonarQubeService sonarQubeService;

    @MockBean
    private CheckStyleService checkStyleService;

    @MockBean
    private SpotBugsService spotBugsService;

    @Test
    void analyzeCode_ShouldAggregateResults() {
        // Given
        String repoUrl = "https://github.com/test/repo";
        String prNumber = "123";
        Path mockedPath = Path.of("/tmp/test");

        ReviewComment sonarComment = new ReviewComment();
        sonarComment.setFilePath("src/main/java/Test.java");
        sonarComment.setLineNumber(10);
        sonarComment.setMessage("SonarQube issue");

        ReviewComment checkstyleComment = new ReviewComment();
        checkstyleComment.setFilePath("src/main/java/Test.java");
        checkstyleComment.setLineNumber(20);
        checkstyleComment.setMessage("Checkstyle issue");

        // When
        when(githubService.checkoutPullRequest(repoUrl, prNumber)).thenReturn(mockedPath);
        when(sonarQubeService.analyze(any())).thenReturn(List.of(sonarComment));
        when(checkStyleService.analyze(any())).thenReturn(List.of(checkstyleComment));
        when(spotBugsService.analyze(any())).thenReturn(List.of());

        CompletableFuture<CodeReview> future = codeAnalysisService.analyzeCode(repoUrl, prNumber);
        CodeReview result = future.join();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRepositoryUrl()).isEqualTo(repoUrl);
        assertThat(result.getPullRequestNumber()).isEqualTo(prNumber);
        assertThat(result.getOverallScore()).isNotNull();
    }
}