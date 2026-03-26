package com.muxin.gateway.core.route.predicate;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class MethodPredicateFactory implements PredicateFactory {

    public static final String SUPPORTED_NAME = "METHOD";

    private static final List<String> VALID_METHODS = List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE", "CONNECT"
    );

    @Override
    public Predicate createPredicate(PredicateDefinition definition) {
        log.debug("[MethodPredicateFactory] 创建MethodPredicate: {}", definition);
        return new MethodPredicate(definition);
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

        Map<String, Object> args = definition.getArgs();
        if (args == null) {
            throw new IllegalArgumentException("MethodPredicate配置不能为空");
        }

        Object methodsObj = args.get("methods");
        if (methodsObj == null) {
            throw new IllegalArgumentException("MethodPredicate必须配置methods参数");
        }

        if (methodsObj instanceof String) {
            String methodStr = ((String) methodsObj).trim();
            if (methodStr.isEmpty()) {
                throw new IllegalArgumentException("methods参数不能为空字符串");
            }
            validateMethodString(methodStr);
        } else if (methodsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> methodList = (List<Object>) methodsObj;
            if (methodList.isEmpty()) {
                throw new IllegalArgumentException("methods列表不能为空");
            }
            for (Object m : methodList) {
                if (m == null) {
                    throw new IllegalArgumentException("methods列表中不能包含null值");
                }
                String method = m.toString().trim();
                validateSingleMethod(method);
            }
        } else if (methodsObj instanceof String[]) {
            String[] methodArray = (String[]) methodsObj;
            if (methodArray.length == 0) {
                throw new IllegalArgumentException("methods数组不能为空");
            }
            for (String m : methodArray) {
                if (m == null || m.trim().isEmpty()) {
                    throw new IllegalArgumentException("methods数组中不能包含null或空字符串");
                }
                validateSingleMethod(m.trim());
            }
        } else {
            throw new IllegalArgumentException("methods参数类型不支持: " + methodsObj.getClass() +
                    "，支持: String, List, String[]");
        }

        log.debug("[MethodPredicateFactory] 配置验证通过");
    }

    private void validateMethodString(String methodStr) {
        if (methodStr.contains(",")) {
            String[] parts = methodStr.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    validateSingleMethod(trimmed);
                }
            }
        } else {
            validateSingleMethod(methodStr);
        }
    }

    private void validateSingleMethod(String method) {
        if (!VALID_METHODS.contains(method)) {
            throw new IllegalArgumentException("无效的HTTP方法: " + method +
                    "，有效方法: " + VALID_METHODS);
        }
    }
}
