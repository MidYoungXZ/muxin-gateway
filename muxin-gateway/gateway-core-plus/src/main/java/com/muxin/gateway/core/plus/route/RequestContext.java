package com.muxin.gateway.core.plus.route;


import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.protocol.message.Message;
import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.route.node.ServiceNode;

import java.util.Map;

/**
 * 通用请求上下文 - 协议无关
 *
 * @author muxin
 */
public interface RequestContext {

    String requestId();

    /**
     * 入站消息
     */
    Message getInboundMessage();

    void setInboundMessage(Message message);

    /**
     * 入站协议
     */
    Protocol getOrigialInboundProtocol();


    /**
     * 原始入站数据
     */
    Object getOriginalInboundData();

    void setOriginalInboundData(Object inboundData);

    /**
     * 出站消息
     */
    Message getOutboundMessage();

    void setOutboundMessage(Message message);

    /**
     * 原始出站数据
     */
    Object getOriginalOutboundData();

    void setOriginalOutboundData(Object inboundData);


    /**
     * 出站协议
     */
    Protocol getOrigalOutboundProtocol();


    /**
     * 入站连接
     */
    ServerConnection serverConnection();

    void setServerConnection(ServerConnection connection);


    /**
     * 出站连接
     */
    ClientConnection clientConnection();

    void setClientConnection(ClientConnection connection);

    /**
     * 匹配的路由
     */
    Route getMatchedRoute();

    void setMatchedRoute(Route route);

    /**
     * 选中的节点
     */
    ServiceNode getSelectedNode();

    void setSelectedNode(ServiceNode node);


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