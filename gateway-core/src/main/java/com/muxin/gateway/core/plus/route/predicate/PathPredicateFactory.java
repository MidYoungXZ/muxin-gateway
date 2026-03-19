package com.muxin.gateway.core.plus.route.predicate;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class PathPredicateFactory implements PredicateFactory {

    public static final String SUPPORTED_NAME = "PATH";

    @Override
    public Predicate createPredicate(PredicateDefinition definition) {
        log.debug("[PathPredicateFactory] 创建PathPredicate: {}", definition);
        return new PathPredicate(definition);
    }

    @Override
    public String getSupportedPredicateName() {
        return SUPPORTED_NAME;
    }

    @Override
    public void validateConfig(PredicateDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("PredicateDefinition不能为空");
        }

        Map<String, Object> config = definition.getConfig();
        if (config == null) {
            throw new IllegalArgumentException("PathPredicate配置不能为空");
        }

        Object patternObj = config.get("pattern");
        if (patternObj == null) {
            throw new IllegalArgumentException("PathPredicate必须配置pattern参数");
        }

        String pattern = patternObj.toString();
        if (pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("pattern参数不能为空字符串");
        }

        if (!pattern.startsWith("/")) {
            throw new IllegalArgumentException("pattern必须以'/'开头: " + pattern);
        }

        log.debug("[PathPredicateFactory] 配置验证通过: pattern={}", pattern);
    }
}
