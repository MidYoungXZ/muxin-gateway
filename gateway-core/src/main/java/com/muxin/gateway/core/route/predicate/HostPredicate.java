package com.muxin.gateway.core.route.predicate;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class HostPredicate implements Predicate {

    public static final String TYPE = "Host";

    private final List<String> patterns;
    private final List<Pattern> compiledPatterns;
    private final Map<String, Object> config;

    public HostPredicate(String... patterns) {
        this(Arrays.asList(patterns));
    }

    public HostPredicate(List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            throw new IllegalArgumentException("主机模式不能为空");
        }
        this.patterns = new ArrayList<>(patterns);
        this.compiledPatterns = new ArrayList<>();
        this.config = new HashMap<>();
        this.config.put("patterns", this.patterns);
        
        for (String pattern : patterns) {
            compiledPatterns.add(convertToRegex(pattern));
        }
    }

    public HostPredicate(PredicateDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("PredicateDefinition不能为空");
        }
        this.patterns = parsePatterns(definition);
        this.compiledPatterns = new ArrayList<>();
        this.config = definition.getConfig() != null ? definition.getConfig() : new HashMap<>();
        
        for (String pattern : patterns) {
            compiledPatterns.add(convertToRegex(pattern));
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parsePatterns(PredicateDefinition definition) {
        Object patternsObj = definition.getConfigValue("patterns");
        if (patternsObj == null) {
            throw new IllegalArgumentException("HostPredicate必须配置patterns参数");
        }

        List<String> result = new ArrayList<>();
        if (patternsObj instanceof String) {
            String str = ((String) patternsObj).trim();
            if (str.contains(",")) {
                for (String p : str.split(",")) {
                    String trimmed = p.trim();
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
            } else {
                result.add(str);
            }
        } else if (patternsObj instanceof List) {
            for (Object item : (List<?>) patternsObj) {
                if (item != null) {
                    result.add(item.toString().trim());
                }
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("patterns参数解析后为空");
        }

        return result;
    }

    private Pattern convertToRegex(String pattern) {
        String regex = pattern;
        
        if (regex.startsWith("*.")) {
            regex = ".+\\." + Pattern.quote(regex.substring(2));
        } else if (regex.startsWith("*")) {
            regex = ".+" + Pattern.quote(regex.substring(1));
        } else if (regex.contains("*")) {
            regex = regex.replace("*", "[^.]*");
            regex = Pattern.quote(regex).replace("\\Q", "").replace("\\E", "");
        } else {
            regex = Pattern.quote(regex);
        }
        
        return Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean test(HttpServerExchange exchange) {
        if (exchange == null) {
            log.warn("[HostPredicate] exchange为空");
            return false;
        }

        String host = exchange.header("Host");
        if (host == null || host.isEmpty()) {
            log.debug("[HostPredicate] 请求头Host不存在");
            return false;
        }

        if (host.contains(":")) {
            host = host.substring(0, host.indexOf(":"));
        }

        for (int i = 0; i < compiledPatterns.size(); i++) {
            Pattern compiledPattern = compiledPatterns.get(i);
            if (compiledPattern.matcher(host).matches()) {
                log.debug("[HostPredicate] 主机 {} 匹配模式 {}", host, patterns.get(i));
                return true;
            }
        }

        log.debug("[HostPredicate] 主机 {} 不匹配任何模式 {}", host, patterns);
        return false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "HostPredicate-" + String.join(",", patterns);
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    public static class Factory implements PredicateFactory {

        @Override
        public Predicate createPredicate(PredicateDefinition definition) {
            return new HostPredicate(definition);
        }

        @Override
        public String getSupportedPredicateName() {
            return TYPE;
        }

        @Override
        public void validateConfig(PredicateDefinition definition) {
            Object patterns = definition.getConfigValue("patterns");
            if (patterns == null) {
                throw new IllegalArgumentException("HostPredicate 必须配置 patterns 参数");
            }
        }
    }
}