package com.muxin.gateway.admin.config;

import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Flex配置类.
 * <p>
 * 该配置类负责定制MyBatis-Flex框架的全局配置，包括：
 * <ul>
 *     <li>设置逻辑删除字段名为"deleted"</li>
 *     <li>关闭启动时的Banner打印</li>
 * </ul>
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class MyBatisFlexConfig {

    /**
     * MyBatis-Flex自定义配置.
     * <p>
     * 创建并返回一个MyBatisFlexCustomizer实例，用于配置MyBatis-Flex的全局行为。
     * </p>
     *
     * @return MyBatisFlexCustomizer实例，用于自定义MyBatis-Flex配置
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