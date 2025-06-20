package com.muxin.gateway.refactory.http;

import com.muxin.gateway.refactory.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP协议适配器实现
 *
 * @author muxin
 */
public class HttpProtocolAdapter implements ProtocolAdapter {
    
    private final Protocol httpProtocol;
    private final Map<String, Object> config;
    
    public HttpProtocolAdapter() {
        this.httpProtocol = new HttpProtocol();
        this.config = new HashMap<>();
        this.config.put("maxRequestSize", 10 * 1024 * 1024); // 10MB
    }
    
    @Override
    public Protocol getSupportedProtocol() {
        return httpProtocol;
    }
    
    @Override
    public Message adaptInbound(Object protocolSpecificData, Connection connection) {
        // 将HTTP请求数据转换为HttpMessage
        if (protocolSpecificData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> httpData = (Map<String, Object>) protocolSpecificData;
            return createHttpMessageFromData(httpData);
        }
        throw new IllegalArgumentException("Unsupported protocol data type");
    }
    
    @Override
    public Object adaptOutbound(Message message, Connection connection) {
        // 将HttpMessage转换为协议特定的数据
        if (message instanceof HttpMessage) {
            return convertToHttpData((HttpMessage) message);
        }
        throw new IllegalArgumentException("Unsupported message type");
    }
    
    @Override
    public Connection createConnection(EndpointAddress address, Map<String, Object> options) {
        // 简化实现，返回null表示暂未实现
        return null;
    }
    
    @Override
    public boolean validateAddress(EndpointAddress address) {
        // 检查是否为HTTP协议地址
        String uri = address.toUri();
        return uri != null && (uri.startsWith("http://") || uri.startsWith("https://"));
    }
    
    @Override
    public Map<String, Object> getProtocolConfig() {
        return new HashMap<>(config);
    }
    
    private Message createHttpMessageFromData(Map<String, Object> httpData) {
        String messageId = (String) httpData.getOrDefault("id", "msg-" + System.nanoTime());
        
        // 创建头部
        HttpHeaders headers = new HttpHeaders();
        @SuppressWarnings("unchecked")
        Map<String, Object> headerData = (Map<String, Object>) httpData.get("headers");
        if (headerData != null) {
            headerData.forEach(headers::set);
        }
        
        // 创建消息体
        byte[] bodyData = (byte[]) httpData.getOrDefault("body", new byte[0]);
        HttpBody body = new HttpBody(bodyData);
        
        // 创建元数据并解析HTTP信息
        HttpMetadata metadata = new HttpMetadata();
        
        // 从RequestLine解析方法和路径
        String requestLine = headers.get("RequestLine", String.class);
        if (requestLine != null) {
            String[] parts = requestLine.split(" ");
            if (parts.length >= 2) {
                // 设置HTTP方法
                metadata.setMethod(parts[0]);
                
                // 设置路径（去除查询参数）
                String fullPath = parts[1];
                int queryIndex = fullPath.indexOf('?');
                String path = queryIndex > 0 ? fullPath.substring(0, queryIndex) : fullPath;
                metadata.setPath(path);
            }
        }
        
        return new HttpMessage(messageId, MessageType.REQUEST, httpProtocol, headers, body, metadata);
    }
    
    private Map<String, Object> convertToHttpData(HttpMessage message) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", message.getMessageId());
        result.put("headers", message.getHeaders().asMap());
        result.put("body", message.getBody().getBytes());
        return result;
    }
    
    private void validateHttpMessage(HttpMessage message) {
        MessageHeaders headers = message.getHeaders();
        if (!headers.contains("Host")) {
            throw new IllegalArgumentException("Missing Host header");
        }
    }
} 