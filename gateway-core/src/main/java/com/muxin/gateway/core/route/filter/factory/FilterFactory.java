package com.muxin.gateway.core.route.filter.factory;

import com.muxin.gateway.core.route.filter.PartFilter;

import java.util.Map;

public interface FilterFactory {

    PartFilter create(Map<String, String> args);

} 