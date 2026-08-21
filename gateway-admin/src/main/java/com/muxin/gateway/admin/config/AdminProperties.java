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
 * </ul>
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
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

} 