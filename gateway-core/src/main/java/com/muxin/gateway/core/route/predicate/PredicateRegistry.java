package com.muxin.gateway.core.route.predicate;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PredicateRegistry {

    private static final PredicateRegistry INSTANCE = new PredicateRegistry();
    
    private final Map<String, PredicateFactory> factories = new ConcurrentHashMap<>();

    private PredicateRegistry() {
        registerBuiltInFactories();
    }

    public static PredicateRegistry getInstance() {
        return INSTANCE;
    }

    private void registerBuiltInFactories() {
        register(new PathPredicateFactory());
        register(new MethodPredicateFactory());
        register(new HeaderPredicate.Factory());
        register(new QueryPredicate.Factory());
        register(new CookiePredicate.Factory());
        register(new HostPredicate.Factory());
        register(new RemoteAddrPredicate.Factory());
        register(new BetweenPredicate.Factory());
        
        log.info("[PredicateRegistry] 已注册 {} 个内置断言器工厂", factories.size());
    }

    public void register(PredicateFactory factory) {
        String name = factory.getSupportedPredicateName();
        factories.put(name, factory);
        log.debug("[PredicateRegistry] 注册断言器工厂: {}", name);
    }

    public PredicateFactory getFactory(String predicateName) {
        return factories.get(predicateName);
    }

    public Predicate createPredicate(PredicateDefinition definition) {
        String predicateName = definition.getName();
        PredicateFactory factory = factories.get(predicateName);
        
        if (factory == null) {
            log.warn("[PredicateRegistry] 未找到断言器工厂: {}", predicateName);
            throw new IllegalArgumentException("不支持的断言类型: " + predicateName);
        }
        
        try {
            factory.validateConfig(definition);
            return factory.createPredicate(definition);
        } catch (Exception e) {
            log.error("[PredicateRegistry] 创建断言器失败: {}", predicateName, e);
            throw new RuntimeException("创建断言器失败: " + predicateName, e);
        }
    }

    public Set<String> getSupportedPredicateNames() {
        return Collections.unmodifiableSet(factories.keySet());
    }

    public boolean isSupported(String predicateName) {
        return factories.containsKey(predicateName);
    }
}