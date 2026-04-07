## 新增需求

### 需求：路由 CRUD 操作
系统应提供用于创建、读取、更新和删除路由配置的 REST API。所有变更操作应是事务性的。

#### 场景：创建具有唯一 ID 的路由
- **WHEN** 使用包含唯一 `routeId` 的 `RouteCreateDTO` 调用 `POST /api/routes`
- **THEN** 系统应创建 `gw_route` 记录并返回 HTTP 200 和新的路由 ID

#### 场景：拒绝重复的路由 ID
- **WHEN** 使用已存在的 `routeId` 调用 `POST /api/routes`
- **THEN** 系统应返回 400 错误，消息为"路由ID已存在"

#### 场景：软删除路由
- **WHEN** 调用 `DELETE /api/routes/{id}`
- **THEN** 系统应移除路由及其插件关联，然后触发配置刷新

### 需求：从匹配配置持久化断言
系统应将 `RouteCreateDTO` 中的 `matching` 字段转换为 `gw_predicate` + `gw_route_predicate` 记录。每种匹配类型（path、methods、hosts、headers、queries）应创建单独的断言记录。

#### 场景：路径匹配创建 PATH 断言
- **WHEN** 创建路由时传入 `matching.path.pattern: "/api/**"` 和 `matching.path.matchType: "ANT"`
- **THEN** 系统应插入 `gw_predicate`，`predicate_type: "PATH"`，`args: {"pattern":"/api/**","matchType":"ANT"}`，并通过 `gw_route_predicate` 关联

#### 场景：方法匹配创建 METHOD 断言
- **WHEN** 创建路由时传入 `matching.methods: ["GET", "POST"]`
- **THEN** 系统应插入 `gw_predicate`，`predicate_type: "METHOD"`，`args: {"methods":["GET","POST"]}`

#### 场景：可选匹配字段
- **WHEN** 未提供 `matching.hosts`、`matching.headers` 或 `matching.queries`
- **THEN** 系统应跳过为这些类型创建断言

### 需求：插件与路由关联
系统应通过 `gw_route_plugin` 记录将插件与路由关联，存储每路由的配置覆盖。

#### 场景：带配置覆盖关联插件
- **WHEN** 创建路由时传入 `plugins: [{pluginId: 1, config: {rate: 100}, enabled: true}]`
- **THEN** 系统应插入 `gw_route_plugin` 记录，`config: {"rate":100}`，关联路由和插件 ID 1

#### 场景：跳过不存在的插件
- **WHEN** 请求中的插件 ID 在 `gw_plugin` 中不存在
- **THEN** 系统应记录警告并跳过该插件关联，不导致请求失败

### 需求：变更后配置刷新
系统应在任何路由变更（创建、更新、删除、启用、禁用）后触发 gateway-core 配置刷新。

#### 场景：创建后触发刷新
- **WHEN** `RouteServiceImpl.createRoute()` 成功完成
- **THEN** 应调用 `configRefreshService.refreshRoutes()` 将变更传播到 gateway-core

### 需求：路由启用和禁用
系统应支持启用和禁用单个路由而无需删除。

#### 场景：禁用路由
- **WHEN** 调用 `POST /api/routes/{id}/disable`
- **THEN** 路由的 `enabled` 字段应设置为 `false` 并触发配置刷新

### 需求：路由更新清理旧关联
系统在路由更新期间保存新关联之前应清理旧的插件关联。

#### 场景：更新替换插件
- **WHEN** 使用新插件列表调用 `PUT /api/routes/{id}`
- **THEN** 系统应先删除该路由的所有现有 `gw_route_plugin` 记录，然后插入新关联

### 需求：分页路由列表
系统应支持分页路由列表，可选按路由名称、URI 和启用状态过滤。

#### 场景：按路由名称过滤
- **WHEN** 调用 `GET /api/routes?routeName=user`
- **THEN** 系统应返回 `route_name` 包含 "user" 的分页结果
