package com.muxin.gateway.core.plus.route;


import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.protocol.message.Message;
import com.muxin.gateway.core.plus.protocol.message.ProtocolData;
import com.muxin.gateway.core.plus.route.node.ServiceNode;

import java.util.Map;

/**
 * 通用请求上下文 - 协议无关
 *
 * @author muxin
 */
public interface RequestContext {

    /**
     * 请求ID
     */
    String requestId();

    /**
     * 入站协议数据
     */
    ProtocolData getInboundData();

    void setInboundData(ProtocolData data);

    /**
     * 入站消息
     */
    Message getInboundMessage();

    /**
     * 设置入站消息
     */
    void setInboundMessage(Message message);

    /**
     * 出站协议数据
     */
    ProtocolData getOutboundData();

    void setOutboundData(ProtocolData data);

    /**
     * 出站消息
     */
    Message getOutboundMessage();

    /**
     * 设置出站消息
     */
    void setOutboundMessage(Message message);

    /**
     * 原始后端请求
     */
    ProtocolData getBackendServiceRequest();

    void setBackendServiceRequest(ProtocolData data);


    /**
     * 原始后端返回
     */
    ProtocolData getBackendServiceResponse();

    void setBackendServiceResponse(ProtocolData data);


    /**
     * server接收连接
     */
    ServerConnection serverConnection();

    void setServerConnection(ServerConnection connection);


    /**
     * 请求后端服务连接
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