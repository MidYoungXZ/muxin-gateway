package com.muxin.gateway.core.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muxin.gateway.core.config.AdminProperties;
import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.route.RouteDefinition;
import com.muxin.gateway.core.route.RouteDefinitionRepository;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理API处理器
 * 提供登录认证、路由查询等管理接口
 */
@Slf4j
public class AdminApiHandler {

    private final AdminProperties adminProperties;
    private final RouteDefinitionRepository routeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 简单的内存Session存储
    private final Map<String, AdminSession> sessions = new ConcurrentHashMap<>();

    public AdminApiHandler(AdminProperties adminProperties, RouteDefinitionRepository routeRepository) {
        this.adminProperties = adminProperties;
        this.routeRepository = routeRepository;
    }

    /**
     * 处理管理API请求
     */
    public FullHttpResponse handleRequest(ServerWebExchange exchange, String apiPath) {
        try {
            String method = exchange.getRequest().method().name();
            
            // 路由API请求
            switch (apiPath) {
                case "/login":
                    if ("POST".equals(method)) {
                        return handleLogin(exchange);
                    }
                    break;
                case "/logout":
                    return handleLogout(exchange);
                case "/routes":
                    if (isAuthenticated(exchange)) {
                        return handleGetRoutes(exchange);
                    } else {
                        return createUnauthorizedResponse();
                    }
                case "/session/check":
                    return handleSessionCheck(exchange);
                default:
                    if (apiPath.startsWith("/routes/")) {
                        if (isAuthenticated(exchange)) {
                            String routeId = apiPath.substring("/routes/".length());
                            return handleGetRouteDetail(exchange, routeId);
                        } else {
                            return createUnauthorizedResponse();
                        }
                    }
            }
            
            return createNotFoundResponse();
            
        } catch (Exception e) {
            log.error("Error handling admin API request: {}", apiPath, e);
            return createErrorResponse("Internal server error");
        }
    }

    /**
     * 处理登录请求
     */
    private FullHttpResponse handleLogin(ServerWebExchange exchange) throws IOException {
        // 读取请求体（临时简化实现）
        String body = getRequestBody(exchange);
        Map<String, String> params = parseFormData(body);
        
        String username = params.getOrDefault("username", "admin");
        String password = params.getOrDefault("password", "admin123");

        // 验证用户名密码
        if (adminProperties.getUsername().equals(username) && 
            adminProperties.getPassword().equals(password)) {
            
            // 创建Session
            String sessionId = UUID.randomUUID().toString();
            AdminSession session = new AdminSession(sessionId, username, System.currentTimeMillis());
            sessions.put(sessionId, session);
            
            // 清理过期Session
            cleanExpiredSessions();
            
            // 创建成功响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("sessionId", sessionId);
            result.put("message", "登录成功");
            
            FullHttpResponse response = createJsonResponse(HttpResponseStatus.OK, result);
            // 设置Session Cookie
            response.headers().add(HttpHeaderNames.SET_COOKIE, 
                "GATEWAY_SESSION_ID=" + sessionId + "; Path=" + adminProperties.getPathPrefix() + "; HttpOnly");
            
            log.info("Admin user logged in: {}", username);
            return response;
            
        } else {
            // 登录失败
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            
            return createJsonResponse(HttpResponseStatus.UNAUTHORIZED, result);
        }
    }

    /**
     * 处理退出登录
     */
    private FullHttpResponse handleLogout(ServerWebExchange exchange) {
        String sessionId = getSessionId(exchange);
        if (sessionId != null) {
            sessions.remove(sessionId);
            log.info("Admin user logged out, session: {}", sessionId);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "退出成功");
        
        FullHttpResponse response = createJsonResponse(HttpResponseStatus.OK, result);
        // 清除Session Cookie
        response.headers().add(HttpHeaderNames.SET_COOKIE, 
            "GATEWAY_SESSION_ID=; Path=" + adminProperties.getPathPrefix() + "; Max-Age=0");
        
        return response;
    }

