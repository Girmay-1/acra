package com.acra.service;

import com.acra.exception.ReviewException;
import com.acra.model.ReviewComment;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
public class GitHubService {
    private final GitHub gitHub;
    private final String token;
    private final Path workspaceDir;

    public GitHubService(
            @Value("${github.token}") String token,
            @Value("${github.workspace:/tmp/acra}") String workspace) throws IOException {
        this.token = token;
        this.gitHub = GitHub.connectUsingOAuth(token);
        this.workspaceDir = Path.of(workspace);
        
        log.info("GitHubService initialized with workspace: {}", workspace);
    }

    public Path checkoutPullRequest(String repositoryUrl, String pullRequestNumber) {
        try {
            log.info("Checking out PR #{} from {}", pullRequestNumber, repositoryUrl);
            
            String repoName = extractRepoName(repositoryUrl);
            GHRepository repository = gitHub.getRepository(repoName);
            GHPullRequest pullRequest = repository.getPullRequest(Integer.parseInt(pullRequestNumber));
            
            Path checkoutDir = workspaceDir.resolve(repoName).resolve(pullRequestNumber);
            checkoutDir.toFile().mkdirs();

            log.debug("Cloning repository to {}", checkoutDir);
            
            // Clone the repository
            try (Git git = Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(checkoutDir.toFile())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                    .call()) {

                log.debug("Fetching PR branch");
                // Fetch PR branch
                git.fetch()
                    .setRemote("origin")
                    .setRefSpecs("+refs/pull/" + pullRequestNumber + "/head:refs/remotes/origin/pr/" + pullRequestNumber)
                    .call();

                log.debug("Checking out PR branch");
                // Checkout PR branch
                git.checkout()
                    .setName("pr/" + pullRequestNumber)
                    .setStartPoint("origin/pr/" + pullRequestNumber)
                    .call();
            }
            
            log.info("Successfully checked out PR #{} to {}", pullRequestNumber, checkoutDir);
            return checkoutDir;
            
        } catch (IOException | GitAPIException e) {
            String message = String.format("Failed to checkout PR #%s from %s", pullRequestNumber, repositoryUrl);
            log.error(message, e);
            throw new ReviewException.AnalysisFailed(message, e);
        }
    }

    public void postComments(String repositoryUrl, String pullRequestNumber, List<ReviewComment> comments) {
        try {
            log.info("Posting {} comments to PR #{} in {}", comments.size(), pullRequestNumber, repositoryUrl);
            
            String repoName = extractRepoName(repositoryUrl);
            GHRepository repository = gitHub.getRepository(repoName);
            GHPullRequest pullRequest = repository.getPullRequest(Integer.parseInt(pullRequestNumber));

            for (ReviewComment comment : comments) {
                log.debug("Posting comment for file: {} line: {}", comment.filePath(), comment.lineNumber());
                pullRequest.createReviewComment(
                    formatComment(comment),
                    pullRequest.getHead().getSha(),
                    comment.filePath(),
                    comment.lineNumber()
                );
            }
            
            log.info("Successfully posted all comments");
            
        } catch (IOException e) {
            String message = String.format("Failed to post comments to PR #%s in %s", pullRequestNumber, repositoryUrl);
            log.error(message, e);
            throw new ReviewException.AnalysisFailed(message, e);
        }
    }

    private String formatComment(ReviewComment comment) {
        return String.format("**%s**: %s%n%nRule: `%s`", 
            comment.severity().toUpperCase(),
            comment.message(),
            comment.ruleId());
    }

    private String extractRepoName(String repositoryUrl) {
        return repositoryUrl.replace("https://github.com/", "");
    }
}