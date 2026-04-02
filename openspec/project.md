# Muxin Gateway

> Lightweight API Gateway — 基于 Netty 的高性能网关，带完整的 Admin 管理后台

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
| 构建 | Maven | - |
| Java | JDK | 17 |

| 服务发现 | Nacos | 2.3.2 |

### 1.2 核心约束

- 网关引擎 (gateway-core) 不依赖 Spring，可独立运行，纯 Netty 实现
- 管理后台与网关引擎通过 Provider 接口解耦（支持热刷新，无需重启）
- 数据库默认 SQLite（嵌入式），支持扩展至 MySQL/PostgreSQL
- 前端 SPA 架构，暗黑模式（CSS 变量体系）
- 管理后台提供 OpenAPI 3.0 (Swagger) 文档

## 2. 模块架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                       muxin-gateway (parent POM)                        │
├─────────────────────────────────────────────────────────────────────┘
         │                │               │                │
         ▼                ▼               ▼                ▼
  ┌──────────┐  ┌──────────────┐  ┌───────────┐  ┌──────────────┐
  │gateway-bom│  │gateway-core│  │gateway-admin│  │gateway-main │
  │  (BOM)   │  │  (Netty)   │  │ (Spring)   │  │ (Launcher)  │
  └──────────┘  └──────────────┘  └───────────┘  └──────────────┘
                                          │               │
                                          └───────┬───────┘
                                                  │
                                          ┌───────┴───────┐
                                          │gateway-cloud │
                                          │ (Discovery)  │
                                          └──────────────┘
```

| 模块 | 职责 | 依赖 Spring | 打包方式 |
|------|------|-----------|--------|
| `gateway-bom` | 依赖版本管理 (BOM) | 否 | pom |
| `gateway-core` | 网关引擎 (Netty HTTP Server, 路由匹配, 过滤器, 负载均衡, 连接池) | **否** | jar |
| `gateway-admin` | 管理后台 (Spring Boot, MyBatis-Flex, Sa-Token, RBAC) | **是** | jar |
| `gateway-main` | 启动器 (组合 admin + core, Provider 桥接) | **是** | 可执行 jar |
| `gateway-cloud` | 云服务发现 (Nacos) | **是** | jar |

### 2.1 模块依赖关系

```
gateway-main
├── gateway-admin (Spring Boot 管理后台)
│   ├── controller (10个 REST 控制器)
│   ├── service (13个业务服务)
│   ├── entity (16个实体)
│   ├── mapper (16个 MyBatis Mapper)
│   ├── model/dto (34个 DTO)
│   ├── model/vo (15个 VO)
│   └── config (安全, 数据权限, 审计, SPA 转发)
└── gateway-core (Netty 网关引擎)
    ├── route/predicate (8个断言实现)
    ├── route/filter (6个过滤器实现)
    ├── route/loadbalance (4个负载均衡策略)
    ├── connect/netty (Netty 连接池)
    ├── server (Netty HTTP 服务器)
    └── service (服务注册中心)
```

## 3. 系统架构

### 3.1 整体架构

```
                        ┌─────────────────────────────────┐
                        │          客户端请求              │
                        └───────────────┬─────────────────┘
                                        │
                                        ▼
                        ┌───────────────────────────────────┐
                        │      Netty HTTP Server (:9292)    │
                        │      gateway-core 模块             │
                        │                                   │
                        │  ┌─────────────────────────────┐  │
                        │  │    GatewayProcessor          │  │
                        │  │                             │  │
                        │  │  1. Route Matching          │  │
                        │  │  2. PRE Filter Chain       │  │
                        │  │  3. Load Balance Select    │  │
                        │  │  4. Connection Pool Acquire │  │
                        │  │  5. Proxy to Upstream       │  │
                        │  │  6. POST Filter Chain      │  │
                        │  │  7. Response to Client     │  │
                        │  └─────────────────────────────┘  │
                        └───────────────┬─────────────────┘
                                        │
                    ┌───────────────────┴──────────────────┐
                    ▼                                   ▼
          ┌───────────────────────┐         ┌───────────────────────┐
          │  upstream service      │         │  upstream service      │
          │  (192.168.1.10:8080)  │         │  (192.168.1.11:8080)  │
          └───────────────────────┘         └───────────────────────┘
