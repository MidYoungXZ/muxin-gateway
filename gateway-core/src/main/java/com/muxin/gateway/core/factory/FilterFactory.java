package com.muxin.gateway.core.factory;

import com.muxin.gateway.core.filter.PartFilter;

import java.util.Map;

public interface FilterFactory {

    PartFilter create(Map<String, String> args);

} 