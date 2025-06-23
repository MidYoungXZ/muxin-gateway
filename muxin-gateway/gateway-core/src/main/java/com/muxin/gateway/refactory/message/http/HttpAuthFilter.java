package com.muxin.gateway.refactory.message.http;

import com.muxin.gateway.refactory.*;
import com.muxin.gateway.refactory.filter.FilterType;
import com.muxin.gateway.refactory.filter.UniversalFilter;
import com.muxin.gateway.refactory.filter.UniversalFilterChain;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.route.UniversalRequestContext;

import java.util.HashSet;
import java.util.Set;

/**
 * HTTP认证过滤器
 *
 * @author muxin
 */
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
                System.out.println("[AUTH] No authorization header found");
                // 设置认证失败标记
                context.setAttribute("auth.status", "FAILED");
                context.setAttribute("auth.reason", "Missing Authorization header");
            } else if (authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                if (isValidToken(token)) {
                    System.out.println("[AUTH] Token validation successful");
                    context.setAttribute("auth.status", "SUCCESS");
                    context.setAttribute("auth.user", extractUserFromToken(token));
                } else {
                    System.out.println("[AUTH] Invalid token");
                    context.setAttribute("auth.status", "FAILED");
                    context.setAttribute("auth.reason", "Invalid token");
                }
            } else {
                System.out.println("[AUTH] Invalid authorization format");
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
        protocols.add(new HttpProtocol());
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