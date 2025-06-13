package com.muxin.gateway.core.admin;

import com.muxin.gateway.core.http.ServerWebExchange;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 静态资源处理器
 * 用于服务管理界面的静态文件（HTML、CSS、JS等）
 */
@Slf4j
public class StaticResourceHandler {

    private static final Map<String, String> CONTENT_TYPE_MAP = new HashMap<>();
    
    static {
        CONTENT_TYPE_MAP.put("html", "text/html; charset=utf-8");
        CONTENT_TYPE_MAP.put("css", "text/css; charset=utf-8");
        CONTENT_TYPE_MAP.put("js", "application/javascript; charset=utf-8");
        CONTENT_TYPE_MAP.put("json", "application/json; charset=utf-8");
        CONTENT_TYPE_MAP.put("png", "image/png");
        CONTENT_TYPE_MAP.put("jpg", "image/jpeg");
        CONTENT_TYPE_MAP.put("jpeg", "image/jpeg");
        CONTENT_TYPE_MAP.put("gif", "image/gif");
        CONTENT_TYPE_MAP.put("ico", "image/x-icon");
    }

    private final String resourcePath;

    public StaticResourceHandler(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * 处理静态资源请求
     */
    public FullHttpResponse handleRequest(ServerWebExchange exchange, String resourceName) {
        try {
            // 安全检查，防止路径遍历攻击
            if (resourceName.contains("..") || resourceName.contains("//")) {
                return createNotFoundResponse();
            }

            // 默认文件处理
            if (resourceName.isEmpty() || resourceName.equals("/")) {
                resourceName = "index.html";
            }

            // 构建资源路径
            String fullResourcePath = resourcePath + "/" + resourceName;
            ClassPathResource resource = new ClassPathResource(fullResourcePath);

            if (!resource.exists()) {
                log.debug("Static resource not found: {}", fullResourcePath);
                return createNotFoundResponse();
            }

            // 读取文件内容
            byte[] content;
            try (InputStream inputStream = resource.getInputStream()) {
                content = inputStream.readAllBytes();
            }

            // 确定Content-Type
            String contentType = getContentType(resourceName);
            
            // 创建响应
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, 
                HttpResponseStatus.OK, 
                Unpooled.wrappedBuffer(content)
            );
            
            // 设置响应头
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
            
            // 特殊处理favicon的缓存
            if (resourceName.equals("favicon.ico")) {
                response.headers().set(HttpHeaderNames.CACHE_CONTROL, "public, max-age=86400"); // 1天
                response.headers().set("Expires", "Thu, 31 Dec 2099 23:59:59 GMT");
            } else {
                response.headers().set(HttpHeaderNames.CACHE_CONTROL, "max-age=3600");
            }
            
            log.debug("Served static resource: {} ({}bytes)", fullResourcePath, content.length);
            return response;

        } catch (Exception e) {
            log.error("Error serving static resource: {}", resourceName, e);
            return createErrorResponse();
        }
    }

    private String getContentType(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            String extension = fileName.substring(lastDot + 1).toLowerCase();
            return CONTENT_TYPE_MAP.getOrDefault(extension, "application/octet-stream");
        }
        return "application/octet-stream";
    }

    private FullHttpResponse createNotFoundResponse() {
        String content = "<!DOCTYPE html><html><head><title>404 Not Found</title></head>" +
                        "<body><h1>404 - Resource Not Found</h1></body></html>";
        ByteBuf buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, 
            HttpResponseStatus.NOT_FOUND, 
            buffer
        );
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
        
        return response;
    }

    private FullHttpResponse createErrorResponse() {
        String content = "<!DOCTYPE html><html><head><title>500 Internal Server Error</title></head>" +
                        "<body><h1>500 - Internal Server Error</h1></body></html>";
        ByteBuf buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, 
            HttpResponseStatus.INTERNAL_SERVER_ERROR, 
            buffer
        );
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
        
        return response;
    }
} 