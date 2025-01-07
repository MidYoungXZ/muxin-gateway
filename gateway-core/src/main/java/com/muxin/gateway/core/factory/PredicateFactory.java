package com.muxin.gateway.core.factory;

import com.muxin.gateway.core.route.RoutePredicate;
import java.util.Map;

public interface PredicateFactory {
    RoutePredicate create(String name, Map<String, String> args);
} 