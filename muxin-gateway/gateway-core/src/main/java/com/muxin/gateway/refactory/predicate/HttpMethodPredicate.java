package com.muxin.gateway.refactory.predicate;

import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.message.http.HttpMetadata;
import com.muxin.gateway.refactory.route.UniversalRequestContext;

import java.util.*;

/**
 * HTTP方法断言实现
 *
 * @author muxin
 */
public class HttpMethodPredicate implements UniversalPredicate {
    
    private final Set<String> allowedMethods;
    private final Map<String, Object> config;
    
    public HttpMethodPredicate(String... methods) {
        this.allowedMethods = new HashSet<>();
        for (String method : methods) {
            this.allowedMethods.add(method.toUpperCase());
        }
        this.config = new HashMap<>();
        this.config.put("allowedMethods", new ArrayList<>(this.allowedMethods));
    }
    
    public HttpMethodPredicate(Collection<String> methods) {
        this.allowedMethods = new HashSet<>();
        for (String method : methods) {
            this.allowedMethods.add(method.toUpperCase());
        }
        this.config = new HashMap<>();
        this.config.put("allowedMethods", new ArrayList<>(this.allowedMethods));
    }
    
    @Override
    public boolean test(UniversalRequestContext context) {
        if (context == null || context.getInboundMessage() == null) {
            return false;
        }
        
        String method = getRequestMethod(context);
        return method != null && allowedMethods.contains(method.toUpperCase());
    }
    
    @Override
    public String getType() {
        return "METHOD";
    }
    
    @Override
    public String getName() {
        return "HTTP Method Predicate: " + String.join(", ", allowedMethods);
    }
    
    @Override
    public Set<Protocol> getSupportedProtocols() {
        Set<Protocol> protocols = new HashSet<>();
        protocols.add(new Protocol.HttpProtocol());
        return protocols;
    }
    
    @Override
    public Map<String, Object> getConfig() {
        return new HashMap<>(config);
    }
    
    private String getRequestMethod(UniversalRequestContext context) {
        Message message = context.getInboundMessage();
        if (message == null) {
            return null;
        }
        
        // 从元数据中获取方法
        if (message.getMetadata() instanceof HttpMetadata) {
            HttpMetadata metadata = (HttpMetadata) message.getMetadata();
            return metadata.getMethod();
        }
        
        // 从头部获取方法
        String requestLine = message.getHeaders().get("RequestLine", String.class);
        if (requestLine != null) {
            String[] parts = requestLine.split(" ");
            if (parts.length >= 1) {
                return parts[0];
            }
        }
        
        // 从头部直接获取
        return message.getHeaders().get("Method", String.class);
    }
} 