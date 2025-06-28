package com.muxin.gateway.core.plus.message.http;

import com.muxin.gateway.core.plus.message.MessageHeaders;

import java.util.*;

/**
 * HTTP头部实现
 *
 * @author muxin
 */
public class HttpHeaders implements MessageHeaders {
    
    private final Map<String, Object> headers;
    private final Map<String, Object> protocolHeaders;
    
    public HttpHeaders() {
        this.headers = new LinkedHashMap<>();
        this.protocolHeaders = new LinkedHashMap<>();
    }
    
    public HttpHeaders(Map<String, Object> headers) {
        this.headers = new LinkedHashMap<>(headers);
        this.protocolHeaders = new LinkedHashMap<>();
    }
    
    @Override
    public void set(String name, Object value) {
        headers.put(name, value);
    }
    
    @Override
    public <T> T get(String name, Class<T> type) {
        Object value = headers.get(name);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }
    
    @Override
    public <T> Optional<T> getOptional(String name, Class<T> type) {
        return Optional.ofNullable(get(name, type));
    }
    
    @Override
    public boolean contains(String name) {
        return headers.containsKey(name);
    }
    
    @Override
    public void remove(String name) {
        headers.remove(name);
    }
    
    @Override
    public Set<String> getNames() {
        return new HashSet<>(headers.keySet());
    }
    
    @Override
    public Map<String, Object> asMap() {
        return new LinkedHashMap<>(headers);
    }
    
    @Override
    public void setProtocolHeaders(Map<String, Object> headers) {
        this.protocolHeaders.clear();
        this.protocolHeaders.putAll(headers);
    }
    
    @Override
    public Map<String, Object> getProtocolHeaders() {
        return new LinkedHashMap<>(protocolHeaders);
    }
    
    // HTTP特定方法
    public String getContentType() {
        return get("Content-Type", String.class);
    }
    
    public void setContentType(String contentType) {
        set("Content-Type", contentType);
    }
    
    public String getContentLength() {
        return get("Content-Length", String.class);
    }
    
    public void setContentLength(long length) {
        set("Content-Length", String.valueOf(length));
    }
    
    public String getUserAgent() {
        return get("User-Agent", String.class);
    }
    
    public String getAuthorization() {
        return get("Authorization", String.class);
    }
} 