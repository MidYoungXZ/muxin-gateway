package com.muxin.gateway.refactory.message.http;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.connect.NettyServerConnection;
import com.muxin.gateway.refactory.message.*;
import com.muxin.gateway.refactory.node.EndpointAddress;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP协议适配器实现
 * 支持Netty HTTP对象的直接转换，基于泛型设计提供类型安全
 * 
 * 泛型参数：
 * - REQ: FullHttpRequest (HTTP请求)
 * - RESP: FullHttpResponse (HTTP响应)  
 * - CTX: ChannelHandlerContext (Netty上下文)
 * - CONN: NettyServerConnection (服务器连接)
 *
 * @author muxin
 */
@Slf4j
public class HttpProtocolAdapter implements ProtocolAdapter<FullHttpRequest, FullHttpResponse, ChannelHandlerContext, NettyServerConnection> {
    
    private final Protocol httpProtocol;
    private final Map<String, Object> config;
    
    public HttpProtocolAdapter() {
        this.httpProtocol = new Protocol.HttpProtocol();
        this.config = new HashMap<>();
        this.config.put("maxRequestSize", 10 * 1024 * 1024); // 10MB
        
        log.debug("[HttpProtocolAdapter] 创建HTTP协议适配器");
    }
    
    @Override
    public Protocol getSupportedProtocol() {
        return httpProtocol;
    }
    
    @Override
    public Message adaptInbound(FullHttpRequest request, ChannelHandlerContext context) {
        try {
            // 1. 生成消息ID
            String messageId = generateMessageId();
            
            // 2. 转换头部
            HttpHeaders refactoryHeaders = convertNettyHeaders(request.headers());
            
            // 3. 转换消息体
            HttpBody refactoryBody = convertNettyBody(request.content());
            
            // 4. 创建HTTP元数据
            HttpMetadata metadata = createHttpMetadata(request, context);
            
            // 5. 创建refactory消息
            HttpMessage refactoryMessage = new HttpMessage(
                messageId, 
                MessageType.REQUEST, 
                httpProtocol, 
                refactoryHeaders, 
                refactoryBody, 
                metadata
            );
            
            log.debug("[HttpProtocolAdapter] HTTP请求转换为refactory消息: {} -> {}", 
                request.uri(), messageId);
            
            return refactoryMessage;
            
        } catch (Exception e) {
            log.error("[HttpProtocolAdapter] HTTP请求转换失败", e);
            throw new RuntimeException("HTTP请求转换失败", e);
        }
    }
    
