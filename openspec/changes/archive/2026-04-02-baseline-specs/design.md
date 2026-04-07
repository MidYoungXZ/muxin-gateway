## 背景

Muxin Gateway 是一个已实现的系统，包含 Netty 网关引擎和 Spring Boot 管理后台。代码按"先实现后文档"模式开发，现需记录架构决策和设计选择作为后续变更的基础。

当前状态：
- gateway-core: 8 种 Predicate 实现（仅 PATH/METHOD 注册到 Factory）、6 种 Filter（全部注册）、4 种负载均衡策略、Netty 连接池
- gateway-admin: 10 个 Controller、13 个 Service、16 张数据库表、完整 RBAC
- gateway-admin-ui: Vue 3 SPA、23 个组件、动态路由、暗黑模式

## 目标 / 非目标

**Goals:**
- 记录当前实现的架构决策和设计选择
- 为 6 个能力领域建立形式化规范基线
- 标识已知问题和技术债

**Non-Goals:**
- 不改变任何现有代码行为
- 不引入新功能
- 不修改数据库 Schema

## 决策

### D1: 双模块架构 (gateway-core + gateway-admin)

**选择**: gateway-core 不依赖 Spring，纯 Netty 实现；gateway-admin 基于 Spring Boot

**理由**: 核心引擎可独立运行，不受 Spring 生命周期约束，性能更优。管理后台用 Spring Boot 快速开发。

**替代方案**: 全部基于 Spring WebFlux — 放弃，因为 Netty 直接控制更灵活。

### D2: Plugin 概念代替直接 Filter 管理

**选择**: Admin 使用 Plugin (gw_plugin) 概念，通过 `DatabaseRouteConfigProvider.mapPluginToFilters()` 转换为 core 的 Filter

**理由**: Plugin 提供 JSON Schema 配置定义，前端可动态渲染配置表单。Filter 是 core 层概念，不应暴露给用户。

**映射关系**: plugin_name → FilterDefinition.name（硬编码在 DatabaseRouteConfigProvider 中）

### D3: Predicate 双层存储

**选择**: 断言存储在 `gw_predicate` (可复用定义) + `gw_route_predicate` (关联表)，而非直接嵌入路由表

**理由**: 支持断言复用和多对多关联。但当前实现中 `saveRouteMatching()` 每次创建新断言实例，尚未利用复用能力。

### D4: SQLite 嵌入式数据库

**选择**: SQLite 作为默认数据库，MyBatis-Flex ORM

**理由**: 零部署成本，单文件数据库，适合中小规模。`SqliteInitializer` 自动初始化 schema 和 data。

**限制**: 单写者并发、无网络访问。高并发场景需迁移至 MySQL/PostgreSQL。

### D5: CSS 变量暗黑模式

**选择**: 自定义 CSS 变量体系 (`--bg-primary`, `--card-bg` 等)，不使用 Element Plus 主题变量

**理由**: 更灵活的控制，避免 Element Plus 主题系统的白块问题。`.dark` 类切换。

### D6: Schema 驱动表单渲染

**选择**: 插件配置使用 JSON Schema 定义，前端 `SchemaField.vue` 递归渲染

**理由**: 新增插件时无需编写前端表单代码，只需在后端定义 JSON Schema。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| Predicate 注册不全 (仅 PATH/METHOD) | 需在后续变更中注册剩余 6 种 Factory |
| Plugin→Filter 映射硬编码 | 新 Filter 需同时修改 core 和 main |
| SQLite 单写者限制 | 高并发场景需考虑迁移 |
| 路由更新不清理旧断言关联 | 需修复 updateRoute() |
| gateway-cloud 模块为空壳 | Nacos 集成代码散落在 admin 中 |

## Open Questions

- 是否需要将 HEADER, QUERY, COOKIE, HOST, REMOTE_ADDR, BETWEEN 注册到 PredicateFactory?
- gateway-cloud 模块是否应保留或合并到 gateway-admin?
- 断言复用能力是否需要在 Admin UI 中暴露?
