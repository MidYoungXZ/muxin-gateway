package com.muxin.gateway.refactory.server;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.message.ProtocolAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 泛型协议服务器抽象基类
 * 提供协议无关的服务器模板逻辑，支持任意协议的实现
 * 
 * @param <REQ> 协议特定的请求类型
 * @param <RESP> 协议特定的响应类型  
 * @param <CTX> 协议特定的上下文类型
 * @param <CONN> 协议特定的连接类型
 * 
 * @author muxin
 */
@Slf4j
public abstract class GenericProtocolServer<REQ, RESP, CTX, CONN extends Connection> 
    extends AbstractProtocolServer {
    
    protected final ProtocolAdapter<REQ, RESP, CTX, CONN> protocolAdapter;
    
    /**
     * 构造函数
     * 
     * @param protocol 支持的协议
     * @param port 监听端口
     * @param protocolAdapter 协议适配器
     */
    public GenericProtocolServer(Protocol protocol, int port, 
                               ProtocolAdapter<REQ, RESP, CTX, CONN> protocolAdapter) {
        super(protocol, port, protocolAdapter.getProtocolConfig());
        this.protocolAdapter = protocolAdapter;
        
        log.info("[GenericProtocolServer] 创建泛型协议服务器 - 协议: {}, 端口: {}", 
            protocol.getName(), port);
    }
    
    /**
     * 处理入站请求的模板方法
     * 定义了完整的请求处理流程，协议无关
     * 
     * @param request 协议特定的请求对象
     * @param context 协议特定的上下文对象
     */
    protected final void handleInboundRequest(REQ request, CTX context) {
        recordMessage();
        
        log.debug("[GenericProtocolServer] 处理入站请求 - 协议: {}", protocol.getName());
        
        try {
            // 1. 创建连接
            CONN connection = protocolAdapter.createServerConnection(context);
            
            // 2. 协议适配：协议特定请求 -> 统一消息
            Message message = protocolAdapter.adaptInbound(request, context);
            
            // 3. 业务处理：调用MessageHandler
            if (messageHandler == null) {
                log.error("[GenericProtocolServer] MessageHandler未设置");
                handleError(context, new IllegalStateException("MessageHandler未设置"));
                return;
            }
            
            CompletableFuture<Message> responseFuture = messageHandler.handleMessage(message, connection);
            
            // 4. 响应处理：异步处理响应
            responseFuture.whenComplete((response, throwable) -> {
                if (throwable != null) {
                    log.error("[GenericProtocolServer] 业务处理异常", throwable);
                    recordError();
                    handleError(context, throwable);
                } else {
                    handleResponse(response, context);
                }
            });
            
        } catch (Exception e) {
            log.error("[GenericProtocolServer] 处理入站请求异常", e);
            recordError();
            handleError(context, e);
        }
    }
    
    /**
     * 处理响应的模板方法
     * 将统一消息转换为协议特定响应并写出
     * 
     * @param response 统一的响应消息
     * @param context 协议特定的上下文对象
     */
    protected final void handleResponse(Message response, CTX context) {
        try {
            if (response != null) {
                // 协议适配：统一消息 -> 协议特定响应
                RESP protocolResponse = protocolAdapter.adaptOutbound(response, context);
                writeResponse(protocolResponse, context);
                
                log.debug("[GenericProtocolServer] 响应发送成功 - 消息ID: {}", response.getMessageId());
            } else {
                // 发送空响应
                log.warn("[GenericProtocolServer] 响应消息为空，发送空响应");
                sendEmptyResponse(context);
            }
            
        } catch (Exception e) {
            log.error("[GenericProtocolServer] 处理响应异常", e);
            recordError();
            handleError(context, e);
        }
    }
    
    /**
     * 处理错误的模板方法
     * 创建错误响应并发送
     * 
     * @param context 协议特定的上下文对象
     * @param error 错误对象
     */
    protected final void handleError(CTX context, Throwable error) {
        try {
            // 创建错误响应
            Message errorResponse = protocolAdapter.createErrorResponse(500, 
                "服务器内部错误: " + error.getMessage());
            
            // 转换并发送错误响应
            RESP protocolResponse = protocolAdapter.adaptOutbound(errorResponse, context);
            writeResponse(protocolResponse, context);
            
            log.debug("[GenericProtocolServer] 错误响应发送成功");
            
        } catch (Exception e) {
            log.error("[GenericProtocolServer] 发送错误响应失败", e);
            // 最后的兜底：直接关闭连接
            closeConnection(context);
        }
    }
    
    /**
     * 发送空响应
     * 
     * @param context 协议特定的上下文对象
     */
    protected final void sendEmptyResponse(CTX context) {
        try {
            Message emptyResponse = protocolAdapter.createEmptyResponse();
            RESP protocolResponse = protocolAdapter.adaptOutbound(emptyResponse, context);
            writeResponse(protocolResponse, context);
            
            log.debug("[GenericProtocolServer] 空响应发送成功");
            
        } catch (Exception e) {
            log.error("[GenericProtocolServer] 发送空响应失败", e);
            closeConnection(context);
        }
    }
    
    /**
     * 处理连接建立事件
     * 
     * @param context 协议特定的上下文对象
     */
    protected final void handleConnectionEstablished(CTX context) {
        recordConnectionEstablished();
        
        if (messageHandler != null) {
            try {
                CONN connection = protocolAdapter.createServerConnection(context);
                messageHandler.onConnectionEstablished(connection);
            } catch (Exception e) {
                log.error("[GenericProtocolServer] 处理连接建立事件异常", e);
            }
        }
    }
    
    /**
     * 处理连接关闭事件
     * 
     * @param context 协议特定的上下文对象
     */
    protected final void handleConnectionClosed(CTX context) {
        recordConnectionClosed();
        
        if (messageHandler != null) {
            try {
                CONN connection = protocolAdapter.createServerConnection(context);
                messageHandler.onConnectionClosed(connection);
            } catch (Exception e) {
                log.error("[GenericProtocolServer] 处理连接关闭事件异常", e);
            }
        }
    }
    
    /**
     * 处理连接异常事件
     * 
     * @param context 协议特定的上下文对象
     * @param cause 异常原因
     */
    protected final void handleConnectionException(CTX context, Throwable cause) {
        recordError();
        
        if (messageHandler != null) {
            try {
                CONN connection = protocolAdapter.createServerConnection(context);
                messageHandler.onException(connection, cause);
            } catch (Exception e) {
                log.error("[GenericProtocolServer] 处理连接异常事件异常", e);
            }
        }
    }
    
    // ========== 抽象方法：子类实现协议特定逻辑 ==========
    
    /**
     * 写入响应到客户端
     * 子类实现具体的响应写入逻辑
     * 
     * @param response 协议特定的响应对象
     * @param context 协议特定的上下文对象
     */
    protected abstract void writeResponse(RESP response, CTX context);
    
    /**
     * 关闭连接
     * 子类实现具体的连接关闭逻辑
     * 
     * @param context 协议特定的上下文对象
     */
    protected abstract void closeConnection(CTX context);
    
    // ========== 访问器方法 ==========
    
    /**
     * 获取协议适配器
     * 
     * @return 协议适配器实例
     */
    public ProtocolAdapter<REQ, RESP, CTX, CONN> getProtocolAdapter() {
        return protocolAdapter;
    }
    
    /**
     * 获取服务器统计信息
     * 
     * @return 包含泛型协议服务器特定统计的Map
     */
    @Override
    public Map<String, Object> getServerStats() {
        Map<String, Object> stats = super.getServerStats();
        stats.put("adapterClass", protocolAdapter.getClass().getSimpleName());
        return stats;
    }
} 