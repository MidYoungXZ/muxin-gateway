## 修改需求

### 需求：路由 CRUD 操作
系统应提供用于创建、读取、更新和删除路由配置的 REST API。所有变更操作应是事务性的。`GET /api/routes/{id}` 端点应返回包含关联断言和插件的完整路由详情。权限检查应使用与数据库种子数据一致的一致权限字符串。

#### 场景：创建具有唯一 ID 的路由
- **WHEN** 使用包含唯一 `routeId` 的 `RouteCreateDTO` 调用 `POST /api/routes`
- **THEN** 系统应创建 `gw_route` 记录并返回 HTTP 200 和新的路由 ID

#### 场景：拒绝重复的路由 ID
- **WHEN** 使用已存在的 `routeId` 调用 `POST /api/routes`
- **THEN** 系统应返回 400 错误，消息为"路由ID已存在"

#### 场景：软删除路由
- **WHEN** 调用 `DELETE /api/routes/{id}`
- **THEN** 系统应移除路由及其插件关联，然后触发配置刷新

#### 场景：获取包含断言的路由详情
- **WHEN** 调用 `GET /api/routes/{id}` 且用户具有 `route:view` 权限
- **THEN** 系统应返回 `RouteVO`，包含 `predicates` 列表（每项含 `predicateType` 和 `args`）、`plugins` 列表和所有路由字段

#### 场景：权限字符串与数据库匹配
- **WHEN** 控制器方法通过 `@SaCheckPermission` 需要权限
- **THEN** 权限字符串应与数据库种子数据中的 `sys_menu.perms` 值完全匹配

### 需求：路由更新清理旧关联
系统在路由更新期间保存新关联之前，应清理旧的插件关联和断言关联。如果提供了 `matching`，则应创建新断言。

#### 场景：更新替换插件
- **WHEN** 使用新插件列表调用 `PUT /api/routes/{id}`
- **THEN** 系统应先删除该路由的所有现有 `gw_route_plugin` 记录，然后插入新关联

#### 场景：更新替换断言
- **WHEN** 使用 `matching` 数据调用 `PUT /api/routes/{id}`
- **THEN** 系统应先删除该路由的所有现有 `gw_route_predicate` 和关联的 `gw_predicate` 记录，然后通过 `saveRouteMatching()` 插入新断言记录

## 新增需求

### 需求：路由详情包含断言
`GET /api/routes/{id}` 返回的 `RouteVO` 应包含 `predicates` 字段，包含所有关联断言的类型和参数，使前端能够用现有匹配规则填充编辑表单。

#### 场景：路由详情加载断言
- **WHEN** 为具有 3 个断言（PATH、METHOD、HOST）的路由调用 `RouteServiceImpl.getRouteDetail()`
- **THEN** 返回的 `RouteVO.predicates` 应包含 3 条记录，每条含 `predicateType` 和 `args` 字段

#### 场景：路由无断言
- **WHEN** 为没有断言的路由调用 `RouteServiceImpl.getRouteDetail()`
- **THEN** 返回的 `RouteVO.predicates` 应为空列表（非 null）

### 需求：负载均衡策略同步到 gateway-core
`DatabaseRouteConfigProvider` 应从 `gw_route` 读取 `load_balance_strategy` 并设置到 `RouteDefinition.loadBalance` 字段，使配置的策略在运行时生效。

#### 场景：应用自定义负载均衡策略
- **WHEN** 路由在数据库中有 `load_balance_strategy: "WEIGHTED"`
- **THEN** `DatabaseRouteConfigProvider.convertToRouteDefinition()` 应创建 `LoadBalanceDefinition`，策略为 `"WEIGHTED"`，并设置到 `RouteDefinition`

#### 场景：未设置时使用默认负载均衡
- **WHEN** 路由的 `load_balance_strategy` 为 null 或空
- **THEN** `RouteDefinition.loadBalance` 应为 null，gateway-core 应使用默认的 "ROUND_ROBIN"

### 需求：路径重写和超时自动转换为插件
`RouteServiceImpl` 应自动将路由 DTO 中的 `pathRewrite` 和 `timeouts` 分别转换为 `request-rewrite` 和 `timeout` 插件条目，确保这些配置被持久化并同步到 gateway-core。

#### 场景：路径重写创建 request-rewrite 插件
- **WHEN** 调用 `createRoute()` 时传入 `pathRewrite: {from: "/api/(.*)", to: "/$1"}`
- **THEN** 系统应创建一个 `gw_route_plugin` 条目，插件名称为 `request-rewrite`，配置为 `{"pathFrom": "/api/(.*)", "pathTo": "/$1", "rewriteType": "REGEX"}`

#### 场景：超时创建 timeout 插件
- **WHEN** 调用 `createRoute()` 时传入 `timeouts: {connect: 5000, response: 30000}`
- **THEN** 系统应创建一个 `gw_route_plugin` 条目，插件名称为 `timeout`，配置为 `{"connectTimeout": 5000, "responseTimeout": 30000}`

#### 场景：无路径重写或超时
- **WHEN** 调用 `createRoute()` 时未传入 `pathRewrite` 或 `timeouts`
- **THEN** 不应创建额外的插件条目

### 需求：权限种子数据一致性
数据库种子数据（`data.sql`）应使用与所有控制器中 `@SaCheckPermission` 注解完全匹配的权限字符串。现有数据库应提供迁移脚本以更新现有权限记录。

#### 场景：权限字符串与控制器匹配
- **WHEN** 应用使用种子数据初始化
- **THEN** `sys_menu.perms` 值应为 `route:view`、`route:plugin:view`、`system:user:view`、`system:role:view`——与控制器注解完全匹配

#### 场景：现有数据库迁移
- **WHEN** 现有数据库具有旧的权限字符串（如 `route:detail` 等）
- **THEN** 应提供迁移 SQL 脚本以将这些值更新为新约定
