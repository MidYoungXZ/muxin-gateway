package com.muxin.gateway.core.plus.route;


import com.muxin.gateway.core.plus.common.Attributes;
import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.protocol.message.Message;
import com.muxin.gateway.core.plus.protocol.message.ProtocolData;
import com.muxin.gateway.core.plus.route.service.ServiceInstance;

/**
 * 通用请求上下文 - 协议无关
 *
 * @author muxin
 */
public interface RequestContext extends Attributes {

    /**
     * 请求ID
     */
    String requestId();

    /**
     * 入站协议数据
     */
    ProtocolData getInboundData();

    /**
     * 设置入站数据
     */
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

    /**
     * 设置出站数据
     */
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

    /**
     * 设置原始后端请求
     */
    void setBackendServiceRequest(ProtocolData data);


    /**
     * 原始后端返回
     */
    ProtocolData getBackendServiceResponse();

    /**
     * 设置后端返回
     */
    void setBackendServiceResponse(ProtocolData data);


    /**
     * server接收连接
     */
    ServerConnection serverConnection();

    /**
     * 设置server接收连接
     */
    void setServerConnection(ServerConnection connection);

    /**
     * 请求后端服务连接
     */
    ClientConnection clientConnection();

    /**
     * 设置后端服务连接
     */
    void setClientConnection(ClientConnection connection);

    /**
     * 匹配的路由
     */
    Route getMatchedRoute();

    /**
     * 设置匹配的路由
     */
    void setMatchedRoute(Route route);

    /**
     * 选中的节点
     */
    ServiceInstance getSelectedNode();

    /**
     * 设置选中的节点
     */
    void setSelectedNode(ServiceInstance node);

    /**
     * 是否需要协议转换
     */
    boolean needsProtocolConversion();

    /**
     * 生命周期
     */
    long getStartTime();

    /**
     * 标记完成
     */
    void markComplete();

    /**
     * 判断是否完成
     */
    boolean isCompleted();

    /**
     * 错误处理
     */
    Throwable getError();

    /**
     * 设置异常
     */
    void setError(Throwable error);

    /**
     * 是否有异常
     */
    boolean hasError();
} 