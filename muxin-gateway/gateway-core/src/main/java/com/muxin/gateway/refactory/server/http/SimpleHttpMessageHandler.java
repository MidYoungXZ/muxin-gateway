package com.muxin.gateway.refactory.server.http;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.MessageType;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.message.http.HttpBody;
import com.muxin.gateway.refactory.message.http.HttpHeaders;
import com.muxin.gateway.refactory.message.http.HttpMessage;
import com.muxin.gateway.refactory.message.http.HttpMetadata;
import com.muxin.gateway.refactory.server.MessageHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * 简单的HTTP消息处理器
 * 用于测试NettyHttpServer和HttpServerHandler的集成
 * 
 * @author muxin
 */
@Slf4j
public class SimpleHttpMessageHandler implements MessageHandler {
    
    private static final Protocol HTTP_PROTOCOL = new Protocol.HttpProtocol();
    
    @Override
    public CompletableFuture<Message> handleMessage(Message message, Connection connection) {
        log.info("[SimpleHttpMessageHandler] 处理消息 - ID: {}, 类型: {}, 连接: {}", 
            message.getMessageId(), message.getType(), connection.getConnectionId());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 检查是否是HTTP消息
                if (!(message instanceof HttpMessage)) {
                    log.warn("[SimpleHttpMessageHandler] 不支持的消息类型: {}", message.getClass());
                    return createErrorResponse("不支持的消息类型");
                }
                
                HttpMessage httpMessage = (HttpMessage) message;
                HttpMetadata metadata = (HttpMetadata) httpMessage.getMetadata();
                
                String method = metadata.getMethod();
                String path = metadata.getPath();
                
                log.info("[SimpleHttpMessageHandler] 处理HTTP请求 - {} {}", method, path);
                
                // 根据路径处理不同的请求
                switch (path) {
                    case "/health":
                        return createHealthResponse();
                    case "/echo":
                        return createEchoResponse(httpMessage);
                    case "/info":
                        return createInfoResponse(connection);
                    default:
                        return createHelloResponse(method, path);
                }
                
            } catch (Exception e) {
                log.error("[SimpleHttpMessageHandler] 处理消息异常", e);
                return createErrorResponse("处理请求异常: " + e.getMessage());
            }
        });
    }
    
    /**
     * 创建健康检查响应
     */
    private Message createHealthResponse() {
        String responseBody = String.format(
            "{\"status\":\"UP\",\"timestamp\":\"%s\",\"service\":\"refactory-http-server\"}", 
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        return createJsonResponse(responseBody, 200);
    }
    
    /**
     * 创建回显响应
     */
    private Message createEchoResponse(HttpMessage request) {
        String requestBody = request.getBody().getString();
        String responseBody = String.format(
            "{\"echo\":%s,\"method\":\"%s\",\"path\":\"%s\",\"timestamp\":\"%s\"}", 
            requestBody.isEmpty() ? "\"\"" : "\"" + requestBody + "\"",
            ((HttpMetadata) request.getMetadata()).getMethod(),
            ((HttpMetadata) request.getMetadata()).getPath(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        return createJsonResponse(responseBody, 200);
    }
    
    /**
     * 创建信息响应
     */
    private Message createInfoResponse(Connection connection) {
        String responseBody = String.format(
            "{\"connection\":\"%s\",\"protocol\":\"%s\",\"local\":\"%s\",\"remote\":\"%s\",\"timestamp\":\"%s\"}", 
            connection.getConnectionId(),
            connection.getProtocol().getName(),
            connection.getLocalAddress().toUri(),
            connection.getRemoteAddress().toUri(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        return createJsonResponse(responseBody, 200);
    }
    
    /**
     * 创建Hello响应
     */
    private Message createHelloResponse(String method, String path) {
        String responseBody = String.format(
            "{\"message\":\"Hello from refactory HTTP server!\",\"method\":\"%s\",\"path\":\"%s\",\"timestamp\":\"%s\"}", 
            method, path, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        return createJsonResponse(responseBody, 200);
    }
    
    /**
     * 创建错误响应
     */
    private Message createErrorResponse(String error) {
        String responseBody = String.format(
            "{\"error\":\"%s\",\"timestamp\":\"%s\"}", 
            error, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        return createJsonResponse(responseBody, 500);
    }
    
    /**
     * 创建JSON响应
     */
    private Message createJsonResponse(String jsonBody, int statusCode) {
        String messageId = "resp-" + System.currentTimeMillis() + "-" + System.nanoTime();
        
        // 创建响应头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Status-Code", String.valueOf(statusCode));
        headers.set("Content-Type", "application/json;charset=UTF-8");
        headers.set("Content-Length", String.valueOf(jsonBody.getBytes(StandardCharsets.UTF_8).length));
        headers.set("Server", "refactory-gateway/1.0");
        headers.set("Access-Control-Allow-Origin", "*");
        
        // 创建响应体
        HttpBody body = new HttpBody(jsonBody.getBytes(StandardCharsets.UTF_8), "application/json;charset=UTF-8");
        
        // 创建元数据
        HttpMetadata metadata = new HttpMetadata();
        metadata.setAttribute("statusCode", statusCode);
        metadata.setAttribute("statusMessage", getStatusMessage(statusCode));
        
        return new HttpMessage(messageId, MessageType.RESPONSE, HTTP_PROTOCOL, headers, body, metadata);
    }
    
    /**
     * 获取状态消息
     */
    private String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }
    
    @Override
    public void onConnectionEstablished(Connection connection) {
        log.info("[SimpleHttpMessageHandler] 连接建立 - {}", connection.getConnectionId());
    }
    
    @Override
    public void onConnectionClosed(Connection connection) {
        log.info("[SimpleHttpMessageHandler] 连接关闭 - {}", connection.getConnectionId());
    }
    
    @Override
    public void onException(Connection connection, Throwable cause) {
        log.error("[SimpleHttpMessageHandler] 连接异常 - {}", connection.getConnectionId(), cause);
    }
} 