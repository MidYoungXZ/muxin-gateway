package com.muxin.gateway.refactory.http;

import com.muxin.gateway.refactory.EndpointAddress;
import com.muxin.gateway.refactory.Protocol;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP端点地址实现
 *
 * @author muxin
 */
public class HttpEndpointAddress implements EndpointAddress {
    
    private final Protocol protocol;
    private final String host;
    private final int port;
    private final String path;
    private final Map<String, String> parameters;
    private final String originalUri;
    
    public HttpEndpointAddress(String uri) {
        this.originalUri = uri;
        this.protocol = new HttpProtocol();
        this.parameters = new HashMap<>();
        
        try {
            URI parsedUri = URI.create(uri);
            this.host = parsedUri.getHost();
            this.port = parsedUri.getPort() != -1 ? parsedUri.getPort() : 80;
            this.path = parsedUri.getPath() != null ? parsedUri.getPath() : "/";
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URI: " + uri, e);
        }
    }
    
    @Override
    public Protocol getProtocol() {
        return protocol;
    }
    
    @Override
    public String getHost() {
        return host;
    }
    
    @Override
    public int getPort() {
        return port;
    }
    
    @Override
    public String getPath() {
        return path;
    }
    
    @Override
    public Map<String, String> getParameters() {
        return new HashMap<>(parameters);
    }
    
    @Override
    public String toUri() {
        return originalUri;
    }
    
    @Override
    public boolean isValid() {
        return host != null && !host.isEmpty() && port > 0;
    }
    
    @Override
    public Map<String, Object> getProtocolSpecificInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("scheme", originalUri.startsWith("https") ? "https" : "http");
        return info;
    }
} 