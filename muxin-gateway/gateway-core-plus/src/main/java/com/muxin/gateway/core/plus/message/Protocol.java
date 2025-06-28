package com.muxin.gateway.core.plus.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 协议接口 - 定义协议的基本特征
 *
 * @author muxin
 */
public interface Protocol {
    
    /**
     * 通用协议常量 - 用于协议无关的消息表示
     */
    Protocol UNIVERSAL = new UniversalProtocol();
    
    /**
     * 协议名称 (HTTP, TCP, UDP, WebSocket, gRPC, MQTT等)
     */
    String getName();
    
    /**
     * 协议版本
     */
    String getVersion();
    
    /**
     * 协议类型
     */
    ProtocolType getType();
    
    /**
     * 是否面向连接
     */
    boolean isConnectionOriented();
    
    /**
     * 是否支持请求-响应模式
     */
    boolean isRequestResponseBased();
    
    /**
     * 是否支持流式传输
     */
    boolean isStreamingSupported();
    
    /**
     * 默认端口
     */
    int getDefaultPort();
    
    /**
     * 协议特定配置
     */
    Map<String, Object> getProtocolConfig();

    /**
     * HTTP协议实现
     *
     * @author muxin
     */
    class HttpProtocol implements Protocol {

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
    
    /**
     * 通用协议实现 - 用于协议无关的消息表示
     * 
     * @author muxin
     */
    class UniversalProtocol implements Protocol {
        
        @Override
        public String getName() {
            return "UNIVERSAL";
        }
        
        @Override
        public String getVersion() {
            return "1.0";
        }
        
        @Override
        public ProtocolType getType() {
            return ProtocolType.CUSTOM;
        }
        
        @Override
        public boolean isConnectionOriented() {
            return false;
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
            return -1; // 不适用
        }
        
        @Override
        public Map<String, Object> getProtocolConfig() {
            return new HashMap<>();
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof UniversalProtocol;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(getName(), getVersion());
        }
        
        @Override
        public String toString() {
            return "UniversalProtocol{version='1.0'}";
        }
    }
}