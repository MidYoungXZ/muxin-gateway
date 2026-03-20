package com.muxin.gateway.core.route;

import com.muxin.gateway.core.common.AttributesHolder;
import com.muxin.gateway.core.connect.ClientConnection;
import com.muxin.gateway.core.connect.ServerConnection;
import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import com.muxin.gateway.core.service.EndpointAddress;

/**
 * HTTP请求上下文
 * 简化版本：只支持HTTP协议，移除协议无关抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface RequestContext extends AttributesHolder {

    String requestId();

    HttpServerExchange exchange();

    ServerConnection serverConnection();

    void setServerConnection(ServerConnection connection);

    ClientConnection clientConnection();

    void setClientConnection(ClientConnection connection);

    Route getMatchedRoute();

    void setMatchedRoute(Route route);

    EndpointAddress getSelectedEndpoint();

    void setSelectedEndpoint(EndpointAddress instance);

    long getStartTime();

    void markComplete();

    boolean isCompleted();

    Throwable getError();

    void setError(Throwable error);

    boolean hasError();
}
