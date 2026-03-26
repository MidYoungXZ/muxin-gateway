package com.muxin.gateway.core.route.predicate;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class CookiePredicate implements Predicate {

    public static final String TYPE = "Cookie";

    private final String name;
    private final String regexp;
    private final Pattern pattern;
    private final Map<String, Object> config;

    public CookiePredicate(String name) {
        this(name, null);
    }

    public CookiePredicate(String name, String regexp) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cookie名称不能为空");
        }
        this.name = name;
        this.regexp = regexp;
        this.pattern = regexp != null && !regexp.isEmpty() ? Pattern.compile(regexp) : null;
        this.config = new HashMap<>();
        this.config.put("name", name);
        if (regexp != null) {
            this.config.put("regexp", regexp);
        }
    }

    public CookiePredicate(PredicateDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("PredicateDefinition不能为空");
        }
        this.name = definition.getStringArg("name");
        this.regexp = definition.getStringArg("regexp");
        this.config = definition.getArgs() != null ? definition.getArgs() : new HashMap<>();
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cookie名称(name)不能为空");
        }
        
        this.pattern = regexp != null && !regexp.isEmpty() ? Pattern.compile(regexp) : null;
    }

    @Override
    public boolean test(HttpServerExchange exchange) {
        if (exchange == null) {
            log.warn("[CookiePredicate] exchange为空");
            return false;
        }

        String cookieHeader = exchange.header("Cookie");
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            log.debug("[CookiePredicate] 请求中没有Cookie");
            return false;
        }

        String cookieValue = getCookieValue(cookieHeader, name);
        if (cookieValue == null) {
            log.debug("[CookiePredicate] Cookie {} 不存在", name);
            return false;
        }

        if (pattern == null) {
            log.debug("[CookiePredicate] Cookie {} 存在, 匹配成功", name);
            return true;
        }

        boolean matched = pattern.matcher(cookieValue).matches();
        log.debug("[CookiePredicate] Cookie {} = {}, 正则 {} 匹配结果: {}", 
                 name, cookieValue, regexp, matched);
        return matched;
    }

    private String getCookieValue(String cookieHeader, String cookieName) {
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String trimmed = cookie.trim();
            int eqIndex = trimmed.indexOf('=');
            if (eqIndex > 0) {
                String currentName = trimmed.substring(0, eqIndex).trim();
                if (cookieName.equals(currentName)) {
                    return trimmed.substring(eqIndex + 1).trim();
                }
            }
        }
        return null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "CookiePredicate-" + name;
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    public static class Factory implements PredicateFactory {

        @Override
        public Predicate createPredicate(PredicateDefinition definition) {
            return new CookiePredicate(definition);
        }

        @Override
        public String getSupportedPredicateName() {
            return TYPE;
        }

        @Override
        public void validateConfig(PredicateDefinition definition) {
            String name = definition.getStringArg("name");
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("CookiePredicate 必须配置 name 参数");
            }
        }
    }
}