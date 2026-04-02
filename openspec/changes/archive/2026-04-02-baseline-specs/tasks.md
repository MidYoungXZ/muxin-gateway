## 1. 规范创建

- [x] 1.1 创建 route-engine 规范 (8 种 Predicate + 6 种 Filter + 负载均衡 + 连接池)
- [x] 1.2 创建 route-management 规范 (路由 CRUD + 断言持久化 + 插件关联)
- [x] 1.3 创建 service-management 规范 (节点 CRUD + Nacos 集成)
- [x] 1.4 创建 plugin-management 规范 (插件 CRUD + Schema + Filter 映射)
- [x] 1.5 创建 auth-rbac 规范 (JWT + RBAC + 数据作用域)
- [x] 1.6 创建 admin-ui 规范 (SPA + 动态路由 + 暗黑模式 + Schema 表单)

## 2. 已知问题修复

- [x] 2.1 在 `RouteConfigConverter.initPredicateFactories()` 中注册剩余 6 种 PredicateFactory (HEADER, QUERY, COOKIE, HOST, REMOTE_ADDR, BETWEEN)
- [x] 2.2 在 `RouteServiceImpl.updateRoute()` 中先清理旧的 `gw_route_predicate` 关联再重建
- [x] 2.3 修复 `openspec/project.md` 格式问题 (断裂的列表项、多余字符)

## 3. 验证

- [x] 3.1 运行 `mvn compile -q` 验证后端编译
- [x] 3.2 运行 `npm run build` 验证前端构建
- [x] 3.3 检查所有规范覆盖了已实现的关键功能
