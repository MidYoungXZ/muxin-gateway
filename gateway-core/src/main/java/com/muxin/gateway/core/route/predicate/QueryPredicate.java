package com.muxin.gateway.core.route.predicate;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class QueryPredicate implements Predicate {

    public static final String TYPE = "Query";

    private final String param;
    private final String regexp;
    private final Pattern pattern;
    private final Map<String, Object> config;

    public QueryPredicate(String param) {
        this(param, null);
    }

    public QueryPredicate(String param, String regexp) {
        if (param == null || param.trim().isEmpty()) {
            throw new IllegalArgumentException("查询参数名不能为空");
        }
        this.param = param;
        this.regexp = regexp;
        this.pattern = regexp != null && !regexp.isEmpty() ? Pattern.compile(regexp) : null;
        this.config = new HashMap<>();
        this.config.put("param", param);
        if (regexp != null) {
            this.config.put("regexp", regexp);
        }
    }

    public QueryPredicate(PredicateDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("PredicateDefinition不能为空");
        }
        this.param = definition.getStringArg("param");
        this.regexp = definition.getStringArg("regexp");
        this.config = definition.getArgs() != null ? definition.getArgs() : new HashMap<>();
        
        if (param == null || param.trim().isEmpty()) {
            throw new IllegalArgumentException("查询参数名(param)不能为空");
        }
        
        this.pattern = regexp != null && !regexp.isEmpty() ? Pattern.compile(regexp) : null;
    }

    @Override
    public boolean test(HttpServerExchange exchange) {
        if (exchange == null) {
            log.warn("[QueryPredicate] exchange为空");
            return false;
        }

        String paramValue = exchange.param(param);
        if (paramValue == null) {
            log.debug("[QueryPredicate] 查询参数 {} 不存在", param);
            return Boolean.TRUE.equals(config.get("not"));
        }

        if (pattern == null) {
            log.debug("[QueryPredicate] 查询参数 {} 存在, 匹配成功", param);
            return true;
        }

        boolean matched = pattern.matcher(paramValue).matches();
        if (Boolean.TRUE.equals(config.get("not"))) matched = !matched;
        log.debug("[QueryPredicate] 查询参数 {} = {}, 正则 {} 匹配结果: {}", 
                 param, paramValue, regexp, matched);
        return matched;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "QueryPredicate-" + param;
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    public static class Factory implements PredicateFactory {

        @Override
        public Predicate createPredicate(PredicateDefinition definition) {
            return new QueryPredicate(definition);
        }

        @Override
        public String getSupportedPredicateName() {
            return TYPE;
        }

        @Override
        public void validateConfig(PredicateDefinition definition) {
            String param = definition.getStringArg("param");
            if (param == null || param.trim().isEmpty()) {
                throw new IllegalArgumentException("QueryPredicate 必须配置 param 参数");
            }
        }
    }
}
