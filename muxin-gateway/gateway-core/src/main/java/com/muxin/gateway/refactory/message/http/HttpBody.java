package com.muxin.gateway.refactory.message.http;

import com.muxin.gateway.refactory.message.MessageBody;

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
    
    public HttpBody(byte[] data) {
        this(data, "application/octet-stream");
    }
    
    public HttpBody(byte[] data, String contentType) {
        this.data = data != null ? data.clone() : new byte[0];
        this.contentType = contentType != null ? contentType : "application/octet-stream";
    }
    
    @Override
    public byte[] getBytes() {
        return data.clone();
    }
    
    @Override
    public String getString() {
        return new String(data, StandardCharsets.UTF_8);
    }
    
    @Override
    public <T> T getContent(Class<T> type) {
        if (type == String.class) {
            return type.cast(getString());
        } else if (type == byte[].class) {
            return type.cast(getBytes());
        } else if (type == InputStream.class) {
            return type.cast(getInputStream());
        }
        throw new IllegalArgumentException("Unsupported content type: " + type);
    }
    
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }
    
    @Override
    public long getContentLength() {
        return data.length;
    }
    
    @Override
    public boolean isEmpty() {
        return data.length == 0;
    }
    
    @Override
    public String getContentType() {
        return contentType;
    }
    
    @Override
    public boolean isStreaming() {
        return false; // HTTP body is typically loaded into memory
    }
    
    // 辅助方法
    public String asString(String charset) {
        try {
            return new String(data, charset);
        } catch (Exception e) {
            return getString();
        }
    }
} 