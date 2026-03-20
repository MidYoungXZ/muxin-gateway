package com.muxin.gateway.core.plus.route.predicate;

import com.muxin.gateway.core.plus.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class PathPredicate implements Predicate {

    public static final String TYPE = "PATH";

    private final String pattern;
    private final Map<String, Object> config;
    private final Pattern regexPattern;
    private final boolean endsWithDoubleStar;
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
        this.regexPattern = convertAntPatternToRegex(pattern);
        this.endsWithDoubleStar = pattern.endsWith("/**");
    }

    public PathPredicate(PredicateDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("PredicateDefinition不能为空");
        }
        this.pattern = definition.getStringConfig("pattern");
        this.config = definition.getConfig();
        this.stripPrefixCount = definition.getIntConfig("strip-prefix", 0);
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("路径模式(pattern)不能为空");
        }
        this.regexPattern = convertAntPatternToRegex(pattern);
        this.endsWithDoubleStar = pattern.endsWith("/**");
    }

    private Pattern convertAntPatternToRegex(String antPattern) {
        String regex = antPattern
                .replace(".", "\\.")
                .replace("**/", "[\\\\w\\\\W]*?/")
                .replace("**", "[\\\\w\\\\W]*?")
                .replace("*", "[^/]*")
                .replace("?", "[^/]");
        if (!regex.startsWith("^")) {
            regex = "^" + regex;
        }
        if (!regex.endsWith("$") && !regex.endsWith("[\\\\w\\\\W]*?")) {
            regex = regex + "$";
        }
        return Pattern.compile(regex);
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

        boolean matched = regexPattern.matcher(path).matches();
        
        if (!matched && endsWithDoubleStar) {
            String prefixPattern = pattern.substring(0, pattern.length() - 3);
            matched = path.equals(prefixPattern) || path.startsWith(prefixPattern + "/");
        }

        log.debug("[PathPredicate] 路径匹配: {} matches {} = {}", path, pattern, matched);
        return matched;
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
