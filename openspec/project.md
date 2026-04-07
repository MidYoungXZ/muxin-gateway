# Muxin Gateway

> Lightweight API Gateway - 基于 Netty 的高性能网关，带完整的 Admin 管理后台

## 1. 项目概述

Muxin Gateway 是一个高性能 API 网关系统，采用 Netty 作为核心转发引擎，提供路由匹配、请求过滤、负载均衡、服务发现等网关核心能力，同时配备 Spring Boot 驱动的管理后台，支持路由/服务/插件的可视化配置、RBAC 权限管理和操作审计。

### 1.1 技术栈

| 层级 | 技术选型 | 版本 |
|------|---------|------|
| 网关引擎 | Netty (HTTP Server) | 4.1.114+ |
| 管理后台 | Spring Boot | 3.3.5 |
| ORM | MyBatis-Flex | 1.9.8 |
| 数据库 | SQLite | 3.45.3 |
| 认证 | Sa-Token + JWT | 1.37.0 |
| 前端 | Vue 3 + Element Plus | 5.0.x |
| 构建 | Maven + JDK 17 | - |
| 服务发现 | Nacos | 2.3.2 |

### 1.2 核心约束

- 网关引擎 (gateway-core) 不依赖 Spring，可独立运行
- 管理后台与网关引擎通过 Provider 接口解耦（支持热刷新）
- 数据库默认 SQLite（嵌入式），支持扩展至 MySQL/PostgreSQL
- 前端 SPA 架构，暗黑模式（CSS 变量体系）

## 2. 模块架构

| 模块 | 职责 | 依赖 Spring |
|------|------|-----------|
| `gateway-bom` | 依赖版本管理 (BOM) | 否 |
| `gateway-core` | 网关引擎 (Netty, 路由匹配, 过滤器, 负载均衡) | 否 |
| `gateway-admin` | 管理后台 (Spring Boot, MyBatis-Flex, RBAC) | 是 |
| `gateway-main` | 启动器 (组合 admin + core, Provider 桥接) | 是 |
| `gateway-cloud` | 云服务发现 (当前为空壳) | 是 |
| `gateway-admin-ui` | 前端 (Vue 3 + Element Plus) | - |

## 3. 核心能力

### 3.1 路由断言 (Predicate) - 8种

| 断言类型 | 配置参数 | 当前状态 |
|---------|---------|---------|
| PATH | `pattern`/`patterns`, `strip-prefix` | **已注册** |
| METHOD | `methods` | **已注册** |
| HEADER | `header`, `regexp` | 已实现，未注册 |
| QUERY | `param`, `regexp` | 已实现，未注册 |
| COOKIE | `name`, `regexp` | 已实现，未注册 |
| HOST | `patterns` | 已实现，未注册 |
| REMOTE_ADDR | `sources` (CIDR) | 已实现，未注册 |
| BETWEEN | `datetime1`, `datetime2` | 已实现，未注册 |

### 3.2 请求过滤器 (Filter) - 6种

| 过滤器 | 类型 | 配置参数 | 错误码 |
|-------|------|---------|-------|
| RequestRateLimiter | PRE | `replenishRate`, `burstCapacity` | 429 |
| CircuitBreaker | PRE | `failureRateThreshold`, `ringBufferSize`, `waitDurationInOpenState` | 503 |
| CorsFilter | PRE | `allowOrigins`, `allowMethods`, `allowHeaders` | - |
| TimeoutFilter | PRE | `connectTimeout`, `responseTimeout` | 504 |
| RequestRewriteFilter | PRE | `pathRegex`, `pathReplacement`, `headersToAdd/Remove` | - |
| ResponseRewriteFilter | POST | `bodyRegex`, `bodyReplacement`, `headersToAdd/Remove` | - |

Plugin 到 Filter 映射（在 `DatabaseRouteConfigProvider.mapPluginToFilters()` 中）：`rate-limit` -> RequestRateLimiter, `circuit-breaker` -> CircuitBreaker, `cors` -> CorsFilter, `timeout` -> TimeoutFilter, `request-rewrite` -> RequestRewriteFilter, `response-rewrite` -> ResponseRewriteFilter

### 3.3 负载均衡 - 4种策略

ROUND_ROBIN（默认）, RANDOM, WEIGHTED_ROUND_ROBIN, LEAST_CONNECTIONS

## 4. 数据模型

### 4.1 ER 关系

```
网关域:
  gw_route ──< gw_route_predicate >── gw_predicate
  gw_route ──< gw_route_plugin >── gw_plugin
  gw_service_node (按 service_name 分组)
  gw_route_template (路由模板)

RBAC 域:
  sys_user ──< sys_user_role >── sys_role ──< sys_role_menu >── sys_menu
  sys_role ──< sys_role_dept >── sys_dept
  sys_user ──> sys_dept (dept_id)
  sys_operation_log, sys_config
```

### 4.2 数据库表清单 (16张)

**网关域 (7张)**: `gw_route`, `gw_predicate`, `gw_route_predicate`, `gw_plugin`, `gw_route_plugin`, `gw_route_template`, `gw_service_node`

**RBAC 域 (9张)**: `sys_user`, `sys_role`, `sys_dept`, `sys_menu`, `sys_user_role`, `sys_role_menu`, `sys_role_dept`, `sys_operation_log`, `sys_config`

### 4.3 权限模型

- 认证：Sa-Token + JWT (Bearer Token)
- 权限格式：`{module}:{entity}:{action}`
- 数据权限 5 级：1=全部 / 2=自定义部门 / 3=本部门 / 4=本部门及子部门 / 5=仅本人

## 5. 路由创建流程 (端到端)

```
1. 前端 RouteFormDialog (4步向导: 基本信息→路由匹配→目标服务→插件配置)
   → POST /api/routes (RouteCreateDTO)
2. RouteServiceImpl.createRoute()
   → gw_route + gw_predicate + gw_route_plugin (持久化)
   → configRefreshService.refreshRoutes()
3. DatabaseRouteConfigProvider.refresh()
   → loadPredicates() + loadFiltersFromPlugins()
   → convertToRouteDefinition()
4. RouteConfigConverter.convert()
   → PredicateFactory.create() (仅PATH,METHOD已注册)
   → FilterFactory.create() (6种均已注册)
5. DefaultRouteManager.addRoute() → 路由生效
```

## 6. 配置参考

| 端口 | 服务 | 配置项 |
|------|------|--------|
| 9191 | Admin API (Tomcat) | `server.port` |
| 9292 | Gateway (Netty) | `muxin.gateway.netty.server.port` |

## 7. 已知问题与技术债

| 问题 | 影响 | 优先级 |
|------|------|--------|
| **Predicate 注册不全**: 仅 PATH/METHOD 注册到 Factory | HOST/HEADER/QUERY 断言保存到 DB 但运行时不生效 | **P0** |
| **Route 更新不清理旧 Predicate**: `updateRoute()` 未清理旧关联 | 重复创建或残留数据 | P1 |
| **无端到端测试**: 路由创建→加载→转发未验证 | 功能正确性无法保证 | P1 |
| **CircuitBreaker 状态无持久化**: 熔断器状态仅存内存 | 重启后状态丢失 | P2 |
| **连接池无管理界面**: 无法通过 Admin 查看/管理 | 运维困难 | P2 |
| **gateway-cloud 模块为空**: Nacos 集成代码在 gateway-admin 中 | 模块职责不清晰 | P3 |