    /**
     * 处理获取路由列表
     */
    private FullHttpResponse handleGetRoutes(ServerWebExchange exchange) throws IOException {
        Iterable<RouteDefinition> routeIterable = routeRepository.findAll();
        List<RouteDefinition> routes = new ArrayList<>();
        routeIterable.forEach(routes::add);
        
        // 转换为简化的路由信息
        List<Map<String, Object>> routeList = new ArrayList<>();
        for (RouteDefinition route : routes) {
            Map<String, Object> routeInfo = new HashMap<>();
            routeInfo.put("id", route.getId());
            routeInfo.put("uri", route.getUri());
            routeInfo.put("order", route.getOrder());
            routeInfo.put("predicatesCount", route.getPredicates().size());
            routeInfo.put("filtersCount", route.getFilters().size());
            routeInfo.put("metadata", route.getMetadata());
            routeList.add(routeInfo);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", routeList);
        result.put("total", routeList.size());
        
        return createJsonResponse(HttpResponseStatus.OK, result);
    }

    /**
     * 处理获取路由详情
     */
    private FullHttpResponse handleGetRouteDetail(ServerWebExchange exchange, String routeId) throws IOException {
        Iterable<RouteDefinition> routeIterable = routeRepository.findAll();
        List<RouteDefinition> routes = new ArrayList<>();
        routeIterable.forEach(routes::add);
        
        Optional<RouteDefinition> routeOpt = routes.stream()
                .filter(r -> r.getId().equals(routeId))
                .findFirst();
                
        if (routeOpt.isPresent()) {
            RouteDefinition route = routeOpt.get();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", route);
            
            return createJsonResponse(HttpResponseStatus.OK, result);
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "路由不存在");
            
            return createJsonResponse(HttpResponseStatus.NOT_FOUND, result);
        }
    }

    /**
     * 处理Session检查
     */
    private FullHttpResponse handleSessionCheck(ServerWebExchange exchange) {
        boolean authenticated = isAuthenticated(exchange);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("authenticated", authenticated);
        
        return createJsonResponse(HttpResponseStatus.OK, result);
    }

    /**
     * 检查是否已认证
     */
    private boolean isAuthenticated(ServerWebExchange exchange) {
        String sessionId = getSessionId(exchange);
        if (sessionId == null) {
            return false;
        }
        
        AdminSession session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }
        
        // 检查Session是否过期
        long now = System.currentTimeMillis();
        long sessionAge = now - session.getCreateTime();
        long maxAge = adminProperties.getSessionTimeout() * 60 * 1000L; // 转换为毫秒
        
        if (sessionAge > maxAge) {
            sessions.remove(sessionId);
            return false;
        }
        
        return true;
    }

    /**
     * 获取SessionID
     */
    private String getSessionId(ServerWebExchange exchange) {
        try {
            HttpHeaders headers = exchange.getRequest().requestHeaders();
            if (headers == null) {
                log.debug("Request headers is null");
                return null;
            }
            
            String cookieHeader = headers.get(HttpHeaderNames.COOKIE);
            if (cookieHeader == null) {
                log.debug("No cookie header found");
                return null;
            }
            
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && "GATEWAY_SESSION_ID".equals(parts[0])) {
                    return parts[1];
                }
            }
        } catch (Exception e) {
            log.error("Error getting session ID from request", e);
        }
        return null;
    }

    /**
     * 解析表单数据
     */
    private Map<String, String> parseFormData(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.trim().isEmpty()) {
            return params;
        }
        
        try {
            for (String pair : body.split("&")) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2) {
                    String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing form data", e);
        }
        return params;
    }

    /**
     * 获取请求体内容
     */
    private String getRequestBody(ServerWebExchange exchange) {
        // 从HttpServerRequest的body()方法获取请求体
        try {
            io.netty.buffer.ByteBuf body = exchange.getRequest().body();
            if (body != null && body.readableBytes() > 0) {
                byte[] bytes = new byte[body.readableBytes()];
                body.readBytes(bytes);
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Error reading request body", e);
        }
        return "";
    }

    /**
     * 清理过期Session
     */
    private void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        long maxAge = adminProperties.getSessionTimeout() * 60 * 1000L;
        
        sessions.entrySet().removeIf(entry -> 
            (now - entry.getValue().getCreateTime()) > maxAge
        );
    }

    /**
     * 创建JSON响应
     */
    private FullHttpResponse createJsonResponse(HttpResponseStatus status, Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            ByteBuf buffer = Unpooled.copiedBuffer(json, CharsetUtil.UTF_8);
            
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, 
                status, 
                buffer
            );
            
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS");
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization");
            
            return response;
        } catch (Exception e) {
            log.error("Error creating JSON response", e);
            return createErrorResponse("JSON serialization error");
        }
    }

    private FullHttpResponse createNotFoundResponse() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "API not found");
        return createJsonResponse(HttpResponseStatus.NOT_FOUND, result);
    }

    private FullHttpResponse createUnauthorizedResponse() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Unauthorized");
        return createJsonResponse(HttpResponseStatus.UNAUTHORIZED, result);
    }

    private FullHttpResponse createErrorResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return createJsonResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, result);
    }

    /**
     * 管理Session类
     */
    private static class AdminSession {
        private final String sessionId;
        private final String username;
        private final long createTime;

        public AdminSession(String sessionId, String username, long createTime) {
            this.sessionId = sessionId;
            this.username = username;
            this.createTime = createTime;
        }

        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
        public long getCreateTime() { return createTime; }
    }
} 