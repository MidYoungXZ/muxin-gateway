package com.muxin.gateway.core.route.predicate;

import cn.hutool.core.text.AntPathMatcher;
import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
public class PathPredicate implements Predicate {

    public static final String TYPE = "PATH";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final String pattern;
    private final Map<String, Object> config;
    private final int stripPrefixCount;

    public PathPredicate(String pattern) {
        this(pattern, 0);
    }

    public PathPredicate(String pattern, int stripPrefixCount) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("路径模式不能为空");
        }
        this.pattern = pattern;
        this.stripPrefixCount = stripPrefixCount;
        this.config = new HashMap<>();
        this.config.put("pattern", pattern);
        this.config.put("strip-prefix", stripPrefixCount);
    }

    public PathPredicate(PredicateDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("PredicateDefinition不能为空");
        }
        
        Object patternObj = definition.getArgs().get("pattern");
        if (patternObj == null) {
            patternObj = definition.getArgs().get("patterns");
        }
        if (patternObj instanceof java.util.List) {
            java.util.List<?> patterns = (java.util.List<?>) patternObj;
            if (!patterns.isEmpty()) {
                this.pattern = patterns.get(0).toString();
            } else {
                this.pattern = null;
            }
        } else if (patternObj != null) {
            this.pattern = patternObj.toString();
        } else {
            this.pattern = null;
        }
        
        this.config = definition.getArgs();
        this.stripPrefixCount = definition.getIntArg("strip-prefix", 0);
        if (this.pattern == null || this.pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("路径模式不能为空");
        }
    }

    @Override
    public boolean test(HttpServerExchange exchange) {
        if (exchange == null) {
            log.warn("[PathPredicate] exchange为空");
            return false;
        }

        String path = exchange.fullPath();
        if (path == null) {
            log.warn("[PathPredicate] 请求路径为空");
            return false;
        }

        String effectivePath = path;
        String effectivePattern = pattern;
        if (getBoolean("ignoreCase")) {
            effectivePath = path.toLowerCase(Locale.ROOT);
            effectivePattern = pattern.toLowerCase(Locale.ROOT);
        }
        String matchType = String.valueOf(config.getOrDefault("matchType", "ANT"));
        boolean matched = switch (matchType.toUpperCase(Locale.ROOT)) {
            case "EXACT" -> effectivePattern.equals(effectivePath);
            case "REGEX" -> Pattern.matches(effectivePattern, effectivePath);
            default -> PATH_MATCHER.match(effectivePattern, effectivePath);
        };
        
        log.debug("[PathPredicate] 路径匹配: {} matches {} = {}", path, pattern, matched);
        return matched;
    }

    private boolean getBoolean(String key) {
        Object value = config.get(key);
        return value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value));
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "PathPredicate-" + pattern;
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    public String getPattern() {
        return pattern;
    }

    public int getStripPrefixCount() {
        return stripPrefixCount;
    }

    public String stripPrefix(String path) {
        if (stripPrefixCount <= 0 || path == null) {
            return path;
        }
        String[] segments = path.split("/");
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            count++;
            if (count > stripPrefixCount) {
                result.append("/").append(segment);
            }
        }
        return result.length() == 0 ? "/" : result.toString();
    }

    @Override
    public String toString() {
        return String.format("PathPredicate{pattern='%s', stripPrefix=%d}", pattern, stripPrefixCount);
    }
}
