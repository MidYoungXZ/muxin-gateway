package com.muxin.gateway.core.plus.route;


import com.muxin.gateway.core.plus.common.AttributesHolder;
import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.ServerExchange;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;

/**
 * 通用请求上下文 - 协议无关
 *
 * @author muxin
 */
public interface RequestContext extends AttributesHolder {

    String requestId();


    ServerExchange<? extends Message, ? extends Message> exchange();
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
    EndpointAddress getSelectedEndpoint();

    /**
     * 设置选中的节点
     */
    void setSelectedEndpoint(EndpointAddress instance);

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