```

### 3.2 双进程架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                      gateway-main 进程                               │
│                                                                     │
│  ┌─────────────────────┐         ┌───────────────────────────────┐  │
│  │   gateway-admin     │         │        gateway-core            │  │
│  │   (Tomcat :9191)    │         │        (Netty :9292)           │  │
│  │                     │         │                               │  │
│  │  REST API 管理接口  │ Provider │  GatewayBootstrap           │  │
│  │  RouteController  ─┼────────▶│  RouteConfigProvider (DB)     │  │
│  │  PluginController │ refresh │  ServiceConfigProvider (DB)     │  │
│  │  UserController   │         │  RouteManager                 │  │
│  │  ...                │         │  GatewayProcessor             │  │
│  │                     │         │  ConnectionPoolManager        │  │
│  └─────────────────────┘         └───────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.3 请求处理流水线

```
客户端请求
 (:9292)
        │
        ▼
┌───────────────────┐
│ RouteManager      │  匹配路由 (Predicate 匹配)
│ .matchRoute()     │  → DefaultRoute (按 order 排序，取第一个匹配)
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│ PRE Filter Chain │  按优先级排序执行
│ (RateLimiter,     │  → CircuitBreaker 棦Cors → Timeout → ...
│  CorsFilter,      │
│  TimeoutFilter)   │
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│ LoadBalance       │  选择目标实例
│ .selectTarget()   │  → RoundRobin / Random / WeightedRR / LeastConn
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│ ConnectionPool    │  获取/创建连接
│ .acquire()        │  → Netty FixedChannelPool (per endpoint)
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│ Proxy Request     │  转发请求到上游
│ + Path Stripping  │  → PathPredicate stripPrefix 处理
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│ POST Filter Chain │  处理响应
│ (ResponseRewrite) │  → 修改响应体/头
└───────┬───────────┘
        │
        ▼
   响应返回客户端
