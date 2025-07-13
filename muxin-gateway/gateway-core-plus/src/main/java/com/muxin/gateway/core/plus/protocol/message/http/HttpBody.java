package com.muxin.gateway.core.plus.protocol.message.http;

import com.muxin.gateway.core.plus.protocol.message.MessageBody;
import com.muxin.gateway.core.plus.utils.JsonUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * HTTP消息体实现
 *
 * @author muxin
 */
public class HttpBody implements MessageBody {
    
    private final byte[] data;
    private final String contentType;
    private final boolean streaming;
    
    public HttpBody(byte[] data, String contentType) {
        this(data, contentType, false);
    }
    
    public HttpBody(byte[] data, String contentType, boolean streaming) {
        this.data = data != null ? data : new byte[0];
        this.contentType = contentType != null ? contentType : "application/octet-stream";
        this.streaming = streaming;
    }
    
    public HttpBody(String content, String contentType) {
        this(content != null ? content.getBytes(StandardCharsets.UTF_8) : new byte[0], contentType);
    }
    
    public HttpBody(String content) {
        this(content, "text/plain; charset=utf-8");
    }
    
    @Override
    public byte[] getBytes() {
        return data;
    }
    
    @Override
    public String getString() {
        return new String(data, StandardCharsets.UTF_8);
    }
    
    @Override
    public <T> T getContent(Class<T> type) {
        if (type == String.class) {
            return type.cast(getString());
        }
        
        if (type == byte[].class) {
            return type.cast(data);
        }
        
        // 对于其他类型，尝试JSON反序列化
        try {
            String json = getString();
            return JsonUtils.fromJson(json, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法将消息体转换为类型: " + type.getName(), e);
        }
    }
    
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }
    
    @Override
    public boolean isEmpty() {
        return data.length == 0;
    }
    
    @Override
    public long getContentLength() {
        return data.length;
    }
    
    @Override
    public String getContentType() {
        return contentType;
    }
    
    @Override
    public boolean isStreaming() {
        return streaming;
    }
    
    // 静态工厂方法
    public static HttpBody empty() {
        return new HttpBody(new byte[0], "application/octet-stream");
    }
    
    public static HttpBody of(String content) {
        return new HttpBody(content);
    }
    
    public static HttpBody of(String content, String contentType) {
        return new HttpBody(content, contentType);
    }
    
    public static HttpBody of(byte[] data, String contentType) {
        return new HttpBody(data, contentType);
    }
    
    public static HttpBody json(Object object) {
        try {
            String json = JsonUtils.toJson(object);
            return new HttpBody(json, "application/json; charset=utf-8");
        } catch (Exception e) {
            throw new IllegalArgumentException("无法序列化对象为JSON", e);
        }
    }
} 