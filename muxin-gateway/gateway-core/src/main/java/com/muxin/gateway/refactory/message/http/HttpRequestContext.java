package com.muxin.gateway.refactory.message.http;

import com.muxin.gateway.refactory.*;
import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.route.UniversalRequestContext;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求上下文实现
 *
 * @author muxin
 */
public class HttpRequestContext implements UniversalRequestContext {
    
    private Message inboundMessage;
    private Message outboundMessage;
    private final Connection inboundConnection;
    private Connection outboundConnection;
    private Object matchedRoute;
    private Object selectedNode;
    private final long startTime;
    private final Map<String, Object> attributes;
    private Throwable error;
    private boolean completed;
    
    public HttpRequestContext(Connection inboundConnection, Message inboundMessage) {
        this.inboundConnection = inboundConnection;
        this.inboundMessage = inboundMessage;
        this.startTime = System.currentTimeMillis();
        this.attributes = new HashMap<>();
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
    public Connection getInboundConnection() {
        return inboundConnection;
    }
    
    @Override
    public Connection getOutboundConnection() {
        return outboundConnection;
    }
    
    @Override
    public void setOutboundConnection(Connection connection) {
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
        Protocol inbound = getInboundProtocol();
        Protocol outbound = getOutboundProtocol();
        return inbound != null && outbound != null && !inbound.equals(outbound);
    }
    
    @Override
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
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
} 