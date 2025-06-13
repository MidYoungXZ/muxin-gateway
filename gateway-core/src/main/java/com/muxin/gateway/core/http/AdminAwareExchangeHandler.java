package com.muxin.gateway.core.http;

import com.muxin.gateway.core.admin.AdminHandler;
import com.muxin.gateway.core.utils.ResponseUtil;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理界面感知的交换处理器
 * 使用委托模式在现有的路由处理基础上添加管理界面支持
 */
@Slf4j
public class AdminAwareExchangeHandler implements ExchangeHandler {

    private final ExchangeHandler delegate;
    private final AdminHandler adminHandler;

    public AdminAwareExchangeHandler(ExchangeHandler delegate, AdminHandler adminHandler) {
        this.delegate = delegate;
        this.adminHandler = adminHandler;
    }

    @Override
    public void handle(ServerWebExchange exchange) {
        String path = exchange.getRequest().fullPath();

        // 检查是否为管理界面请求
        if (adminHandler != null && adminHandler.isAdminRequest(path)) {
            log.debug("Processing admin request: {}", exchange.getRequest().fullPath());
            FullHttpResponse response = adminHandler.handleRequest(exchange);
            exchange.setOriginalResponse(response);
            return;
        }

        // 委托给实际的处理器处理普通网关请求
        if (delegate != null) {
            delegate.handle(exchange);
        } else {
            log.error("No delegate handler available for request: {}", path);
            exchange.setOriginalResponse(ResponseUtil.error());
        }
    }
} 