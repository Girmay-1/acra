package com.acra.dao;

import com.acra.model.ReviewComment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.UUID;

@Repository
public class ReviewCommentDao {
    private final JdbcTemplate jdbc;

    public ReviewCommentDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<ReviewComment> rowMapper = (rs, rowNum) -> new ReviewComment(
        UUID.fromString(rs.getString("id")),
        UUID.fromString(rs.getString("review_id")),
        rs.getString("file_path"),
        rs.getInt("line_number"),
        rs.getString("message"),
        rs.getString("severity"),
        rs.getString("rule_id"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );

    public ReviewComment create(ReviewComment comment) {
        jdbc.update(Queries.CREATE_COMMENT.getQuery(),
            comment.id().toString(),
            comment.reviewId().toString(),
            comment.filePath(),
            comment.lineNumber(),
            comment.message(),
            comment.severity(),
            comment.ruleId(),
            Timestamp.valueOf(comment.createdAt())
        );
        return comment;
    }
}