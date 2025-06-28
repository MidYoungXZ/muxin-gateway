package com.muxin.gateway.core.plus.node.health;

/**
 * 健康检查结果
 *
 * @author muxin
 */
public class HealthCheckResult {
    
    private final boolean healthy;
    private final String message;
    private final long responseTime;
    private final long timestamp;
    
    public HealthCheckResult(boolean healthy, String message, long responseTime) {
        this.healthy = healthy;
        this.message = message;
        this.responseTime = responseTime;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 创建成功的健康检查结果
     */
    public static HealthCheckResult success(long responseTime) {
        return new HealthCheckResult(true, "健康检查通过", responseTime);
    }
    
    /**
     * 创建失败的健康检查结果
     */
    public static HealthCheckResult failure(String message, long responseTime) {
        return new HealthCheckResult(false, message, responseTime);
    }
    
    public boolean isHealthy() {
        return healthy;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getResponseTime() {
        return responseTime;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("HealthCheckResult{healthy=%s, message='%s', responseTime=%dms, timestamp=%d}", 
            healthy, message, responseTime, timestamp);
    }
} 