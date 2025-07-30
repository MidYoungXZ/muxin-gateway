package com.muxin.gateway.admin.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监控WebSocket处理器
 *
 * @author muxin
 */
@Component
@RequiredArgsConstructor
public class MonitorWebSocketHandler implements WebSocketHandler {
    
    private static final Logger log = LoggerFactory.getLogger(MonitorWebSocketHandler.class);
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("WebSocket连接建立: {}", session.getId());
        
        // 发送初始数据
        sendInitialData(session);
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 处理客户端消息
        log.debug("收到WebSocket消息: {}", message.getPayload());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", session.getId(), exception);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session.getId());
        log.info("WebSocket连接关闭: {}, 状态: {}", session.getId(), closeStatus);
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 广播监控数据
     */
    public void broadcastMetrics(Object data) {
        String message;
        try {
            message = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("序列化监控数据失败", e);
            return;
        }
        
        sessions.values().parallelStream().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                log.error("发送WebSocket消息失败", e);
            }
        });
    }
    
    /**
     * 发送初始数据
     */
    private void sendInitialData(WebSocketSession session) {
        // TODO: 发送初始监控数据
    }
} 