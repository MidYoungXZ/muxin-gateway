package com.muxin.gateway.admin.config;

import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Flex 配置
 *
 * @author muxin
 */
@Configuration
public class MyBatisFlexConfig {
    
    /**
     * MyBatis-Flex 自定义配置
     */
    @Bean
    public MyBatisFlexCustomizer myBatisFlexCustomizer() {
        return globalConfig -> {
            // 设置逻辑删除字段
            globalConfig.setLogicDeleteColumn("deleted");
            // 打印banner
            globalConfig.setPrintBanner(false);
        };
    }
} 