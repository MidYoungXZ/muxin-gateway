package com.muxin.gateway.core.config;

import com.muxin.gateway.core.route.filter.FilterDefinition;
import com.muxin.gateway.core.route.RouteDefinition;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关配置属性类
 *
 * @author Administrator
 * @date 2024/11/20 16:51
 */
@Data
@ConfigurationProperties(prefix = "muxin.gateway")
public class GatewayProperties {

    /**
     * 路由配置列表
     */
    private List<RouteDefinition> routes = new ArrayList<>();

    /**
     * 默认过滤器配置列表
     */
    private List<FilterDefinition> defaultFilters = new ArrayList<>();

    /**
     * Netty服务器和客户端配置
     */
    private NettyConfig netty = new NettyConfig();



    /**
     * 注册中心配置
     */
    private RegisterProperties register = new RegisterProperties();

    /**
     * Netty配置类
     */
    @Data
    public static class NettyConfig {
        /**
         * 服务器配置
         */
        private NettyHttpServerProperties server = new NettyHttpServerProperties();

        /**
         * 客户端配置
         */
        private NettyHttpClientProperties client = new NettyHttpClientProperties();
    }

    /**
     * 注册中心配置类
     */
    @Data
    public static class RegisterProperties {
        /**
         * 注册中心类型，如：nacos
         */
        private String type = "nacos";

        /**
         * 注册中心地址
         */
        private String address = "localhost:8848";

        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;

        /**
         * 分组名称
         */
        private String group = "DEFAULT_GROUP";

        /**
         * 命名空间
         */
        private String namespace = "DEFAULT";

        /**
         * 网关自注册配置
         */
        private GatewayRegisterProperties gateway = new GatewayRegisterProperties();
    }

    /**
     * 网关自注册配置类
     */
    @Data
    public static class GatewayRegisterProperties {
        /**
         * 是否启用网关自注册
         */
        private boolean enabled = true;

        /**
         * 网关实例权重
         */
        private double weight = 1.0;

        /**
         * 网关服务版本
         */
        private String version = "1.0";

        /**
         * 网关服务描述
         */
        private String description = "Muxin API Gateway - 统一网关服务";

        /**
         * 服务ID，默认使用 spring.application.name
         */
        private String serviceId;

        /**
         * 健康检查URL
         */
        private String healthCheckUrl = "/health";

        /**
         * 健康检查间隔（秒）
         */
        private int healthCheckInterval = 30;

        /**
         * 实例状态：UP, DOWN, STARTING, OUT_OF_SERVICE
         */
        private String status = "UP";

        /**
         * 是否安全连接
         */
        private boolean secure = false;

        /**
         * 额外的元数据信息
         */
        private java.util.Map<String, String> metadata = new java.util.HashMap<>();
    }
}
