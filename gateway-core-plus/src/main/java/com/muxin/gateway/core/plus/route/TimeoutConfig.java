package com.muxin.gateway.core.plus.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * 超时配置类
 *
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeoutConfig {
    
    /**
     * 连接超时时间
     */
    private Duration connection;
    
    /**
     * 请求超时时间
     */
    private Duration request;
    
    /**
     * 总超时时间（包含重试）
     */
    private Duration total;
    
    /**
     * 读取超时时间
     */
    private Duration read;
    
    /**
     * 写入超时时间
     */
    private Duration write;
    
    /**
     * 熔断器超时时间
     */
    private Duration circuitBreaker;
    
    /**
     * 获取指定类型的超时时间
     */
    public Duration getTimeout(TimeoutType type) {
        switch (type) {
            case CONNECTION:
                return connection;
            case REQUEST:
                return request;
            case TOTAL:
                return total;
            case READ:
                return read;
            case WRITE:
                return write;
            case CIRCUIT_BREAKER:
                return circuitBreaker;
            default:
                return null;
        }
    }
    
    /**
     * 设置指定类型的超时时间
     */
    public void setTimeout(TimeoutType type, Duration timeout) {
        switch (type) {
            case CONNECTION:
                this.connection = timeout;
                break;
            case REQUEST:
                this.request = timeout;
                break;
            case TOTAL:
                this.total = timeout;
                break;
            case READ:
                this.read = timeout;
                break;
            case WRITE:
                this.write = timeout;
                break;
            case CIRCUIT_BREAKER:
                this.circuitBreaker = timeout;
                break;
        }
    }
    
    /**
     * 检查是否设置了指定类型的超时时间
     */
    public boolean hasTimeout(TimeoutType type) {
        return getTimeout(type) != null;
    }
    
    /**
     * 获取超时时间，如果未设置则返回默认值
     */
    public Duration getTimeoutOrDefault(TimeoutType type, Duration defaultValue) {
        Duration timeout = getTimeout(type);
        return timeout != null ? timeout : defaultValue;
    }
    
    /**
     * 创建默认超时配置
     */
    public static TimeoutConfig defaultConfig() {
        return TimeoutConfig.builder()
                .connection(Duration.ofSeconds(5))
                .request(Duration.ofSeconds(30))
                .total(Duration.ofSeconds(60))
                .read(Duration.ofSeconds(30))
                .write(Duration.ofSeconds(10))
                .circuitBreaker(Duration.ofSeconds(60))
                .build();
    }
} 