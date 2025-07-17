package com.muxin.gateway.core.plus.connect;


import com.muxin.gateway.core.plus.message.Message;

import java.util.concurrent.CompletableFuture;

/**
 * 服务端连接接口
 * 网关作为服务端接收客户端请求时使用
 * 
 * @author muxin
 */
public interface ServerConnection extends Connection {
    
    // ========== 响应发送 ==========
    
    /**
     * 发送响应给客户端
     * 
     * @param response 响应消息
     * @return 发送操作的Future
     */
    CompletableFuture<Void> sendResponse(Message response);
    
    /**
     * 发送错误响应给客户端
     * 
     * @param error 错误信息
     * @return 发送操作的Future
     */
    CompletableFuture<Void> sendError(Throwable error);
    
    // ========== 连接管理 ==========
    
    /**
     * 获取客户端地址
     */
    String getClientAddress();

} 