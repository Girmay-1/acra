package com.acra.dao;

import com.acra.model.CodeReview;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CodeReviewDao {
    private final JdbcTemplate jdbc;

    public CodeReviewDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<CodeReview> rowMapper = (rs, rowNum) -> new CodeReview(
        UUID.fromString(rs.getString("id")),
        rs.getString("repository_url"),
        rs.getString("pull_request_number"),
        rs.getString("commit_hash"),
        rs.getString("status"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime(),
        rs.getDouble("overall_score")
    );

    public CodeReview create(CodeReview review) {
        jdbc.update(Queries.CREATE_REVIEW.getQuery(),
            review.id().toString(),
            review.repositoryUrl(),
            review.pullRequestNumber(),
            review.commitHash(),
            review.status(),
            Timestamp.valueOf(review.createdAt()),
            Timestamp.valueOf(review.updatedAt()),
            review.overallScore()
        );
        return review;
    }

    public Optional<CodeReview> findById(UUID id) {
        return jdbc.query(
            Queries.GET_REVIEW_BY_ID.getQuery(),
            rowMapper,
            id.toString()
        ).stream().findFirst();
    }

    public List<CodeReview> findByRepository(String repositoryUrl) {
        return jdbc.query(
            Queries.GET_REVIEWS_BY_REPO.getQuery(),
            rowMapper,
            repositoryUrl
        );
    }

    public void updateStatus(UUID id, String status, LocalDateTime updatedAt) {
        jdbc.update(
            Queries.UPDATE_REVIEW_STATUS.getQuery(),
            status,
            Timestamp.valueOf(updatedAt),
            id.toString()
        );
    }
}