package com.muxin.gateway.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 管理界面配置属性
 * 
 * @author muxin
 */
@Data
@ConfigurationProperties(prefix = "muxin.gateway.admin")
public class AdminProperties {

    /**
     * 是否启用管理界面
     */
    private boolean enabled = true;

    /**
     * 路由刷新间隔（秒）
     */
    private Long routeFlashInterval = 30L;

} 