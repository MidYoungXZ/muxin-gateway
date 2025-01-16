package com.muxin.gateway.core.factory;

import com.muxin.gateway.core.filter.RouteRuleFilter;

import java.util.Map;

public interface FilterFactory {

    RouteRuleFilter create(Map<String, String> args);

} 