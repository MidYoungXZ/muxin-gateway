## Why

Muxin Gateway 已完成核心功能实现（网关引擎 + 管理后台），但缺少形式化规范文档。所有代码均为"先实现后文档"模式，需要从代码库反向生成 baseline 规范，为后续变更建立基础。

## What Changes

- 创建 6 个能力领域的初始规范，记录当前实现的完整行为契约
- 修复 `openspec/project.md` 格式问题
- 记录已知技术债和未注册 Predicate 的 bug

## Capabilities

### New Capabilities

- `route-engine`: 网关核心引擎 — 路由匹配 (8 种 Predicate)、过滤器链 (6 种 Filter)、负载均衡 (4 种策略)、Netty 连接池、请求处理流水线
- `route-management`: 路由管理 API — 路由 CRUD、断言持久化 (matching→gw_predicate)、插件关联 (gw_route_plugin)、配置刷新触发
- `service-management`: 服务节点管理 — 节点 CRUD、服务分组统计、Nacos 服务发现集成、健康检查配置
- `plugin-management`: 插件管理 — 插件定义 (含 JSON Schema)、CRUD、Plugin→Filter 映射、Schema 驱动配置表单
- `auth-rbac`: 认证授权 — Sa-Token JWT 认证、RBAC (User→Role→Menu)、5 级数据作用域、按钮级权限
- `admin-ui`: 前端应用 — Vue 3 SPA、动态路由加载、暗黑模式 (CSS 变量体系)、递归 Schema 表单、路由创建四步向导

### Modified Capabilities

(无 — 全部为新增 baseline 规范)

## Impact

- 纯文档变更，无代码影响
- 为后续所有变更提供规范基线
- 揭示 Predicate 注册不全等已存在问题