```

## 4. 核心能力规格

### 4.1 路由匹配 (Predicate)

网关支持 8 种路由断言类型。路由按 `order` 升序排序，第一个匹配成功的路由将被选中。

| 断言类型 | 实现类 | 配置参数 | 当前状态 |
|---------|--------|---------|---------|
| PATH | `PathPredicate` | `pattern`/`patterns`, `strip-prefix` | **已注册** |
| METHOD | `MethodPredicate` | `methods` | **已注册** |
| HEADER | `HeaderPredicate` | `header`, `regexp` | 已实现，未注册到 Factory |
| QUERY | `QueryPredicate` | `param`, `regexp` | 已实现，未注册到 Factory |
| COOKIE | `CookiePredicate` | `name`, `regexp` | 已实现，未注册到 Factory |
| HOST | `HostPredicate` | `patterns` | 已实现，未注册到 Factory |
| REMOTE_ADDR | `RemoteAddrPredicate` | `sources` (CIDR) | 已实现，未注册到 Factory |
| BETWEEN | `BetweenPredicate` | `datetime1`, `datetime2` | 已实现，未注册到 Factory |

> **已知问题**: `RouteConfigConverter.initPredicateFactories()` 仅注册了 `PathPredicateFactory` 和 `MethodPredicateFactory`。其余 6 种断言虽有实现代码，但未注册到 `PredicateFactory` 映射表中。Admin 前端的 `saveRouteMatching()` 会保存 HOST/HEADER/QUERY 类型的断言到数据库，但 gateway-core 加载时无法创建对应的 Predicate 实例。

### 4.2 请求过滤器 (Filter)

网关支持 6 种过滤器。过滤器分为 PRE（请求阶段）和 POST（响应阶段）两种类型。

| 过滤器名称 | 类型 | 实现类 | 配置参数 | 错误响应 |
|-----------|------|--------|---------|---------|
| RequestRateLimiter | PRE | `RequestRateLimiterFilter` | `replenishRate`(10), `burstCapacity`(20) | HTTP 429 |
| CircuitBreaker | PRE | `CircuitBreakerFilter` | `failureRateThreshold`(50%), `ringBufferSize`(100), `waitDurationInOpenState`(60000ms) | HTTP 503 |
| CorsFilter | PRE | `CorsFilter` | `allowOrigins`, `allowMethods`, `allowHeaders`, `allowCredentials`, `maxAge`(3600) | - |
| TimeoutFilter | PRE | `TimeoutFilter` | `connectTimeout`(5000ms), `responseTimeout`(30000ms) | HTTP 504 |
| RequestRewriteFilter | PRE | `RequestRewriteFilter` | `pathRegex`, `pathReplacement`, `headersToAdd`, `headersToRemove` | - |
| ResponseRewriteFilter | POST | `ResponseRewriteFilter` | `bodyRegex`, `bodyReplacement`, `headersToAdd`, `headersToRemove` | - |

**过滤器映射关系**: Admin 管理的 Plugin 通过 `DatabaseRouteConfigProvider.mapPluginToFilters()` 转换为 gateway-core 的 Filter:

```
Admin Plugin         →  Core Filter           →  FilterDefinition.name
─────────────────────────────────────────────────────────────────
rate-limit           →  RequestRateLimiter     →  "RequestRateLimiter"
circuit-breaker      →  CircuitBreaker         →  "CircuitBreaker"
cors                 →  CorsFilter             →  "CorsFilter"
timeout              →  TimeoutFilter          →  "TimeoutFilter"
request-rewrite      →  RequestRewriteFilter   →  "RequestRewriteFilter"
response-rewrite     →  ResponseRewriteFilter  →  "ResponseRewriteFilter"
```

### 4.3 负载均衡

| 策略 | 实现类 | 说明 |
|------|--------|------|
| ROUND_ROBIN | `RoundRobinLoadBalanceStrategy` | 依次轮询选择实例（默认策略） |
| RANDOM | `RandomLoadBalanceStrategy` | 随机选择实例 |
| WEIGHTED_ROUND_ROBIN | `WeightedRoundRobinLoadBalanceStrategy` | 按权重轮询选择 |
| LEAST_CONNECTIONS | `LeastConnectionsLoadBalanceStrategy` | 选择活跃连接数最少的实例 |

### 4.4 连接池

- 基于 Netty `FixedChannelPool`，每个目标地址独立池
- 默认配置: `maxConnections=10`, `acquireTimeout=30s`, `connectTimeout=10s`
- 支持 Epoll (Linux) 和 NIO (跨平台)
- 连接健康检查、预热 (warmup)、统计追踪

### 4.5 服务发现

| 类型 | 说明 |
|------|------|
| STATIC | 静态地址列表（数据库配置或 YAML） |
| DISCOVERY | 动态服务发现（Nacos 注册中心） |

### 4.6 配置提供者 (ConfigProvider)

```
┌──────────────────────────────────────────────────────────────────┐
│              ConfigProvider 层次结构                              │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  @ConditionalOnMissingBean                                       │
│  ├── DatabaseRouteConfigProvider (gateway-main, 优先)            │
│  │   数据源: SQLite (gw_route + gw_predicate + gw_route_plugin) │
│  │   触发: Admin API 调用 refresh()                               │
│  │                                                               │
│  └── YamlRouteConfigProvider (gateway-core, 兜底)                │
│      数据源: YAML 配置文件                                        │
│      触发: WatchService 文件变更监听                               │
│                                                                  │
│  刷新链路:                                                        │
│  Admin API → ConfigRefreshService.refreshAll()                   │
│           → RouteConfigProvider.refresh()                         │
│           → ConfigChangedEvent → GatewayBootstrap.refreshRoutes() │
│           → RouteConfigConverter.convert() → 新路由注册            │
└──────────────────────────────────────────────────────────────────┘
```

## 5. 数据模型

### 5.1 ER 关系图

```
┌────────────────────────────────────────────────────────────────────────┐
│                         网关域 (Gateway)                                │
│                                                                        │
│  gw_route ──────────< gw_route_predicate >──────── gw_predicate        │
│  (路由)               (关联表)                   (断言定义)             │
│      │                                                                 │
│      │                                                                 │
│      └────────────────< gw_route_plugin >────────── gw_plugin           │
│                       (关联表+配置)                 (插件定义)           │
│                                                                        │
│  gw_route_template (路由模板)                                           │
│                                                                        │
│  gw_service_node (服务节点)                                             │
│      service_name 分组                                                   │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                         RBAC 域 (系统管理)                              │
│                                                                        │
│  sys_user ───────< sys_user_role >────────── sys_role                  │
│  (用户)             (关联表)                  (角色)                    │
│      │                │                       │  │                      │
│      │                │                       │  ▼                      │
│      │                │               sys_role_menu (角色-菜单)          │
│      │                │                       │  │                      │
│      │                │                       │  ▼                      │
│      │                │                  sys_menu (菜单/权限)            │
│      │                │                                               │
│      └── dept_id      │               sys_role_dept (角色-部门)         │
│          │            │                       │                          │
│          ▼            │                       ▼                          │
│       sys_dept ───────┘                  sys_dept (自定义数据范围)      │
│       (部门)                                                         │
│                                                                     │
│  sys_operation_log (操作日志)                                        │
│  sys_config (系统配置)                                               │
└────────────────────────────────────────────────────────────────────────┘
```

### 5.2 数据库表清单 (16张表)

#### 网关域 (7张表)

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `gw_route` | 路由定义 | `route_id`(唯一), `uri`, `order`, `enabled`, `load_balance_strategy` |
| `gw_predicate` | 断言定义 (可复用) | `predicate_type`, `args`(JSON) |
| `gw_route_predicate` | 路由-断言关联 | `route_id` + `predicate_id`(联合唯一) |
| `gw_plugin` | 插件定义 (含 JSON Schema) | `plugin_name`(唯一), `plugin_type`, `schema`, `default_config` |
| `gw_route_plugin` | 路由-插件关联 (含覆盖配置) | `route_id` + `plugin_id`(联合唯一), `config`(JSON) |
| `gw_route_template` | 路由模板 | `category`, `config`(JSON), `variables`(JSON) |
| `gw_service_node` | 服务节点实例 | `service_name`, `address`, `port`, `weight`, `status`, 健康检查配置 |

#### RBAC 域 (9张表)

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `sys_user` | 用户账户 | `username`(唯一), `dept_id`, `status` |
| `sys_role` | 角色 | `role_code`(唯一), `data_scope`(1-5), `status` |
| `sys_dept` | 部门 (树形) | `parent_id`, `ancestors`, `dept_code` |
| `sys_menu` | 菜单 (M目录/C页面/F按钮) | `parent_id`, `menu_type`, `perms` |
| `sys_user_role` | 用户-角色关联 | `user_id` + `role_id`(联合唯一) |
| `sys_role_menu` | 角色-菜单关联 | `role_id` + `menu_id`(联合唯一) |
| `sys_role_dept` | 角色-部门关联 (自定义数据范围) | `role_id` + `dept_id`(联合唯一) |
| `sys_operation_log` | 操作审计日志 | `operator_id`, `operate_time`, `module` |
| `sys_config` | 系统键值配置 | `config_key`(唯一), `config_value` |

## 6. API 接口清单

### 6.1 网关管理 API

| 前缀 | 控制器 | 端点数 | 核心能力 |
|------|--------|--------|---------|
| `/api/auth` | AuthController | 4 | 登录、登出、用户信息、刷新令牌 |
| `/api/routes` | RouteController | 13 | 路由 CRUD、启用/禁用、测试、配置刷新 |
| `/api/nodes` | ServiceNodeController | 14 | 服务节点 CRUD、服务统计、发现集成 |
| `/api/plugins` | PluginController | 5 | 插件 CRUD |

### 6.2 系统管理 API

| 前缀 | 控制器 | 端点数 | 核心能力 |
|------|--------|--------|---------|
| `/api/users` | UserController | 14 | 用户 CRUD、角色分配、密码管理 |
| `/api/roles` | RoleController | 13 | 角色 CRUD、菜单权限分配 |
| `/api/dept` | DeptController | 11 | 部门树 CRUD、移动、统计 |
| `/api/menus` | MenuController | 12 | 菜单树 CRUD、用户菜单、权限 |
| `/api/system/logs/operation` | OperationLogController | 7 | 日志查询、导出、清理 |
| `/api/configs` | ConfigController | 11 | 配置 CRUD、缓存刷新 |
| `/api/test` | TestController | 3 | 开发辅助 (初始化用户) |

### 6.3 权限模型

- 认证方式: Sa-Token + JWT (Bearer Token)
- 权限格式: `{module}:{entity}:{action}`，如 `route:create`, `system:user:list`
- 数据权限: 5 级数据作用域 (`DataScope` 注解 + AOP)

| data_scope | 名称 | SQL 过滤规则 |
|-----------|------|-------------|
| 1 | 全部数据 | 无过滤 |
| 2 | 自定义数据 | `dept_id IN (sys_role_dept 选定的部门)` |
| 3 | 本部门数据 | `dept_id = 当前用户部门` |
| 4 | 本部门及以下 | `dept_id IN (当前用户部门 + 所有子部门)` |
| 5 | 仅本人数据 | `id = 当前用户` |

## 7. 前端架构

### 7.1 技术组成

- **框架**: Vue 3 (Composition API)
- **UI 库**: Element Plus
- **状态管理**: Pinia (2个 Store)
- **路由**: Vue Router (动态路由)
- **HTTP**: Axios
- **构建**: Vite 5.0

### 7.2 页面结构

```
src/
├── views/
│   ├── login/                    # 登录页
│   ├── dashboard/                # 仪表盘
│   ├── profile/                  # 个人资料
│   ├── error/ (403, 404)         # 错误页
│   ├── routes/
│   │   ├── list/                 # 路由管理
│   │   │   └── components/
│   │   │       ├── RouteFormDialog.vue    # 路由表单对话框
│   │   │       ├── StepBasicInfo.vue      # 步骤1: 基本信息
│   │   │       ├── StepRouteMatching.vue  # 步骤2: 路由匹配
│   │   │       ├── StepTargetService.vue  # 步骤3: 目标服务
│   │   │       ├── StepPlugins.vue        # 步骤4: 插件配置
│   │   │       ├── StepNavigation.vue     # 步骤导航
│   │   │       ├── PluginConfigDrawer.vue # 插件配置抽屉
│   │   │       └── SchemaField.vue       # Schema 驱动表单 (递归)
│   │   ├── nodes/                 # 服务节点管理
│   │   └── plugins/               # 插件管理
│   └── system/
│       ├── users/                 # 用户管理
│       ├── roles/                 # 角色管理
│       ├── departments/           # 部门管理
│       ├── operation-logs/        # 操作日志
│       └── config/                # 系统配置
```

### 7.3 动态路由机制

```
登录 → 获取用户菜单 → 注册 Vue Router 路由 → 渲染侧边栏菜单

