package com.muxin.gateway.core.config;

import lombok.Data;

/**
 * 管理界面配置属性
 */
@Data
public class AdminProperties {

    /**
     * 是否启用管理界面
     */
    private boolean enabled = true;

    /**
     * 管理界面访问路径前缀
     */
    private String pathPrefix = "/admin";

    /**
     * 管理员用户名
     */
    private String username = "admin";

    /**
     * 管理员密码
     */
    private String password = "admin123";

    /**
     * Session超时时间（分钟）
     */
    private int sessionTimeout = 30;

    /**
     * 是否启用基础认证
     */
    private boolean basicAuth = false;
} 