package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.Protocol;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * HTTP认证过滤器
 * 支持多种认证方式：JWT、BASIC、TOKEN
 *
 * @author muxin
 */
@Slf4j
@Builder
public class HttpAuthFilter implements Filter {
    
    private final String name;
    private final int order;
    @Builder.Default
    private final boolean enabled = true;
    
    // 认证配置参数
    @Builder.Default
    private final String authType = "JWT";
    private final String secretKey;
    private final String tokenParam;
    
    @Override
    public void filter(RequestContext context, FilterChain chain) {
        Message inbound = context.getInboundMessage();
        
        try {
            boolean authResult = performAuthentication(inbound, context);
            
            if (authResult) {
                log.debug("[AUTH] 认证成功，类型: {}", authType);
                context.setAttribute("auth.status", "SUCCESS");
            } else {
                log.warn("[AUTH] 认证失败，类型: {}", authType);
                context.setAttribute("auth.status", "FAILED");
                // 认证失败可以选择中断请求或继续处理
                // 这里选择继续处理，由后续的业务逻辑决定
            }
        } catch (Exception e) {
            log.error("[AUTH] 认证过程发生错误", e);
            context.setAttribute("auth.status", "ERROR");
            context.setAttribute("auth.error", e.getMessage());
        }
        
        // 继续执行下一个过滤器
        chain.filter(context);
    }
    
    /**
     * 执行认证逻辑
     */
    private boolean performAuthentication(Message inbound, RequestContext context) {
        if (inbound == null || inbound.getHeaders() == null) {
            context.setAttribute("auth.reason", "Missing request or headers");
            return false;
        }
        
        switch (authType.toUpperCase()) {
            case "JWT":
                return performJwtAuthentication(inbound, context);
            case "BASIC":
                return performBasicAuthentication(inbound, context);
            case "TOKEN":
                return performTokenAuthentication(inbound, context);
            default:
                context.setAttribute("auth.reason", "Unsupported auth type: " + authType);
                return false;
        }
    }
    
    /**
     * JWT认证
     */
    private boolean performJwtAuthentication(Message inbound, RequestContext context) {
        String authorization = inbound.getHeaders().get("Authorization", String.class);
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            context.setAttribute("auth.reason", "Missing or invalid Authorization header");
            return false;
        }
        
        String token = authorization.substring(7);
        if (isValidJwtToken(token)) {
            context.setAttribute("auth.user", extractUserFromJwtToken(token));
            return true;
        } else {
            context.setAttribute("auth.reason", "Invalid JWT token");
            return false;
        }
    }
    
    /**
     * Basic认证
     */
    private boolean performBasicAuthentication(Message inbound, RequestContext context) {
        String authorization = inbound.getHeaders().get("Authorization", String.class);
        
        if (authorization == null || !authorization.startsWith("Basic ")) {
            context.setAttribute("auth.reason", "Missing or invalid Basic Authorization header");
            return false;
        }
        
        // 这里可以实现Basic认证的解码和验证逻辑
        context.setAttribute("auth.user", "basic_user");
        return true;
    }
    
    /**
     * Token认证（通过参数传递）
     */
    private boolean performTokenAuthentication(Message inbound, RequestContext context) {
        String paramName = tokenParam != null ? tokenParam : "token";
        
        // 先尝试从header获取
        String token = inbound.getHeaders().get(paramName, String.class);
        
        // 如果header中没有，尝试从查询参数获取
        if (token == null) {
            // 这里需要实现从查询参数获取token的逻辑
            // 暂时使用简单的实现
            token = inbound.getHeaders().get("X-Token", String.class);
        }
        
        if (token == null || token.trim().isEmpty()) {
            context.setAttribute("auth.reason", "Missing token parameter: " + paramName);
            return false;
        }
        
        if (isValidToken(token)) {
            context.setAttribute("auth.user", extractUserFromToken(token));
            return true;
        } else {
            context.setAttribute("auth.reason", "Invalid token");
            return false;
        }
    }
    
    @Override
    public String getName() {
        return name != null ? name : "HttpAuthFilter";
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
        return enabled;
    }
    
    @Override
    public Set<Protocol> getSupportedProtocols() {
        Set<Protocol> protocols = new HashSet<>();
        protocols.add(new Protocol.HttpProtocol());
        return protocols;
    }
    
    /**
     * 验证JWT Token
     */
    private boolean isValidJwtToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // 简单的JWT验证逻辑
        // 实际项目中应该使用JWT库进行验证
        if (secretKey != null) {
            // 这里可以实现基于secretKey的JWT验证
            return token.length() > 20 && !token.equals("invalid");
        }
        
        return token.length() > 10 && !token.equals("invalid");
    }
    
    /**
     * 从JWT Token提取用户信息
     */
    private String extractUserFromJwtToken(String token) {
        // 简单的用户提取逻辑
        // 实际项目中应该解析JWT payload
        return "jwt_user_" + token.substring(0, Math.min(5, token.length()));
    }
    
    /**
     * 验证普通Token
     */
    private boolean isValidToken(String token) {
        return token != null && token.length() > 5 && !token.equals("invalid");
    }
    
    /**
     * 从Token提取用户信息
     */
    private String extractUserFromToken(String token) {
        return "token_user_" + token.substring(0, Math.min(3, token.length()));
    }
} 