package com.muxin.gateway.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 管理界面配置属性类.
 * <p>
 * 该类用于从配置文件中读取管理后台相关的配置参数，配置前缀为muxin.gateway.admin。
 * 支持的配置项包括：
 * <ul>
 *     <li>enabled: 是否启用管理界面，默认为true</li>
 *     <li>routeFlashInterval: 路由刷新间隔（秒），默认为30秒</li>
 * </ul>
 * </p>
 *
 * @author muxin
 * @since 1.0.0
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "muxin.gateway.admin")
public class AdminProperties {

    /**
     * 是否启用管理界面.
     * <p>
     * 默认值为true，设置为false可禁用整个管理后台功能。
     * </p>
     */
    private boolean enabled = true;

    /**
     * 路由刷新间隔（秒）.
     * <p>
     * 用于定时刷新网关路由配置的间隔时间，默认值为30秒。
     * 较短的间隔可以更快地同步路由变更，但会增加数据库查询频率。
     * </p>
     */
    private Long routeFlashInterval = 30L;

} 