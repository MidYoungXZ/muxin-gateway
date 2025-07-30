package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 协议转换器配置
 * 
 * @author muxin
 */
@Data
@Builder
public class ProtocolConverterConfig {
    
    @Builder.Default
    private boolean enableAutoRegistration = true;
    
    @Builder.Default
    private int maxConvertersPerProtocol = 10;
    
    @Builder.Default
    private Duration conversionTimeout = Duration.ofSeconds(5);
    
    @Builder.Default
    private boolean enableConversionCache = false;
    
    @Builder.Default
    private Duration conversionCacheExpiration = Duration.ofMinutes(5);
    
    @Builder.Default
    private int maxCacheSize = 1000;
    
    @Builder.Default
    private boolean enableConversionMetrics = true;
    
    @Builder.Default
    private boolean enableConversionTracing = true;
    
    @Builder.Default
    private boolean enableFallbackConverter = true;
    
    @Builder.Default
    private String fallbackConverterType = "UNIVERSAL";
    
    @Builder.Default
    private boolean strictValidation = false;
    
    @Builder.Default
    private Duration converterWarmupTimeout = Duration.ofSeconds(10);
    
    public static ProtocolConverterConfig defaultConfig() {
        return ProtocolConverterConfig.builder().build();
    }
    
    public void validate() {
        if (maxConvertersPerProtocol <= 0) {
            throw new IllegalArgumentException("maxConvertersPerProtocol必须大于0");
        }
        if (conversionTimeout == null || conversionTimeout.isNegative()) {
            throw new IllegalArgumentException("conversionTimeout必须大于0");
        }
        if (maxCacheSize < 0) {
            throw new IllegalArgumentException("maxCacheSize不能小于0");
        }
        if (fallbackConverterType == null || fallbackConverterType.trim().isEmpty()) {
            throw new IllegalArgumentException("fallbackConverterType不能为空");
        }
    }
}