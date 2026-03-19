package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

/**
 * 协议转换器配置
 * 
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class ProtocolConverterConfig {
    
    @Builder.Default
    private boolean enableAutoRegistration = true;
    
    @Builder.Default
    private int maxConvertersPerProtocol = 10;
    
    @Builder.Default
    private Long conversionTimeout = 5000L;
    
    @Builder.Default
    private boolean enableConversionCache = false;
    
    @Builder.Default
    private Long conversionCacheExpiration = 300000L;
    
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
    private Long converterWarmupTimeout = 10000L;
    
    public static ProtocolConverterConfig defaultConfig() {
        return ProtocolConverterConfig.builder().build();
    }
    
    public void validate() {
        if (maxConvertersPerProtocol <= 0) {
            throw new IllegalArgumentException("maxConvertersPerProtocol 必须大于 0");
        }
        if (conversionTimeout == null || conversionTimeout < 0) {
            throw new IllegalArgumentException("conversionTimeout 必须大于 0");
        }
        if (maxCacheSize < 0) {
            throw new IllegalArgumentException("maxCacheSize 不能小于 0");
        }
        if (fallbackConverterType == null || fallbackConverterType.trim().isEmpty()) {
            throw new IllegalArgumentException("fallbackConverterType 不能为空");
        }
    }
}