静态路由 (仅基础):
  /login, /404, /403

动态路由 (按权限加载):
  /system/users, /system/roles, /system/departments,
  /routes/list, /routes/nodes, /routes/plugins, ...
```

### 7.4 暗黑模式

- 使用 `.dark` CSS 类切换
- 自定义 CSS 变量体系（不依赖 Element Plus 变量）
- 关键变量: `--bg-primary`, `--bg-secondary`, `--card-bg`, `--text-primary`, `--border-primary`

## 8. 路由创建流程 (端到端)

```
┌──────────────────────────────────────────────────────────────────────┐
│                     路由创建端到端数据流                               │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  1. 前端 (RouteFormDialog.vue)                                       │
│     用户填写四步向导:                                                 │
│     Step1: routeId, routeName, uri, order, enabled                   │
│     Step2: path pattern, methods, headers, hosts, queries            │
│     Step3: serviceName, loadBalanceStrategy                         │
│     Step4: selectedPlugins[] with config                             │
│                    │                                                 │
│                    ▼                                                 │
│  2. API 请求 POST /api/routes                                       │
│     Body: RouteCreateDTO {                                          │
│       routeId, routeName, uri, order, enabled,                      │
│       matching: { path, methods, headers, hosts, queries },         │
│       plugins: [{ pluginId, config, enabled }]                      │
│     }                                                               │
│                    │                                                 │
│                    ▼                                                 │
│  3. RouteServiceImpl.createRoute()                                  │
│     ├── 保存 gw_route (基本信息)                                    │
│     ├── saveRouteMatching() →                                       │
│     │   ├── gw_predicate (PATH) + gw_route_predicate               │
│     │   ├── gw_predicate (METHOD) + gw_route_predicate (如选择)     │
│     │   ├── gw_predicate (HOST) + gw_route_predicate (如选择)       │
│     │   ├── gw_predicate (HEADER) + gw_route_predicate (如选择)     │
│     │   └── gw_predicate (QUERY) + gw_route_predicate (如选择)      │
│     ├── saveRoutePlugins() →                                        │
│     │   └── gw_route_plugin (每个选中的插件 + 覆盖配置)              │
│     └── configRefreshService.refreshRoutes()                          │
│                    │                                                 │
│                    ▼                                                 │
│  4. DatabaseRouteConfigProvider.refresh()                           │
│     ├── 加载 gw_route (过滤 deleted, 按 order 排序)              │
│     ├── loadPredicates() → routePredicateMapper.findPredicatesByRouteId()│
│     │   └── 读取 gw_predicate.predicate_type + args             │
│     │       → 转为 PredicateDefinition(name=PATH/METHOD, args={...})│
│     ├── loadFiltersFromPlugins() → routePluginMapper.findPluginsByRouteId()│
│     │   └── 读取 gw_plugin.plugin_name + plugin_type + config       │
│     │       → mapPluginToFilters(): plugin_name → FilterDefinition │
│     └── convertToRouteDefinition() → RouteDefinition              │
│                    │                                                 │
│                    ▼                                                 │
│  5. GatewayBootstrap.refreshRoutes()                                 │
│     └── RouteConfigConverter.convert()                                │
│         ├── PredicateFactory.create() (仅 PATH, METHOD 已注册)       │
│         ├── FilterFactory.create() (6种均已注册)                    │
│         ├── LoadBalanceStrategyFactory.create()                     │
│         └── RouteServiceFactory.create()                            │
│                    │                                                 │
│                    ▼                                                 │
│  6. DefaultRouteManager.addRoute()                                  │
│     路由注册完成，网关开始处理匹配的请求                                │
└──────────────────────────────────────────────────────────────────────┘
```

## 9. 配置参考

### 9.1 端口配置

| 端口 | 服务 | 配置项 |
|------|------|--------|
| 9191 | Admin API (Tomcat) | `server.port` |
| 9292 | Gateway (Netty) | `muxin.gateway.netty.server.port` |

### 9.2 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:sqlite:${muxin.db.path}
  profiles:
    active: sqlite  # 使用 SQLite Profile
```

