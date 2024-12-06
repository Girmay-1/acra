CREATE TABLE IF NOT EXISTS code_reviews (
    id UUID PRIMARY KEY,
    repository_url VARCHAR(255) NOT NULL,
    pull_request_number VARCHAR(50) NOT NULL,
    commit_hash VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    overall_score DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS review_comments (
    id UUID PRIMARY KEY,
    review_id UUID NOT NULL REFERENCES code_reviews(id),
    file_path VARCHAR(500) NOT NULL,
    line_number INTEGER NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    rule_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_review_comments_review_id ON review_comments(review_id);
CREATE INDEX IF NOT EXISTS idx_code_reviews_repo_pr ON code_reviews(repository_url, pull_request_number);