package com.muxin.gateway.core.plus.route.predicate;

import com.muxin.gateway.core.plus.protocol.message.ProtocolEnum;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.protocol.message.Message;
import com.muxin.gateway.core.plus.protocol.message.Protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * HTTP头部断言实现
 *
 * @author muxin
 */
public class HttpHeaderPredicate implements Predicate {
    
    public enum MatchType {
        EXACT, REGEX, CONTAINS, STARTS_WITH, ENDS_WITH
    }
    
    private final String headerName;
    private final String expectedValue;
    private final MatchType matchType;
    private final Pattern compiledPattern;
    private final Map<String, Object> config;
    
    public HttpHeaderPredicate(String headerName, String expectedValue) {
        this(headerName, expectedValue, "EXACT");
    }
    
    public HttpHeaderPredicate(String headerName, String expectedValue, String matchType) {
        this.headerName = headerName;
        this.expectedValue = expectedValue;
        this.matchType = MatchType.valueOf(matchType.toUpperCase());
        this.compiledPattern = this.matchType == MatchType.REGEX ? 
            Pattern.compile(expectedValue) : null;
        
        this.config = new HashMap<>();
        this.config.put("name", headerName);
        this.config.put("value", expectedValue);
        this.config.put("matchType", this.matchType.name());
    }
    
    @Override
    public boolean test(RequestContext context) {
        if (context == null || context.getInboundMessage() == null) {
            return false;
        }
        
        Message message = context.getInboundMessage();
        if (message.getHeaders() == null) {
            return false;
        }
        
        String actualValue = message.getHeaders().get(headerName, String.class);
        if (actualValue == null) {
            return false;
        }
        
        return matchValue(actualValue, expectedValue, matchType);
    }
    
    @Override
    public String getType() {
        return "HEADER";
    }
    
    @Override
    public String getName() {
        return "HTTP Header Predicate: " + headerName + " " + matchType + " " + expectedValue;
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
    
    /**
     * 根据匹配类型匹配值
     */
    private boolean matchValue(String actualValue, String expectedValue, MatchType matchType) {
        switch (matchType) {
            case EXACT:
                return expectedValue.equals(actualValue);
            case REGEX:
                return compiledPattern.matcher(actualValue).matches();
            case CONTAINS:
                return actualValue.contains(expectedValue);
            case STARTS_WITH:
                return actualValue.startsWith(expectedValue);
            case ENDS_WITH:
                return actualValue.endsWith(expectedValue);
            default:
                return false;
        }
    }
} 