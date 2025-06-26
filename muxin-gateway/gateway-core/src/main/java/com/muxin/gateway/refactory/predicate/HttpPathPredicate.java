package com.muxin.gateway.refactory.predicate;

import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.message.http.HttpMetadata;
import com.muxin.gateway.refactory.route.UniversalRequestContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * HTTP路径断言实现
 *
 * @author muxin
 */
public class HttpPathPredicate implements UniversalPredicate {
    
    private final String pathPattern;
    private final boolean isRegex;
    private final Pattern compiledPattern;
    private final Map<String, Object> config;
    
    public HttpPathPredicate(String pathPattern) {
        this(pathPattern, false);
    }
    
    public HttpPathPredicate(String pathPattern, boolean isRegex) {
        this.pathPattern = pathPattern;
        this.isRegex = isRegex;
        this.compiledPattern = isRegex ? Pattern.compile(pathPattern) : null;
        this.config = new HashMap<>();
        this.config.put("pathPattern", pathPattern);
        this.config.put("isRegex", isRegex);
    }
    
    @Override
    public boolean test(UniversalRequestContext context) {
        if (context == null || context.getInboundMessage() == null) {
            return false;
        }
        
        // 从消息元数据中获取路径信息
        String requestPath = getRequestPath(context);
        if (requestPath == null) {
            return false;
        }
        
        if (isRegex) {
            return compiledPattern.matcher(requestPath).matches();
        } else {
            return pathPattern.equals(requestPath) || 
                   (pathPattern.endsWith("/**") && 
                    requestPath.startsWith(pathPattern.substring(0, pathPattern.length() - 3)));
        }
    }
    
    @Override
    public String getType() {
        return "PATH";
    }
    
    @Override
    public String getName() {
        return "HTTP Path Predicate: " + pathPattern;
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
    
    private String getRequestPath(UniversalRequestContext context) {
        Message message = context.getInboundMessage();
        if (message == null) {
            return null;
        }
        
        // 尝试从元数据中获取路径
        if (message.getMetadata() instanceof HttpMetadata) {
            HttpMetadata metadata = (HttpMetadata) message.getMetadata();
            return metadata.getPath();
        }
        
        // 尝试从头部获取路径
        String requestLine = message.getHeaders().get("RequestLine", String.class);
        if (requestLine != null) {
            String[] parts = requestLine.split(" ");
            if (parts.length >= 2) {
                String fullPath = parts[1];
                int queryIndex = fullPath.indexOf('?');
                return queryIndex > 0 ? fullPath.substring(0, queryIndex) : fullPath;
            }
        }
        
        return null;
    }
} 