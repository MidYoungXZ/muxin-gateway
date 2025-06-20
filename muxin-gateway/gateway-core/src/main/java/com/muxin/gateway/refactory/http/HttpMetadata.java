package com.muxin.gateway.refactory.http;

import com.muxin.gateway.refactory.EndpointAddress;
import com.muxin.gateway.refactory.MessageMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP元数据实现
 *
 * @author muxin
 */
public class HttpMetadata implements MessageMetadata {
    
    private final long timestamp;
    private long receiveTime;
    private long sendTime;
    private EndpointAddress sourceAddress;
    private EndpointAddress targetAddress;
    private String connectionId;
    private String routeId;
    private String serviceName;
    private String traceId;
    private String spanId;
    private final Map<String, Object> attributes;
    
    public HttpMetadata() {
        this.timestamp = System.currentTimeMillis();
        this.receiveTime = timestamp;
        this.sendTime = 0;
        this.attributes = new HashMap<>();
        
        // 设置HTTP特定的元数据
        attributes.put("protocol", "HTTP");
        attributes.put("version", "1.1");
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public long getReceiveTime() {
        return receiveTime;
    }
    
    @Override
    public long getSendTime() {
        return sendTime;
    }
    
    @Override
    public EndpointAddress getSourceAddress() {
        return sourceAddress;
    }
    
    @Override
    public EndpointAddress getTargetAddress() {
        return targetAddress;
    }
    
    @Override
    public String getConnectionId() {
        return connectionId;
    }
    
    @Override
    public String getRouteId() {
        return routeId;
    }
    
    @Override
    public String getServiceName() {
        return serviceName;
    }
    
    @Override
    public String getTraceId() {
        return traceId;
    }
    
    @Override
    public String getSpanId() {
        return spanId;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
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
    
    // HTTP特定方法
    public void setMethod(String method) {
        setAttribute("method", method);
    }
    
    public String getMethod() {
        String method = getAttribute("method", String.class);
        return method != null ? method : "GET";
    }
    
    public void setPath(String path) {
        setAttribute("path", path);
    }
    
    public String getPath() {
        String path = getAttribute("path", String.class);
        return path != null ? path : "/";
    }
    
    public void setStatusCode(int statusCode) {
        setAttribute("statusCode", statusCode);
    }
    
    public int getStatusCode() {
        Integer statusCode = getAttribute("statusCode", Integer.class);
        return statusCode != null ? statusCode : 200;
    }
    
    // Setter方法
    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }
    
    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }
    
    public void setSourceAddress(EndpointAddress sourceAddress) {
        this.sourceAddress = sourceAddress;
    }
    
    public void setTargetAddress(EndpointAddress targetAddress) {
        this.targetAddress = targetAddress;
    }
    
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
    
    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }
} 