## 目的
gateway-admin 中的插件管理，提供插件定义的 CRUD、插件到过滤器的映射、每个路由的配置覆盖和类型分类。

## 需求

### 需求：带 JSON Schema 的插件定义
系统应在 `gw_plugin` 中存储插件定义，`schema` 字段包含描述插件配置结构的 JSON Schema。

#### 场景：带 schema 定义的插件
- **WHEN** 创建插件时设置 `schema: {"type":"object","properties":{"rate":{"type":"integer","minimum":1}}}`
- **THEN** 系统应存储 schema 并用于验证插件配置

### 需求：插件 CRUD 操作
系统应提供创建、读取、更新和删除插件定义的 REST API。

#### 场景：创建插件
- **WHEN** `POST /api/plugins` 使用 `plugin_name`、`plugin_type`（AUTH 或 FILTER）和可选的 `schema` 调用
- **THEN** 系统应创建 `gw_plugin` 记录并返回新插件 ID

#### 场景：插件名称必须唯一
- **WHEN** 创建插件时使用已存在的 `plugin_name`
- **THEN** 数据库唯一约束应拒绝插入

#### 场景：按类型过滤插件
- **WHEN** 调用 `GET /api/plugins?type=FILTER`
- **THEN** 系统应仅返回 `plugin_type: "FILTER"` 的插件

### 需求：插件到过滤器映射
系统应通过 `DatabaseRouteConfigProvider.mapPluginToFilters()` 将 admin 插件名映射到 gateway-core 过滤器名。映射应为：
- `rate-limit` → `RequestRateLimiter`
- `circuit-breaker` → `CircuitBreaker`
- `cors` → `CorsFilter`
- `timeout` → `TimeoutFilter`
- `request-rewrite` → `RequestRewriteFilter`
- `response-rewrite` → `ResponseRewriteFilter`

#### 场景：限流插件映射到过滤器
- **WHEN** 路由拥有插件 `rate-limit`，配置为 `{"rate": 100, "burst": 200}`
- **THEN** `DatabaseRouteConfigProvider` 应生成名为 `RequestRateLimiter` 的 `FilterDefinition`，参数为 `{"replenishRate": 100, "burstCapacity": 200}`

#### 场景：跳过非 FILTER 插件
- **WHEN** 插件的 `plugin_type: "AUTH"`
- **THEN** `mapPluginToFilters()` 应返回空列表并记录 debug 日志

#### 场景：跳过未知插件名
- **WHEN** 插件的 `plugin_name: "custom-plugin"` 没有过滤器映射
- **THEN** `mapPluginToFilters()` 应记录警告并返回空列表

### 需求：每个路由的插件配置覆盖
系统应允许通过 `gw_route_plugin.config` 覆盖每个路由的插件默认配置。

#### 场景：覆盖默认配置
- **WHEN** 路由关联插件 `cors`，配置为 `config: {"allowOrigins": "https://specific.com"}`
- **THEN** 有效配置应合并默认配置和覆盖配置，覆盖配置优先

### 需求：插件类型分类
系统应将插件分为两类：AUTH（认证阶段，优先级 8000-7000）和 FILTER（请求处理阶段，优先级 6000-1000）。

#### 场景：插件阶段信息
- **WHEN** 插件定义为 `plugin_type: "AUTH"` 和 `phase: "AUTH"`
- **THEN** 系统应存储两个字段，并使用 `plugin_type` 进行过滤器映射决策

### 需求：默认优先级继承
系统应允许路由继承插件默认优先级或通过 `priority_override` 覆盖。

#### 场景：使用默认优先级
- **WHEN** 路由-插件关联没有 `priority_override`
- **THEN** 有效优先级应为插件的 `default_priority`（例如 5000）

#### 场景：覆盖优先级
- **WHEN** 路由-插件关联有 `priority_override: 6000`
- **THEN** 有效优先级应为 6000