### 9.3 网关引擎配置

```yaml
muxin:
  gateway:
    netty:
      server:
        port: 9292
        boss-threads: 1
        worker-threads: 4
        max-content-length: 67108864  # 64MB
        request-timeout: 30000
        connection-timeout: 5000
        idle-timeout: 300000
    config-file: gateway-routes.yml    # YAML 配置文件 (兜底)
    config-watch-enabled: true          # 文件变更监听
```

## 10. 已知问题与技术债

| 问题 | 影响 | 优先级 |
|------|------|--------|
| **Predicate 注册不全**: 仅 PATH/METHOD 注册到 Factory | HOST/HEADER/QUERY 断言保存到 DB 但运行时不生效 | **P0** |
| **Route 更新不清理旧 Predicate**: `updateRoute()` 未清理旧的 `gw_route_predicate` 关联 | 重复创建或残留数据 | P1 |
| **无端到端测试**: 路由创建→加载→转发未验证 | 功能正确性无法保证 | P1 |
| **CircuitBreaker 状态无持久化**: 熔断器状态仅存内存 | 重启后状态丢失 | P2 |
| **连接池无全局管理界面**: 无法通过 Admin 查看/管理连接池 | 运维困难 | P2 |
| **gateway-cloud 模块为空**: Nacos 集成代码在 gateway-admin 中 | 模块职责不清晰 | P3 |
