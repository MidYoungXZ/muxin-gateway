package com.muxin.gateway.core.plus.filter;

import com.muxin.gateway.core.plus.route.UniversalRequestContext;
import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * HTTP认证过滤器
 *
 * @author muxin
 */
@Slf4j
public class HttpAuthFilter implements UniversalFilter {
    
    private final int order;
    
    public HttpAuthFilter(int order) {
        this.order = order;
    }
    
    @Override
    public void filter(UniversalRequestContext context, UniversalFilterChain chain) {
        Message inbound = context.getInboundMessage();
        
        if (inbound != null && inbound.getHeaders() != null) {
            String authorization = inbound.getHeaders().get("Authorization", String.class);
            
            if (authorization == null || authorization.isEmpty()) {
                log.warn("[AUTH] 未找到授权头");
                // 设置认证失败标记
                context.setAttribute("auth.status", "FAILED");
                context.setAttribute("auth.reason", "Missing Authorization header");
            } else if (authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                if (isValidToken(token)) {
                    log.debug("[AUTH] Token验证成功");
                    context.setAttribute("auth.status", "SUCCESS");
                    context.setAttribute("auth.user", extractUserFromToken(token));
                } else {
                    log.warn("[AUTH] 无效的token");
                    context.setAttribute("auth.status", "FAILED");
                    context.setAttribute("auth.reason", "Invalid token");
                }
            } else {
                log.warn("[AUTH] 无效的授权格式");
                context.setAttribute("auth.status", "FAILED");
                context.setAttribute("auth.reason", "Invalid authorization format");
            }
        }
        
        // 继续执行下一个过滤器
        chain.filter(context);
    }
    
    @Override
    public String getName() {
        return "HttpAuthFilter";
    }
    
    @Override
    public FilterType getType() {
        return FilterType.PRE;
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
    
    private boolean isValidToken(String token) {
        // 简单的token验证逻辑
        return token != null && token.length() > 10 && !token.equals("invalid");
    }
    
    private String extractUserFromToken(String token) {
        // 简单的用户提取逻辑
        return "user_" + token.substring(0, 5);
    }
} 