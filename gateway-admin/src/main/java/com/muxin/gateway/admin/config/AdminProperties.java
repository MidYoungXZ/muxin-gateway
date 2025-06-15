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
     * 管理员用户名
     */
    private String username = "admin";

    /**
     * 管理员密码
     */
    private String password = "admin123";

    /**
     * 会话超时时间（分钟）
     */
    private int sessionTimeout = 30;

    /**
     * 管理界面路径前缀
     */
    private String pathPrefix = "/admin";
} 