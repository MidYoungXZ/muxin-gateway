package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.protocol.message.Message;
import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.Route;
import com.muxin.gateway.core.plus.route.node.ServiceNode;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认请求上下文实现 - 协议无关
 *
 * @author muxin
 */
public class DefaultRequestContext implements RequestContext {

    private final long startTime;
    private final String requestId;
    private final Map<String, Object> attributes;

    private Message inboundMessage;
    private Message outboundMessage;

    private Object originalOutboundData;
    private Protocol originalOutboundProtocol;

    private Object originalInboundData;
    private Protocol originalInboundProtocol;

    private ServerConnection serverConnection;
    private ClientConnection clientConnection;
    private Route matchedRoute;
    private ServiceNode selectedNode;
    private boolean completed;
    private Throwable error;


    public DefaultRequestContext(Object data, Protocol protocol) {
        this.originalInboundProtocol = protocol;
        this.originalOutboundData = data;
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
    public Message getInboundMessage() {
        return inboundMessage;
    }

    @Override
    public void setInboundMessage(Message message) {
        this.inboundMessage = message;
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
    public Object getOriginalOutboundData() {
        return originalOutboundData;
    }

    @Override
    public void setOriginalOutboundData(Object inboundData) {
        this.originalOutboundData = inboundData;
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
    public ServiceNode getSelectedNode() {
        return selectedNode;
    }

    @Override
    public void setSelectedNode(ServiceNode node) {
        this.selectedNode = node;
    }

    @Override
    public Protocol getOrigialInboundProtocol() {
        return originalInboundProtocol;
    }

    @Override
    public Object getOriginalInboundData() {
        return this.originalInboundData;
    }

    @Override
    public void setOriginalInboundData(Object inboundData) {
        this.originalInboundData = inboundData;
    }

    @Override
    public Protocol getOrigalOutboundProtocol() {
        return originalOutboundProtocol;
    }

    @Override
    public boolean needsProtocolConversion() {
        Protocol inboundProto = getOrigialInboundProtocol();
        Protocol outboundProto = getOrigalOutboundProtocol();

        if (inboundProto == null || outboundProto == null) {
            return false;
        }

        return !inboundProto.equals(outboundProto);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        throw new ClassCastException("无法将属性 " + key + " 转换为类型 " + type.getName());
    }

    @Override
    public void setAttribute(String key, Object value) {
        if (key != null) {
            if (value != null) {
                attributes.put(key, value);
            } else {
                attributes.remove(key);
            }
        }
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

    @Override
    public String toString() {
        return "DefaultRequestContext{" +
                "startTime=" + startTime +
                ", requestId='" + requestId + '\'' +
                ", attributes=" + attributes +
                ", inboundMessage=" + inboundMessage +
                ", outboundMessage=" + outboundMessage +
                ", originalOutboundData=" + originalOutboundData +
                ", originalOutboundProtocol=" + originalOutboundProtocol +
                ", originalInboundData=" + originalInboundData +
                ", originalInboundProtocol=" + originalInboundProtocol +
                ", serverConnection=" + serverConnection +
                ", clientConnection=" + clientConnection +
                ", matchedRoute=" + matchedRoute +
                ", selectedNode=" + selectedNode +
                ", completed=" + completed +
                ", error=" + error +
                '}';
    }
}