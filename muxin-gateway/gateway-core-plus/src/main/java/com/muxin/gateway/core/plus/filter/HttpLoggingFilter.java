package com.muxin.gateway.core.plus.filter;

import com.muxin.gateway.core.plus.route.UniversalRequestContext;
import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.message.http.HttpMetadata;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * HTTP日志过滤器
 *
 * @author muxin
 */
@Slf4j
public class HttpLoggingFilter implements UniversalFilter {
    
    private final String name;
    private final FilterType type;
    private final int order;
    
    public HttpLoggingFilter(FilterType type, int order) {
        this.name = "HttpLoggingFilter-" + type.name();
        this.type = type;
        this.order = order;
    }
    
    @Override
    public void filter(UniversalRequestContext context, UniversalFilterChain chain) {
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
        return name;
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
        return true;
    }
    
    @Override
    public Set<Protocol> getSupportedProtocols() {
        Set<Protocol> protocols = new HashSet<>();
        protocols.add(new Protocol.HttpProtocol());
        return protocols;
    }
    
    private void logRequest(UniversalRequestContext context) {
        Message inbound = context.getInboundMessage();
        if (inbound != null && inbound.getMetadata() instanceof HttpMetadata) {
            HttpMetadata metadata = (HttpMetadata) inbound.getMetadata();
            log.info("[{}] 请求: {} {}", 
                type.name(), metadata.getMethod(), metadata.getPath());
        }
    }
    
    private void logResponse(UniversalRequestContext context, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("[{}] 响应完成，耗时: {}ms", 
            type.name(), duration);
    }
    
    private void logError(UniversalRequestContext context, Exception e, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.error("[{}] {}ms后发生错误: {}", 
            type.name(), duration, e.getMessage(), e);
    }
} 