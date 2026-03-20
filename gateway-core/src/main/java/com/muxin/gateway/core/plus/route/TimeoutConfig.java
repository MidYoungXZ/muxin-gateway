package com.muxin.gateway.core.plus.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeoutConfig {
    
    public static final long DEFAULT_CONNECTION = 5000L;
    public static final long DEFAULT_REQUEST = 30000L;
    public static final long DEFAULT_TOTAL = 60000L;
    public static final long DEFAULT_READ = 30000L;
    public static final long DEFAULT_WRITE = 10000L;
    public static final long DEFAULT_CIRCUIT_BREAKER = 60000L;
    
    private Long connection;
    private Long request;
    private Long total;
    private Long read;
    private Long write;
    private Long circuitBreaker;
    
    public Long getConnection() {
        return connection;
    }
    
    public Long getRequest() {
        return request;
    }
    
    public Long getTotal() {
        return total;
    }
    
    public Long getRead() {
        return read;
    }
    
    public Long getWrite() {
        return write;
    }
    
    public Long getCircuitBreaker() {
        return circuitBreaker;
    }
    
    public Long getTimeout(TimeoutType type) {
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
    
    public void setTimeout(TimeoutType type, Long timeout) {
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
    
    public boolean hasTimeout(TimeoutType type) {
        return getTimeout(type) != null;
    }
    
    public Long getTimeoutOrDefault(TimeoutType type, Long defaultValue) {
        Long timeout = getTimeout(type);
        return timeout != null ? timeout : defaultValue;
    }
    
    public static TimeoutConfig defaultConfig() {
        return TimeoutConfig.builder()
                .connection(DEFAULT_CONNECTION)
                .request(DEFAULT_REQUEST)
                .total(DEFAULT_TOTAL)
                .read(DEFAULT_READ)
                .write(DEFAULT_WRITE)
                .circuitBreaker(DEFAULT_CIRCUIT_BREAKER)
                .build();
    }

    public static long getDefault(TimeoutType type) {
        return switch (type) {
            case CONNECTION -> DEFAULT_CONNECTION;
            case REQUEST -> DEFAULT_REQUEST;
            case TOTAL -> DEFAULT_TOTAL;
            case READ -> DEFAULT_READ;
            case WRITE -> DEFAULT_WRITE;
            case CIRCUIT_BREAKER -> DEFAULT_CIRCUIT_BREAKER;
        };
    }

    public long get(TimeoutType type) {
        Long value = getTimeout(type);
        return value != null ? value : getDefault(type);
    }
}
