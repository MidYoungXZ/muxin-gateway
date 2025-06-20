package com.muxin.gateway.refactory.http;

import com.muxin.gateway.refactory.Protocol;
import com.muxin.gateway.refactory.ProtocolType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP协议实现
 *
 * @author muxin
 */
public class HttpProtocol implements Protocol {
    
    private final String version;
    private final Map<String, Object> config;
    
    public HttpProtocol() {
        this("1.1");
    }
    
    public HttpProtocol(String version) {
        this.version = version;
        this.config = new HashMap<>();
        this.config.put("keepAlive", true);
        this.config.put("maxConnections", 1000);
        this.config.put("connectionTimeout", 30000);
        this.config.put("readTimeout", 60000);
    }
    
    @Override
    public String getName() {
        return "HTTP";
    }
    
    @Override
    public String getVersion() {
        return version;
    }
    
    @Override
    public ProtocolType getType() {
        return ProtocolType.HTTP;
    }
    
    @Override
    public boolean isConnectionOriented() {
        return true;
    }
    
    @Override
    public boolean isRequestResponseBased() {
        return true;
    }
    
    @Override
    public boolean isStreamingSupported() {
        return true;
    }
    
    @Override
    public int getDefaultPort() {
        return 80;
    }
    
    @Override
    public Map<String, Object> getProtocolConfig() {
        return new HashMap<>(config);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HttpProtocol that = (HttpProtocol) obj;
        return Objects.equals(getName(), that.getName()) && 
               Objects.equals(getVersion(), that.getVersion());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getName(), getVersion());
    }
    
    @Override
    public String toString() {
        return "HttpProtocol{" +
                "version='" + version + '\'' +
                '}';
    }
} 