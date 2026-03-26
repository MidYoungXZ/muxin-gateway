package com.muxin.gateway.core.route.predicate;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class HeaderPredicate implements Predicate {

    public static final String TYPE = "Header";

    private final String header;
    private final String regexp;
    private final Pattern pattern;
    private final Map<String, Object> config;

    public HeaderPredicate(String header) {
        this(header, null);
    }

    public HeaderPredicate(String header, String regexp) {
        if (header == null || header.trim().isEmpty()) {
            throw new IllegalArgumentException("请求头名称不能为空");
        }
        this.header = header;
        this.regexp = regexp;
        this.pattern = regexp != null && !regexp.isEmpty() ? Pattern.compile(regexp) : null;
        this.config = new HashMap<>();
        this.config.put("header", header);
        if (regexp != null) {
            this.config.put("regexp", regexp);
        }
    }

    public HeaderPredicate(PredicateDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("PredicateDefinition不能为空");
        }
        this.header = definition.getStringArg("header");
        this.regexp = definition.getStringArg("regexp");
        this.config = definition.getArgs() != null ? definition.getArgs() : new HashMap<>();
        
        if (header == null || header.trim().isEmpty()) {
            throw new IllegalArgumentException("请求头名称(header)不能为空");
        }
        
        this.pattern = regexp != null && !regexp.isEmpty() ? Pattern.compile(regexp) : null;
    }

    @Override
    public boolean test(HttpServerExchange exchange) {
        if (exchange == null) {
            log.warn("[HeaderPredicate] exchange为空");
            return false;
        }

        String headerValue = exchange.header(header);
        if (headerValue == null) {
            log.debug("[HeaderPredicate] 请求头 {} 不存在", header);
            return false;
        }

        if (pattern == null) {
            log.debug("[HeaderPredicate] 请求头 {} 存在, 匹配成功", header);
            return true;
        }

        boolean matched = pattern.matcher(headerValue).matches();
        log.debug("[HeaderPredicate] 请求头 {} = {}, 正则 {} 匹配结果: {}", 
                 header, headerValue, regexp, matched);
        return matched;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "HeaderPredicate-" + header;
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    public static class Factory implements PredicateFactory {

        @Override
        public Predicate createPredicate(PredicateDefinition definition) {
            return new HeaderPredicate(definition);
        }

        @Override
        public String getSupportedPredicateName() {
            return TYPE;
        }

        @Override
        public void validateConfig(PredicateDefinition definition) {
            String header = definition.getStringArg("header");
            if (header == null || header.trim().isEmpty()) {
                throw new IllegalArgumentException("HeaderPredicate 必须配置 header 参数");
            }
        }
    }
}