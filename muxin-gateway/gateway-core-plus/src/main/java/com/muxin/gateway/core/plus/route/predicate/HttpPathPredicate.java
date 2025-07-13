package com.muxin.gateway.core.plus.route.predicate;

import com.muxin.gateway.core.plus.protocol.message.Message;
import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.protocol.message.ProtocolEnum;
import com.muxin.gateway.core.plus.route.RequestContext;

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
public class HttpPathPredicate implements Predicate {
    
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
    public boolean test(RequestContext context) {
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
        protocols.add(ProtocolEnum.HTTP);
        return protocols;
    }
    
    @Override
    public Map<String, Object> getConfig() {
        return new HashMap<>(config);
    }
    
    private String getRequestPath(RequestContext context) {
        Message message = context.getInboundMessage();
        if (message == null) {
            return null;
        }
        return message.url().getPath();
    }
} 