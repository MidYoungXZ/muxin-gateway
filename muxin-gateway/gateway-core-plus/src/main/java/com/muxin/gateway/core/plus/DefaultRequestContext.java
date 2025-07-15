package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.protocol.message.Message;
import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.protocol.message.ProtocolData;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.Route;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.Data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认请求上下文实现 - 协议无关
 *
 * @author muxin
 */
@Data
public class DefaultRequestContext implements RequestContext {

    private final long startTime;
    private final String requestId;
    private final Map<String, Object> attributes;

    private ProtocolData inboundData;
    private ProtocolData outboundData;

    private Message inboundMessage;
    private Message outboundMessage;

    private ProtocolData backendServiceRequest;
    private ProtocolData backendServiceResponse;

    private ServerConnection serverConnection;
    private ClientConnection clientConnection;
    private Route matchedRoute;
    private EndpointAddress endpointAddress;
    private boolean completed;
    private Throwable error;


    public DefaultRequestContext(ProtocolData inboundData) {
        this.inboundData = inboundData;
        this.startTime = System.currentTimeMillis();
        this.attributes = new ConcurrentHashMap<>();
        this.completed = false;
        this.requestId = UUID.randomUUID().toString();
    }

    public DefaultRequestContext(Message inboundMessage, ServerConnection serverConnection) {
        this.startTime = System.currentTimeMillis();
        this.attributes = new ConcurrentHashMap<>();
        this.inboundMessage = inboundMessage;
        this.serverConnection = serverConnection;
        this.completed = false;
        this.requestId = UUID.randomUUID().toString();
    }

    @Override
    public String requestId() {
        return requestId;
    }

    @Override
    public ProtocolData getInboundData() {
        return inboundData;
    }

    @Override
    public void setInboundData(ProtocolData inboundData) {
        this.inboundData = inboundData;
    }

    @Override
    public Message getInboundMessage() {
        return inboundMessage;
    }

    @Override
    public void setInboundMessage(Message message) {
        this.inboundMessage = message;
    }

    @Override
    public ProtocolData getOutboundData() {
        return outboundData;
    }

    @Override
    public void setOutboundData(ProtocolData data) {
        this.outboundData = data;
    }

    @Override
    public Message getOutboundMessage() {
        return outboundMessage;
    }

    @Override
    public void setOutboundMessage(Message message) {
        this.outboundMessage = message;
    }

    @Override
    public ProtocolData getBackendServiceRequest() {
        return backendServiceRequest;
    }

    @Override
    public void setBackendServiceRequest(ProtocolData data) {
        this.backendServiceRequest = data;
    }

    @Override
    public ProtocolData getBackendServiceResponse() {
        return backendServiceResponse;
    }

    @Override
    public void setBackendServiceResponse(ProtocolData data) {
        this.backendServiceResponse = data;
    }

    @Override
    public ServerConnection serverConnection() {
        return serverConnection;
    }

    @Override
    public void setServerConnection(ServerConnection connection) {
        this.serverConnection = connection;
    }

    @Override
    public ClientConnection clientConnection() {
        return clientConnection;
    }

    @Override
    public void setClientConnection(ClientConnection connection) {
        this.clientConnection = connection;
    }

    @Override
    public Route getMatchedRoute() {
        return matchedRoute;
    }

    @Override
    public void setMatchedRoute(Route route) {
        this.matchedRoute = route;
    }

    @Override
    public EndpointAddress getSelectedEndpoint() {
        return endpointAddress;
    }

    @Override
    public void setSelectedEndpoint(EndpointAddress endpointAddress) {
        this.endpointAddress = endpointAddress;
    }


    @Override
    public boolean needsProtocolConversion() {
        Protocol inboundProto = getInboundData().getProtocol();
        Protocol outboundProto = getBackendServiceResponse().getProtocol();

        if (inboundProto == null || outboundProto == null) {
            return false;
        }

        return !inboundProto.equals(outboundProto);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new ConcurrentHashMap<>(attributes);
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void markComplete() {
        this.completed = true;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public Throwable getError() {
        return error;
    }

    @Override
    public void setError(Throwable error) {
        this.error = error;
    }

    @Override
    public boolean hasError() {
        return error != null;
    }

    /**
     * 获取请求处理持续时间（毫秒）
     */
    public long getDuration() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 获取跟踪ID，用于分布式链路追踪
     */
    public String getTraceId() {
        if (inboundMessage != null && inboundMessage.getMetadata() != null) {
            return inboundMessage.getMetadata().getTraceId();
        }
        return null;
    }

}