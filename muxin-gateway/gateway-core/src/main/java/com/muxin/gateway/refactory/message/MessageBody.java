package com.muxin.gateway.refactory.message;


import java.io.InputStream;

/**
 * 消息体接口
 *
 * @author muxin
 */
public interface MessageBody {
    
    /**
     * 获取原始字节数据
     */
    byte[] getBytes();
    
    /**
     * 获取字符串内容
     */
    String getString();
    
    /**
     * 获取结构化内容
     */
    <T> T getContent(Class<T> type);
    
    /**
     * 获取输入流
     */
    InputStream getInputStream();
    
    /**
     * 是否为空
     */
    boolean isEmpty();
    
    /**
     * 内容长度
     */
    long getContentLength();
    
    /**
     * 内容类型
     */
    String getContentType();
    
    /**
     * 是否为流式内容
     */
    boolean isStreaming();
} 