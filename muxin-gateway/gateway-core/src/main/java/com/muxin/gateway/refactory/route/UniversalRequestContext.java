package com.muxin.gateway.refactory.route;


import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.message.Message;

import java.util.Map;

/**
 * 通用请求上下文 - 协议无关
 *
 * @author muxin
 */
public interface UniversalRequestContext {
    
    /**
     * 入站消息
     */
    Message getInboundMessage();
    void setInboundMessage(Message message);
    
    /**
     * 出站消息
     */
    Message getOutboundMessage();
    void setOutboundMessage(Message message);
    
    /**
     * 入站连接
     */
        Connection getInboundConnection();

    /**
     * 出站连接
     */
    Connection getOutboundConnection();
    void setOutboundConnection(Connection connection);
    
    /**
     * 匹配的路由
     */
    Object getMatchedRoute();
    void setMatchedRoute(Object route);
    
    /**
     * 选中的节点
     */
    Object getSelectedNode();
    void setSelectedNode(Object node);
    
    /**
     * 入站协议
     */
    Protocol getInboundProtocol();
    
    /**
     * 出站协议
     */
    Protocol getOutboundProtocol();
    
    /**
     * 是否需要协议转换
     */
    boolean needsProtocolConversion();
    
    /**
     * 属性管理
     */
    <T> T getAttribute(String key, Class<T> type);
    void setAttribute(String key, Object value);
    Map<String, Object> getAttributes();
    
    /**
     * 生命周期
     */
    long getStartTime();
    void markComplete();
    boolean isCompleted();
    
    /**
     * 错误处理
     */
    Throwable getError();
    void setError(Throwable error);
    boolean hasError();
} 