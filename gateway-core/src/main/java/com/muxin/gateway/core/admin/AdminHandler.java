package com.muxin.gateway.core.admin;

import com.muxin.gateway.core.config.AdminProperties;
import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.route.RouteDefinitionRepository;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理界面主处理器
 * 统一处理管理界面相关的请求
 */
@Slf4j
public class AdminHandler {

    private final AdminProperties adminProperties;
    private final StaticResourceHandler staticResourceHandler;
    private final AdminApiHandler adminApiHandler;

    public AdminHandler(AdminProperties adminProperties, RouteDefinitionRepository routeRepository) {
        this.adminProperties = adminProperties;
        this.staticResourceHandler = new StaticResourceHandler("admin");
        this.adminApiHandler = new AdminApiHandler(adminProperties, routeRepository);
    }

    /**
     * 判断是否为管理界面请求
     */
    public boolean isAdminRequest(String path) {
        if (!adminProperties.isEnabled()) {
            return false;
        }
        
        // 处理系统级请求（Chrome DevTools、浏览器发现协议等）
        if (path.startsWith("/.well-known/")) {
            return true; // 让这些请求被处理但返回404
        }
        
        // 管理界面路径或者各种favicon请求路径
        return path.startsWith(adminProperties.getPathPrefix()) || 
               path.equals("/favicon.ico") || 
               path.equals("/admin/favicon.ico") ||
               path.endsWith("favicon.ico");
    }

    /**
     * 处理管理界面请求
     */
    public FullHttpResponse handleRequest(ServerWebExchange exchange) {
        if (!adminProperties.isEnabled()) {
            log.warn("Admin interface is disabled");
            return createDisabledResponse();
        }

        String fullPath = exchange.getRequest().fullPath();
        
        // 处理系统级请求（Chrome DevTools等）
        if (fullPath.startsWith("/.well-known/")) {
            log.debug("Ignoring system-level request: {}", fullPath);
            return createSystemRequestNotFoundResponse(fullPath);
        }
        
        // 特殊处理各种favicon请求
        if (fullPath.equals("/favicon.ico") || 
            fullPath.equals("/admin/favicon.ico") || 
            fullPath.endsWith("favicon.ico")) {
            log.debug("Processing favicon request: {}", fullPath);
            return staticResourceHandler.handleRequest(exchange, "favicon.ico");
        }
        
        String relativePath = fullPath.substring(adminProperties.getPathPrefix().length());
        
        // 去掉开头的/
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        log.debug("Processing admin request: {} -> {}", fullPath, relativePath);

        // API请求处理
        if (relativePath.startsWith("api/")) {
            String apiPath = "/" + relativePath.substring(4); // 去掉"api/"前缀
            return adminApiHandler.handleRequest(exchange, apiPath);
        }

        // 静态资源处理
        return staticResourceHandler.handleRequest(exchange, relativePath);
    }

    /**
     * 创建系统级请求的404响应
     */
    private FullHttpResponse createSystemRequestNotFoundResponse(String path) {
        log.debug("System request not found: {}", path);
        
        io.netty.buffer.ByteBuf buffer = io.netty.buffer.Unpooled.buffer(0);
        
        FullHttpResponse response = new io.netty.handler.codec.http.DefaultFullHttpResponse(
            io.netty.handler.codec.http.HttpVersion.HTTP_1_1, 
            io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND, 
            buffer
        );
        
        response.headers().set(io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=utf-8");
        response.headers().set(io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH, 0);
        
        return response;
    }

    private FullHttpResponse createDisabledResponse() {
        // 创建一个简单的禁用页面响应
        String content = "<!DOCTYPE html><html><head><title>Admin Disabled</title></head>" +
                        "<body><h1>Admin Interface Disabled</h1>" +
                        "<p>The admin interface has been disabled.</p></body></html>";
        
        io.netty.buffer.ByteBuf buffer = io.netty.buffer.Unpooled.copiedBuffer(content, io.netty.util.CharsetUtil.UTF_8);
        
        FullHttpResponse response = new io.netty.handler.codec.http.DefaultFullHttpResponse(
            io.netty.handler.codec.http.HttpVersion.HTTP_1_1, 
            io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE, 
            buffer
        );
        
        response.headers().set(io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
        response.headers().set(io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
        
        return response;
    }
} 