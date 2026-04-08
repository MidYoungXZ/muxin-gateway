package com.muxin.gateway.admin.config;

import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisFlexConfig {

    @Bean
    public MyBatisFlexCustomizer myBatisFlexCustomizer() {
        return globalConfig -> {
            globalConfig.setPrintBanner(false);
        };
    }
} 