package com.muxin.gateway.core.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控指标配置
 */
@Configuration
public class MetricsConfig {
    
    /**
     * 提供一个简单的 MeterRegistry 实现
     * 如果需要导出到 Prometheus 等监控系统，可以替换为对应的实现
     */
    @Bean
    @ConditionalOnMissingBean(MeterRegistry.class)
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
} 