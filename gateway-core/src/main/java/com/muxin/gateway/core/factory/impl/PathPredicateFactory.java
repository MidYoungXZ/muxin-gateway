package com.muxin.gateway.core.factory.impl;

import com.muxin.gateway.core.factory.PredicateFactory;
import com.muxin.gateway.core.route.RoutePredicate;
import com.muxin.gateway.core.route.path.AntPathMatcher;
import com.muxin.gateway.core.route.path.PathMatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.muxin.gateway.core.common.GatewayConstants.*;

/**
 * 路径断言工厂
 */
@Slf4j
public class PathPredicateFactory implements PredicateFactory {

    private final PathMatcher pathMatcher = AntPathMatcher.getDefaultInstance();

    @Override
    public RoutePredicate create(Map<String, String> args) {
        String pattern = args.get(PATTERN_ARG);
        if (pattern == null) {
            pattern = args.get(GENKEY_PREFIX + "0"); // 兼容简化配置
        }
        
        if (pattern == null) {
            throw new IllegalArgumentException(PATH_PATTERN_REQUIRED_ERROR);
        }

        final String finalPattern = pattern;
        log.debug(CREATED_PATH_PREDICATE_LOG, finalPattern);
        
        return exchange -> {
            String path = exchange.getRequest().uri();
            // 去掉查询参数
            int queryIndex = path.indexOf('?');
            if (queryIndex > 0) {
                path = path.substring(0, queryIndex);
            }
            
            boolean matches = pathMatcher.match(finalPattern, path);
            log.debug(PATH_MATCHES_LOG, path, finalPattern, matches);
            return matches;
        };
    }

    @Override
    public String name() {
        return PATH_PREDICATE_NAME;
    }
} 