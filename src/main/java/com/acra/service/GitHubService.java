package com.acra.service;

import com.acra.model.PullRequest;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubService {

    private final GitHub github;

    public GitHubService(@Value("${github.token}") String githubToken) throws IOException {
        this.github = new GitHubBuilder().withOAuthToken(githubToken).build();
    }

    public PullRequest getPullRequest(String repoOwner, String repoName, int pullRequestNumber) {
        try {
            GHRepository repository = github.getRepository(repoOwner + "/" + repoName);
            GHPullRequest ghPullRequest = repository.getPullRequest(pullRequestNumber);

            return convertToPullRequest(ghPullRequest);
        } catch (IOException e) {
            throw new RuntimeException("Error fetching pull request from GitHub", e);
        }
    }

    public List<String> getPullRequestFiles(String repoOwner, String repoName, int pullRequestNumber) {
        try {
            GHRepository repository = github.getRepository(repoOwner + "/" + repoName);
            GHPullRequest ghPullRequest = repository.getPullRequest(pullRequestNumber);

            return ghPullRequest.listFiles().toList()
                    .stream()
                    .map(GHPullRequestFileDetail::getFilename)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error fetching pull request files from GitHub", e);
        }
    }

    public String getPullRequestDiff(String repoOwner, String repoName, int pullRequestNumber) {
        try {
            GHRepository repository = github.getRepository(repoOwner + "/" + repoName);
            GHPullRequest ghPullRequest = repository.getPullRequest(pullRequestNumber);

            return ghPullRequest.getDiffUrl().toString();
        } catch (IOException e) {
            throw new RuntimeException("Error fetching pull request diff from GitHub", e);
        }
    }

    private PullRequest convertToPullRequest(GHPullRequest ghPullRequest) throws IOException {
        PullRequest pullRequest = new PullRequest();
        pullRequest.setNumber(ghPullRequest.getNumber());
        pullRequest.setTitle(ghPullRequest.getTitle());
        pullRequest.setBody(ghPullRequest.getBody());
        pullRequest.setUser(ghPullRequest.getUser().getLogin());
        pullRequest.setCreatedAt(ghPullRequest.getCreatedAt().toInstant());
        pullRequest.setUpdatedAt(ghPullRequest.getUpdatedAt().toInstant());
        pullRequest.setState(ghPullRequest.getState().name());
        return pullRequest;
    }
}