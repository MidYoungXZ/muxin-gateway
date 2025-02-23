package com.muxin.gateway.core.route;

import com.muxin.gateway.core.common.CrudRepository;

/**
 * 定义了一个路由定义仓库接口，继承自 {@link CrudRepository}。
 * 该接口用于对路由定义进行增删改查操作。
 *
 * @author Administrator
 * @date 2024/11/21 11:10
 */
public interface RouteDefinitionRepository extends CrudRepository<RouteDefinition, String> {

}
