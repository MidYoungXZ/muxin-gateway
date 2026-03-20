package com.muxin.gateway.core.plus.route.predicate;

import com.muxin.gateway.core.plus.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class MethodPredicate implements Predicate {

    public static final String TYPE = "METHOD";

    private final Set<String> allowedMethods;
    private final Map<String, Object> config;

    public MethodPredicate(String... methods) {
        if (methods == null || methods.length == 0) {
            throw new IllegalArgumentException("HTTP方法不能为空");
        }
        this.allowedMethods = new HashSet<>();
        this.config = new HashMap<>();
        for (String method : methods) {
            if (method == null || method.trim().isEmpty()) {
                throw new IllegalArgumentException("HTTP方法不能为空字符串");
            }
            this.allowedMethods.add(method.trim());
        }
        this.config.put("methods", Arrays.asList(methods));
    }

    public MethodPredicate(Collection<String> methods) {
        if (methods == null || methods.isEmpty()) {
            throw new IllegalArgumentException("HTTP方法不能为空");
        }
        this.allowedMethods = new HashSet<>();
        this.config = new HashMap<>();
        for (String method : methods) {
            if (method == null || method.trim().isEmpty()) {
                throw new IllegalArgumentException("HTTP方法不能为空字符串");
            }
            this.allowedMethods.add(method.trim());
        }
        this.config.put("methods", new ArrayList<>(methods));
    }

    public MethodPredicate(PredicateDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("PredicateDefinition不能为空");
        }
        this.allowedMethods = parseMethods(definition);
        this.config = definition.getConfig() != null ? definition.getConfig() : new HashMap<>();
    }

    private Set<String> parseMethods(PredicateDefinition definition) {
        Object methodsObj = definition.getConfigValue("methods");
        if (methodsObj == null) {
            throw new IllegalArgumentException("MethodPredicate必须配置methods参数");
        }

        Set<String> methods = new HashSet<>();
        if (methodsObj instanceof String) {
            String methodStr = ((String) methodsObj).trim();
            if (methodStr.isEmpty()) {
                throw new IllegalArgumentException("methods参数不能为空字符串");
            }
            if (methodStr.contains(",")) {
                for (String m : methodStr.split(",")) {
                    String trimmed = m.trim();
                    if (!trimmed.isEmpty()) {
                        methods.add(trimmed);
                    }
                }
            } else {
                methods.add(methodStr);
            }
        } else if (methodsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> methodList = (List<Object>) methodsObj;
            for (Object m : methodList) {
                if (m != null) {
                    String methodStr = m.toString().trim();
                    if (!methodStr.isEmpty()) {
                        methods.add(methodStr);
                    }
                }
            }
        } else if (methodsObj instanceof String[]) {
            for (String m : (String[]) methodsObj) {
                if (m != null) {
                    String trimmed = m.trim();
                    if (!trimmed.isEmpty()) {
                        methods.add(trimmed);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("methods参数类型不支持: " + methodsObj.getClass());
        }

        if (methods.isEmpty()) {
            throw new IllegalArgumentException("methods参数解析后为空");
        }

        return methods;
    }

    @Override
    public boolean test(HttpServerExchange exchange) {
        if (exchange == null) {
            log.warn("[MethodPredicate] exchange为空");
            return false;
        }

        String requestMethod = exchange.method();
        if (requestMethod == null) {
            log.warn("[MethodPredicate] 请求方法为空");
            return false;
        }

        boolean matched = allowedMethods.contains(requestMethod);
        log.debug("[MethodPredicate] 方法匹配: {} in {} = {}", requestMethod, allowedMethods, matched);
        return matched;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "MethodPredicate-" + String.join(",", allowedMethods);
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    public Set<String> getAllowedMethods() {
        return Collections.unmodifiableSet(allowedMethods);
    }

    @Override
    public String toString() {
        return String.format("MethodPredicate{methods=%s}", allowedMethods);
    }
}
