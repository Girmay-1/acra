package com.acra.exception;

public class ReviewException extends RuntimeException {
    public ReviewException(String message) {
        super(message);
    }

    public ReviewException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class NotFound extends ReviewException {
        public NotFound(String message) {
            super(message);
        }
    }

    public static class AnalysisFailed extends ReviewException {
        public AnalysisFailed(String message, Throwable cause) {
            super(message, cause);
        }
    }
}