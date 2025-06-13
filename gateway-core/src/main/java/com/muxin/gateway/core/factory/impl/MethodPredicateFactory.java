package com.muxin.gateway.core.factory.impl;

import com.muxin.gateway.core.factory.PredicateFactory;
import com.muxin.gateway.core.route.RoutePredicate;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.muxin.gateway.core.common.GatewayConstants.*;

/**
 * HTTP方法断言工厂
 */
@Slf4j
public class MethodPredicateFactory implements PredicateFactory {

    @Override
    public RoutePredicate create(Map<String, String> args) {
        String methods = args.get(METHODS_ARG);
        if (methods == null) {
            methods = args.get(METHOD_ARG); // 兼容单数形式
        }
        if (methods == null) {
            methods = args.get(GENKEY_PREFIX + "0"); // 兼容简化配置
        }
        
        if (methods == null) {
            throw new IllegalArgumentException(HTTP_METHODS_REQUIRED_ERROR);
        }

        Set<HttpMethod> allowedMethods = Arrays.stream(methods.split(","))
                .map(String::trim)
                .map(HttpMethod::valueOf)
                .collect(Collectors.toSet());

        log.debug(CREATED_METHOD_PREDICATE_LOG, allowedMethods);
        
        return exchange -> {
            HttpMethod requestMethod = exchange.getRequest().method();
            boolean matches = allowedMethods.contains(requestMethod);
            log.debug(METHOD_MATCHES_LOG, requestMethod, allowedMethods, matches);
            return matches;
        };
    }

    @Override
    public String name() {
        return METHOD_PREDICATE_NAME;
    }
} 