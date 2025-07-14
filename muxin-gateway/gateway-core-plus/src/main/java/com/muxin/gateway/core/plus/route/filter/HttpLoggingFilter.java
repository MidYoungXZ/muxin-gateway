package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.protocol.message.ProtocolEnum;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.protocol.message.Message;
import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.protocol.message.http.HttpMetadata;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * HTTP日志过滤器
 * 支持可配置的日志记录参数
 *
 * @author muxin
 */
@Slf4j
@Builder
public class HttpLoggingFilter implements Filter {
    
    private final String name;
    @Builder.Default
    private final FilterType type = FilterType.PRE;
    private final int order;
    @Builder.Default
    private final boolean enabled = true;
    
    // 配置参数
    @Builder.Default
    private final boolean includeHeaders = true;
    @Builder.Default
    private final boolean includeBody = false;
    @Builder.Default
    private final int maxBodySize = 1024;
    
    @Override
    public void filter(RequestContext context, FilterChain chain) {
        long startTime = System.currentTimeMillis();
        
        try {
            logRequest(context);
            
            // 继续执行后续过滤器
            chain.filter(context);
            
            logResponse(context, startTime);
            
        } catch (Exception e) {
            logError(context, e, startTime);
            throw e;
        }
    }
    
    @Override
    public String getName() {
        return name != null ? name : "HttpLoggingFilter";
    }
    
    @Override
    public FilterType getType() {
        return type;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public Set<Protocol> getSupportedProtocols() {
        Set<Protocol> protocols = new HashSet<>();
        protocols.add(ProtocolEnum.HTTP);
        return protocols;
    }
    
    private void logRequest(RequestContext context) {
        Message inbound = context.getInboundMessage();
        if (inbound != null && inbound.getMetadata() instanceof HttpMetadata) {
            HttpMetadata metadata = (HttpMetadata) inbound.getMetadata();

            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append(String.format("[%s] 请求: %s %s", 
                type.name(), inbound.method(), inbound.url().getPath()));
            
            // 记录请求头 - 从attributes中获取headers
            if (includeHeaders) {
                Object headers = metadata.getAttribute("headers", Object.class);
                if (headers != null) {
                    logBuilder.append(", Headers: ").append(headers);
                }
            }
            
            // 记录请求体
            if (includeBody && inbound.getBody() != null && !inbound.getBody().isEmpty()) {
                String bodyContent = getBodyContent(inbound.getBody().getString());
                if (bodyContent != null && !bodyContent.isEmpty()) {
                    logBuilder.append(", Body: ").append(bodyContent);
                }
            }
            
            log.info(logBuilder.toString());
        }
    }
    
    private void logResponse(RequestContext context, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(String.format("[%s] 响应完成，耗时: %dms", type.name(), duration));
        
        Message outbound = context.getOutboundMessage();
        if (outbound != null) {
            // 记录响应头
            if (includeHeaders && outbound.getMetadata() instanceof HttpMetadata) {
                HttpMetadata metadata = (HttpMetadata) outbound.getMetadata();
                Object headers = metadata.getAttribute("headers", Object.class);
                if (headers != null) {
                    logBuilder.append(", Headers: ").append(headers);
                }
            }
            
            // 记录响应体
            if (includeBody && outbound.getBody() != null && !outbound.getBody().isEmpty()) {
                String bodyContent = getBodyContent(outbound.getBody().getString());
                if (bodyContent != null && !bodyContent.isEmpty()) {
                    logBuilder.append(", Body: ").append(bodyContent);
                }
            }
        }
        
        log.info(logBuilder.toString());
    }
    
    private void logError(RequestContext context, Exception e, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.error("[{}] {}ms后发生错误: {}", 
            type.name(), duration, e.getMessage(), e);
    }
    
    /**
     * 获取请求/响应体内容，限制大小
     */
    private String getBodyContent(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        if (content.length() > maxBodySize) {
            return content.substring(0, maxBodySize) + "...(truncated)";
        }
        
        return content;
    }
} 