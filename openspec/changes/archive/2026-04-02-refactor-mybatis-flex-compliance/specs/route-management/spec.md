## ADDED Requirements

### Requirement: Route 查询必须符合 MyBatis-Flex 规范
RouteServiceImpl 中的所有数据库查询操作必须遵循 `mybatis-flex-query-standard` 规格定义的标准实现方式。

#### Scenario: getServiceNames 使用 TableDef 字段
- **WHEN** 查询所有服务名称列表
- **THEN** 必须使用 `QueryWrapper.create().select(GW_SERVICE_NODE.SERVICE_NAME.distinct()).from(GW_SERVICE_NODE).where(GW_SERVICE_NODE.DELETED.eq(0))` 而非字符串拼接 SQL

#### Scenario: pageQuery 使用动态条件机制
- **WHEN** 分页查询路由列表
- **THEN** 必须使用 `.and(GW_ROUTE.ROUTE_NAME.like(dto.getRouteName(), StringUtils.hasText(dto.getRouteName())))` 而非 if 语句手动添加条件

#### Scenario: 路由关联查询使用 join + wrapper
- **WHEN** 加载路由的插件列表或断言列表
- **THEN** 必须使用 QueryWrapper 的 join 语法而非 XML SQL 或注解 SQL

### Requirement: Route 时间字段手动设置
路由相关实体（GwRoute、GwRoutePlugin、GwRoutePredicate、GwPredicate）的创建和更新操作必须手动设置 `create_time` 和 `update_time` 字段。

#### Scenario: 创建路由时设置时间
- **WHEN** 创建新路由记录
- **THEN** 必须手动设置 `route.setCreateTime(LocalDateTime.now())` 和 `route.setUpdateTime(LocalDateTime.now())`

#### Scenario: 创建路由插件关联时设置时间
- **WHEN** 创建路由插件关联记录
- **THEN** 必须手动设置 `routePlugin.setCreateTime(LocalDateTime.now())`