    @Override
    public FullHttpResponse adaptOutbound(Message message, ChannelHandlerContext context) {
        try {
            if (!(message instanceof HttpMessage)) {
                throw new IllegalArgumentException("消息类型不是HttpMessage: " + message.getClass());
            }
            
            HttpMessage httpMessage = (HttpMessage) message;
            
            // 1. 确定响应状态
            HttpResponseStatus status = determineResponseStatus(httpMessage);
            
            // 2. 转换消息体
            ByteBuf content = convertRefactoryBody(httpMessage.getBody());
            
            // 3. 创建Netty响应
            FullHttpResponse nettyResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, 
                status, 
                content
            );
            
            // 4. 转换头部
            convertRefactoryHeaders(httpMessage.getHeaders(), nettyResponse.headers());
            
            // 5. 设置必要的响应头
            setDefaultResponseHeaders(nettyResponse);
            
            log.debug("[HttpProtocolAdapter] refactory响应转换为HTTP响应: {} -> {}", 
                httpMessage.getMessageId(), status);
            
            return nettyResponse;
            
        } catch (Exception e) {
            log.error("[HttpProtocolAdapter] refactory响应转换失败", e);
            throw new RuntimeException("HTTP响应转换失败", e);
        }
    }
    
    @Override
    public NettyServerConnection createClientConnection(EndpointAddress address, Map<String, Object> options) {
        // TODO: 实现HTTP客户端连接创建
        log.warn("[HttpProtocolAdapter] HTTP客户端连接创建暂未实现");
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
    
    @Override
    public NettyServerConnection createServerConnection(ChannelHandlerContext context) {
        return new NettyServerConnection(context, httpProtocol);
    }
    
    @Override
    public Message createErrorResponse(int statusCode, String message) {
        String messageId = generateMessageId();
        
        // 创建响应头
        HttpHeaders refactoryHeaders = new HttpHeaders();
        refactoryHeaders.set("Status-Code", String.valueOf(statusCode));
        refactoryHeaders.set("Content-Type", "application/json;charset=UTF-8");
        refactoryHeaders.set("Server", "refactory-gateway/1.0");
        
        // 创建响应体
        String errorBody = String.format(
            "{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}", 
            getStatusMessage(statusCode), 
            message != null ? message : getStatusMessage(statusCode),
            System.currentTimeMillis()
        );
        HttpBody refactoryBody = new HttpBody(errorBody.getBytes(StandardCharsets.UTF_8), "application/json;charset=UTF-8");
        
        // 创建HTTP元数据
        HttpMetadata metadata = new HttpMetadata();
        metadata.setAttribute("statusCode", statusCode);
        metadata.setAttribute("statusMessage", getStatusMessage(statusCode));
        
        return new HttpMessage(
            messageId,
            MessageType.RESPONSE,
            httpProtocol,
            refactoryHeaders,
            refactoryBody,
            metadata
        );
    }
    
    @Override
    public Message createEmptyResponse() {
        String messageId = generateMessageId();
        
        // 创建响应头
        HttpHeaders refactoryHeaders = new HttpHeaders();
        refactoryHeaders.set("Status-Code", "204");
        refactoryHeaders.set("Content-Length", "0");
        refactoryHeaders.set("Server", "refactory-gateway/1.0");
        
        // 创建空响应体
        HttpBody refactoryBody = new HttpBody(new byte[0]);
        
        // 创建HTTP元数据
        HttpMetadata metadata = new HttpMetadata();
        metadata.setAttribute("statusCode", 204);
        metadata.setAttribute("statusMessage", "No Content");
        
        return new HttpMessage(
            messageId,
            MessageType.RESPONSE,
            httpProtocol,
            refactoryHeaders,
            refactoryBody,
            metadata
        );
    }
    
    // ========== 私有辅助方法 ==========
    
    private HttpHeaders convertNettyHeaders(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        HttpHeaders refactoryHeaders = new HttpHeaders();
        nettyHeaders.forEach(entry -> {
            refactoryHeaders.set(entry.getKey(), entry.getValue());
        });
        return refactoryHeaders;
    }
    
    private HttpBody convertNettyBody(ByteBuf content) {
        if (content == null || content.readableBytes() == 0) {
            return new HttpBody(new byte[0]);
        }
        byte[] bodyBytes = new byte[content.readableBytes()];
        content.readBytes(bodyBytes);
        return new HttpBody(bodyBytes);
    }
    
    private HttpMetadata createHttpMetadata(FullHttpRequest nettyRequest, ChannelHandlerContext ctx) {
        HttpMetadata metadata = new HttpMetadata();
        metadata.setMethod(nettyRequest.method().name());
        
        String uri = nettyRequest.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri, StandardCharsets.UTF_8);
        metadata.setPath(decoder.path());
        // 将查询参数存储在attributes中
        if (!decoder.parameters().isEmpty()) {
            metadata.setAttribute("queryParams", decoder.parameters());
        }
        
        // 设置地址信息
        if (ctx != null && ctx.channel() != null) {
            metadata.setAttribute("remoteAddress", ctx.channel().remoteAddress().toString());
            metadata.setAttribute("localAddress", ctx.channel().localAddress().toString());
        }
        
        return metadata;
    }
    
    private HttpResponseStatus determineResponseStatus(HttpMessage refactoryMessage) {
        String statusCode = refactoryMessage.getHeaders().get("Status-Code", String.class);
        if (statusCode != null) {
            try {
                int code = Integer.parseInt(statusCode);
                return HttpResponseStatus.valueOf(code);
            } catch (NumberFormatException e) {
                log.warn("[HttpProtocolAdapter] 无效的状态码: {}", statusCode);
            }
        }
        return HttpResponseStatus.OK;
    }
    
    private ByteBuf convertRefactoryBody(MessageBody refactoryBody) {
        if (refactoryBody == null) {
            return Unpooled.EMPTY_BUFFER;
        }
        byte[] bodyBytes = refactoryBody.getBytes();
        if (bodyBytes == null || bodyBytes.length == 0) {
            return Unpooled.EMPTY_BUFFER;
        }
        return Unpooled.copiedBuffer(bodyBytes);
    }
    
    private void convertRefactoryHeaders(MessageHeaders refactoryHeaders, 
                                       io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        if (refactoryHeaders == null) {
            return;
        }
        Map<String, Object> headerMap = refactoryHeaders.asMap();
        headerMap.forEach((name, value) -> {
            if (value != null && !name.equals("Status-Code")) {
                nettyHeaders.set(name, value.toString());
            }
        });
    }
    
    private void setDefaultResponseHeaders(FullHttpResponse response) {
        io.netty.handler.codec.http.HttpHeaders headers = response.headers();
        headers.set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (!headers.contains(HttpHeaderNames.CONTENT_TYPE)) {
            headers.set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        }
        headers.set(HttpHeaderNames.SERVER, "refactory-gateway/1.0");
    }
    
    private String generateMessageId() {
        return "http-msg-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 204: return "No Content";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }
    
    // ========== 原有方法保持兼容 ==========
    
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
} 