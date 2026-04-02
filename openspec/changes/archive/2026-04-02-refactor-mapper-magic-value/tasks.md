## 1. Bug 修复

- [x] 1.1 修复 DatabaseRouteConfigProvider:180 — 删除无效的 `map.get("default_config")` 访问或修改 SQL 返回该字段
- [x] 1.2 验证 ServiceNodeServiceImpl:376-379 — 确认 SQLite 返回的 key 名称，确保改用 VO 后正确映射

## 2. 新增 VO 类

- [x] 2.1 创建 `RoutePluginDetailVO` — 包含 routePluginId, pluginId, pluginName, pluginType, config, priorityOverride, defaultPriority, enabled, phase
- [x] 2.2 创建 `RoutePredicateDetailVO` — 包含 id, predicateName, predicateType, args, sortOrder
- [x] 2.3 创建 `RouteSimpleVO` — 包含 id, routeId, routeName, enabled
- [x] 2.4 创建 `ServiceStatsVO` — 包含 serviceName, totalNodes, healthyNodes, unhealthyNodes, enabledNodes, disabledNodes, maintenanceNodes

## 3. 新增常量类

- [x] 3.1 创建 `PredicateConfigKeys` — 定义 pattern, matchType, ignoreCase, methods, hosts, headers, queries
- [x] 3.2 创建 `PluginConfigKeys` — 定义 pathRegex, pathReplacement, connectTimeout, responseTimeout, rate, burst, failureThreshold, timeout, successThreshold, allowOrigins, allowMethods, allowHeaders, allowCredentials, maxAge, headersToAdd, headersToRemove, bodyRegex, bodyReplacement

## 4. Mapper 层重构 — RoutePluginMapper

- [x] 4.1 删除 `@Delete deleteByRouteId` 注解，改用 BaseMapper deleteByMap
- [x] 4.2 删除 `@Select findPluginsByRouteId` 注解，移到 Service 层实现
- [x] 4.3 修改 Service 层实现：先查 gw_route_plugin 列表，再批量查 gw_plugin，stream 组装 RoutePluginDetailVO

## 5. Mapper 层重构 — RoutePredicateMapper

- [x] 5.1 删除 `@Select countByPredicateId` 注解，改用 QueryWrapper.count()
- [x] 5.2 删除 `@Delete deleteByRouteId` 注解，改用 BaseMapper deleteByMap
- [x] 5.3 删除 `@Select findRoutesByPredicateId` 注解，移到 Service 层实现
- [x] 5.4 删除 `@Select findPredicatesByRouteId` 注解，移到 Service 层实现
- [x] 5.5 修改 Service 层实现：stream 组装返回 VO

## 6. Mapper 层重构 — PluginMapper

- [x] 6.1 删除 `@Select findByPluginName` 注解，改用 QueryWrapper.selectOne()
- [x] 6.2 删除 `@Select findByType` 注解，改用 QueryWrapper.selectList()
- [x] 6.3 删除 `@Select findAllEnabled` 注解，改用 QueryWrapper.selectList()

## 7. Mapper 层重构 — PredicateMapper

- [x] 7.1 删除 `@Select findByPredicateName` 注解，改用 QueryWrapper.selectOne()

## 8. Mapper 层重构 — RouteMapper

- [x] 8.1 删除 `@Select findAllServiceNames` 注解，移到 Service 层用 QueryWrapper 实现
- [x] 8.2 删除 `@Select findRoutesByServiceName` 注解，移到 Service 层实现

## 9. Mapper 层重构 — PermissionMapper

- [x] 9.1 保留 `@Select selectPermissionsByUserId` 注解 — 多表关联查询（5表），权限系统核心功能，返回 `List<String>` 无魔法值，属于特殊情况
- [x] 9.2 保留 `@Select selectRolesByUserId` 注解 — 多表关联查询（3表），权限系统核心功能，返回 `List<String>` 无魔法值，属于特殊情况

## 10. Mapper 层重构 — ServiceNodeMapper

- [x] 10.1 删除 `@Select selectServiceStats` 注解，移到 Service 层用 QueryWrapper.groupBy 实现
- [x] 10.2 删除 `@Select selectServiceNames` 注解，移到 Service 层用 QueryWrapper 实现

## 11. Service 层优化 — RouteServiceImpl

- [x] 11.1 修改 `loadPlugins()` 方法，使用 RoutePluginDetailVO 替代 Map 访问
- [x] 11.2 修改 `loadPredicates()` 方法，使用 RoutePredicateDetailVO 替代 Map 访问
- [x] 11.3 修改 `saveRouteMatching()` 方法，使用 PredicateConfigKeys 常量
- [x] 11.4 修改 `savePathRewritePlugin()` 和 `saveTimeoutPlugin()`，使用 PluginConfigKeys 常量

## 12. Service 层优化 — ServiceNodeServiceImpl

- [x] 12.1 修改 `getServiceStats()` 方法，使用 ServiceStatsVO 替代 Map
- [x] 12.2 修改 `getRoutesByServiceName()` 方法，使用 RouteSimpleVO 替代 Map

## 13. Provider 层优化 — DatabaseRouteConfigProvider

- [x] 13.1 修改 `loadPredicates()` 方法，使用 RoutePredicateDetailVO 或常量访问
- [x] 13.2 修改 `loadFiltersFromPlugins()` 方法，使用 RoutePluginDetailVO 或常量访问
- [x] 13.3 修改插件配置解析方法（createRateLimitFilter 等），使用 PluginConfigKeys 常量

## 14. 验证

- [x] 14.1 `mvn compile` 编译通过
- [x] 14.2 `npm run build` 前端构建通过