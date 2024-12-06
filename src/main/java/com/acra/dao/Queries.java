package com.acra.dao;

public enum Queries {
    CREATE_REVIEW("""
        INSERT INTO code_reviews (id, repository_url, pull_request_number, commit_hash, status, created_at, updated_at, overall_score) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """),
    
    GET_REVIEW_BY_ID("""
        SELECT * FROM code_reviews WHERE id = ?
        """),
    
    GET_REVIEWS_BY_REPO("""
        SELECT * FROM code_reviews WHERE repository_url = ?
        """),
    
    UPDATE_REVIEW_STATUS("""
        UPDATE code_reviews SET status = ?, updated_at = ? WHERE id = ?
        """),
    
    CREATE_COMMENT("""
        INSERT INTO review_comments (id, review_id, file_path, line_number, message, severity, rule_id, created_at) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """),
    
    GET_COMMENTS_BY_REVIEW("""
        SELECT * FROM review_comments WHERE review_id = ?
        """);

    private final String query;

    Queries(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}