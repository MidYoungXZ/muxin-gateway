package com.muxin.gateway.core.plus.common;

/**
 * @projectname: muxin-gateway
 * @filename: ErrorInfo
 * @author: yangxz
 * @data:2025/7/16 22:36
 * @description:
 */
public class ErrorInfo {
    private final ErrorType errorType;
    private final String errorCode;
    private final String errorMessage;
    private final Throwable originalException;
    private final long timestamp;
    private final String requestId;

    private ErrorInfo(ErrorInfo.Builder builder) {
        this.errorType = builder.errorType;
        this.errorCode = builder.errorCode;
        this.errorMessage = builder.errorMessage;
        this.originalException = builder.originalException;
        this.timestamp = builder.timestamp;
        this.requestId = builder.requestId;
    }

    public static ErrorInfo.Builder builder() {
        return new ErrorInfo.Builder();
    }

    // Getters
    public ErrorType getErrorType() {
        return errorType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Throwable getOriginalException() {
        return originalException;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public static class Builder {
        private ErrorType errorType;
        private String errorCode;
        private String errorMessage;
        private Throwable originalException;
        private long timestamp;
        private String requestId;

        public ErrorInfo.Builder errorType(ErrorType errorType) {
            this.errorType = errorType;
            return this;
        }

        public ErrorInfo.Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorInfo.Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public ErrorInfo.Builder originalException(Throwable originalException) {
            this.originalException = originalException;
            return this;
        }

        public ErrorInfo.Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorInfo.Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ErrorInfo build() {
            return new ErrorInfo(this);
        }
    }
}