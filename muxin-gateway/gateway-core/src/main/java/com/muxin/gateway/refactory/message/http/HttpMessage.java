package com.muxin.gateway.refactory.message.http;

import com.muxin.gateway.refactory.message.*;

/**
 * HTTP消息实现
 *
 * @author muxin
 */
public class HttpMessage implements Message {
    
    private final String messageId;
    private final MessageType type;
    private final Protocol protocol;
    private final MessageHeaders headers;
    private final MessageBody body;
    private final MessageMetadata metadata;
    
    public HttpMessage(String messageId, MessageType type, Protocol protocol,
                      MessageHeaders headers, MessageBody body, MessageMetadata metadata) {
        this.messageId = messageId;
        this.type = type;
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
        this.metadata = metadata;
    }
    
    @Override
    public String getMessageId() {
        return messageId;
    }
    
    @Override
    public MessageType getType() {
        return type;
    }
    
    @Override
    public Protocol getProtocol() {
        return protocol;
    }
    
    @Override
    public MessageHeaders getHeaders() {
        return headers;
    }
    
    @Override
    public MessageBody getBody() {
        return body;
    }
    
    @Override
    public MessageMetadata getMetadata() {
        return metadata;
    }
    
    @Override
    public Message createResponse() {
        return new HttpMessage(
            generateResponseId(),
            MessageType.RESPONSE,
            protocol,
            new HttpHeaders(),
            new HttpBody(new byte[0]),
            metadata
        );
    }
    
    @Override
    public Message copy() {
        return new HttpMessage(messageId, type, protocol, headers, body, metadata);
    }
    
    private String generateResponseId() {
        return "resp-" + System.currentTimeMillis() + "-" + System.nanoTime();
    }
} 