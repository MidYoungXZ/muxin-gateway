package com.muxin.gateway.core.route.filter.factory;

import com.muxin.gateway.core.route.filter.PartFilter;

import java.util.Map;

public /**
 * 接口 - 工厂类
 * 
 * 定义标准契约，实现类必须遵循此接口的规范
 * 
 * @author muxin
 * @since 1.0.0
 */

interface FilterFactory {

    PartFilter create(Map<String, String> args);

} 