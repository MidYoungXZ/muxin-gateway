package com.muxin.gateway.core.plus.message;


/**
 * 通用消息接口 - 协议无关的消息抽象
 *
 * @author muxin
 */
public interface Message {
    
    /**
     * 消息ID
     */
    String getMessageId();
    
    /**
     * 消息类型
     */
    MessageType getType();
    
    /**
     * 协议信息
     */
    Protocol getProtocol();
    
    /**
     * 消息头部
     */
    MessageHeaders getHeaders();
    
    /**
     * 消息体
     */
    MessageBody getBody();
    
    /**
     * 消息元数据
     */
    MessageMetadata getMetadata();
    
    /**
     * 创建响应消息
     */
    Message createResponse();
    
    /**
     * 消息克隆
     */
    Message copy();
} 