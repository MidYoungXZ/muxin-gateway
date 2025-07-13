package com.muxin.gateway.core.plus.protocol.message.http;

import com.muxin.gateway.core.plus.protocol.message.MessageMetadata;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP消息元数据实现
 *
 * @author muxin
 */
public class HttpMetadata implements MessageMetadata {
    
    private final long timestamp;
    private final long receiveTime;
    private final long sendTime;
    private final EndpointAddress sourceAddress;
    private final EndpointAddress targetAddress;
    private final String connectionId;
    private final String routeId;
    private final String serviceName;
    private final String traceId;
    private final String spanId;
    private final Map<String, Object> attributes;
    
    public HttpMetadata(Builder builder) {
        this.timestamp = builder.timestamp;
        this.receiveTime = builder.receiveTime;
        this.sendTime = builder.sendTime;
        this.sourceAddress = builder.sourceAddress;
        this.targetAddress = builder.targetAddress;
        this.connectionId = builder.connectionId;
        this.routeId = builder.routeId;
        this.serviceName = builder.serviceName;
        this.traceId = builder.traceId;
        this.spanId = builder.spanId;
        this.attributes = new ConcurrentHashMap<>(builder.attributes);
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
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }
    
    @Override
    public void setAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return new ConcurrentHashMap<>(attributes);
    }
    
    // Builder模式
    public static class Builder {
        private long timestamp = System.currentTimeMillis();
        private long receiveTime = timestamp;
        private long sendTime;
        private EndpointAddress sourceAddress;
        private EndpointAddress targetAddress;
        private String connectionId;
        private String routeId;
        private String serviceName;
        private String traceId = UUID.randomUUID().toString();
        private String spanId = UUID.randomUUID().toString();
        private Map<String, Object> attributes = new ConcurrentHashMap<>();
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder receiveTime(long receiveTime) {
            this.receiveTime = receiveTime;
            return this;
        }
        
        public Builder sendTime(long sendTime) {
            this.sendTime = sendTime;
            return this;
        }
        
        public Builder sourceAddress(EndpointAddress sourceAddress) {
            this.sourceAddress = sourceAddress;
            return this;
        }
        
        public Builder targetAddress(EndpointAddress targetAddress) {
            this.targetAddress = targetAddress;
            return this;
        }
        
        public Builder connectionId(String connectionId) {
            this.connectionId = connectionId;
            return this;
        }
        
        public Builder routeId(String routeId) {
            this.routeId = routeId;
            return this;
        }
        
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }
        
        public Builder spanId(String spanId) {
            this.spanId = spanId;
            return this;
        }
        
        public Builder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }
        
        public Builder attributes(Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }
        
        public HttpMetadata build() {
            return new HttpMetadata(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
} 