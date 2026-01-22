package com.muxin.gateway.admin.config;

import com.muxin.gateway.admin.websocket.MonitorWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类.
 * <p>
 * 该配置类负责WebSocket的相关配置，用于支持实时监控功能。
 * 主要功能包括：
 * <ul>
 *     <li>启用WebSocket支持</li>
 *     <li>注册WebSocket处理器和端点</li>
 *     <li>配置跨域访问策略</li>
 * </ul>
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MonitorWebSocketHandler monitorWebSocketHandler;

    /**
     * 构造函数，注入WebSocket处理器.
     *
     * @param monitorWebSocketHandler 监控WebSocket处理器
     */
    public WebSocketConfig(MonitorWebSocketHandler monitorWebSocketHandler) {
        this.monitorWebSocketHandler = monitorWebSocketHandler;
    }

    /**
     * 注册WebSocket处理器.
     * <p>
     * 将监控WebSocket处理器注册到/ws/monitor端点，并允许所有来源的跨域访问。
     * </p>
     *
     * @param registry WebSocket处理器注册器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(monitorWebSocketHandler, "/ws/monitor")
                .setAllowedOrigins("*");
    }
} 