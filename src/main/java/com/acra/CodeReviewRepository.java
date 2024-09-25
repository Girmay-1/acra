package com.acra;
import com.acra.model.CodeReview;
import com.acra.model.ReviewStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class CodeReviewRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<CodeReview> rowMapper = (rs, rowNum) -> {
        CodeReview review = new CodeReview();
        review.setId(rs.getLong("id"));
        review.setPullRequestNumber(rs.getInt("pull_request_number"));
        review.setStatus(ReviewStatus.valueOf(rs.getString("status")));
        review.setCodeQualityScore(rs.getFloat("code_quality_score"));
        review.setSecurityScore(rs.getFloat("security_score"));
        review.setPerformanceScore(rs.getFloat("performance_score"));
        review.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        // Note: Issues are not loaded here for simplicity
        return review;
    };

    public CodeReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CodeReview save(CodeReview review) {
        String sql = "INSERT INTO code_reviews (pull_request_number, status, code_quality_score, security_score, performance_score, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class,
                review.getPullRequestNumber(),
                review.getStatus().toString(),
                review.getCodeQualityScore(),
                review.getSecurityScore(),
                review.getPerformanceScore(),
                java.sql.Timestamp.from(review.getCreatedAt()));
        review.setId(id);
        return review;
    }

    public Optional<CodeReview> findById(Long id) {
        String sql = "SELECT * FROM code_reviews WHERE id = ?";
        List<CodeReview> reviews = jdbcTemplate.query(sql, rowMapper, id);
        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.get(0));
    }

    public List<CodeReview> findByRepoOwnerAndRepoName(String repoOwner, String repoName) {
        String sql = "SELECT * FROM code_reviews WHERE repo_owner = ? AND repo_name = ?";
        return jdbcTemplate.query(sql, rowMapper, repoOwner, repoName);
    }

    public void update(CodeReview review) {
        String sql = "UPDATE code_reviews SET status = ?, code_quality_score = ?, security_score = ?, performance_score = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                review.getStatus().toString(),
                review.getCodeQualityScore(),
                review.getSecurityScore(),
                review.getPerformanceScore(),
                review.getId());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM code_reviews WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}