package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.Protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认请求上下文实现 - 协议无关
 *
 * @author muxin
 */
public class DefaultRequestContext implements RequestContext {

    private final long startTime;
    private final Map<String, Object> attributes;

    private Message inboundMessage;
    private Message outboundMessage;
    private final ServerConnection inboundConnection;
    private ClientConnection outboundConnection;
    private Object matchedRoute;
    private Object selectedNode;
    private boolean completed;
    private Throwable error;

    public DefaultRequestContext(Message inboundMessage, ServerConnection inboundConnection) {
        this.startTime = System.currentTimeMillis();
        this.attributes = new ConcurrentHashMap<>();
        this.inboundMessage = inboundMessage;
        this.inboundConnection = inboundConnection;
        this.completed = false;
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
    public ServerConnection getInboundConnection() {
        return inboundConnection;
    }

    @Override
    public ClientConnection getOutboundConnection() {
        return outboundConnection;
    }

    @Override
    public void setOutboundConnection(ClientConnection connection) {
        this.outboundConnection = connection;
    }

    @Override
    public Object getMatchedRoute() {
        return matchedRoute;
    }

    @Override
    public void setMatchedRoute(Object route) {
        this.matchedRoute = route;
    }

    @Override
    public Object getSelectedNode() {
        return selectedNode;
    }

    @Override
    public void setSelectedNode(Object node) {
        this.selectedNode = node;
    }

    @Override
    public Protocol getInboundProtocol() {
        return inboundMessage != null ? inboundMessage.getProtocol() : null;
    }

    @Override
    public Protocol getOutboundProtocol() {
        return outboundMessage != null ? outboundMessage.getProtocol() : null;
    }

    @Override
    public boolean needsProtocolConversion() {
        Protocol inboundProto = getInboundProtocol();
        Protocol outboundProto = getOutboundProtocol();

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
                ", duration=" + getDuration() +
                ", traceId='" + getTraceId() + '\'' +
                ", inboundProtocol=" + getInboundProtocol() +
                ", outboundProtocol=" + getOutboundProtocol() +
                ", completed=" + completed +
                ", hasError=" + hasError() +
                '}';
    }
} 