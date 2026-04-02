# AGENTS.md

## 重要
[所有消息均使用简体中文回复我]


> AI 代理开发指南 — Muxin Gateway 项目

## 项目简介

Muxin Gateway 是基于 Netty 的高性能 API 网关，包含网关引擎 (gateway-core) 和管理后台 (gateway-admin)。详细规格参见 `openspec/project.md`。

## 构建与验证命令

```bash
# 后端编译（全模块）
mvn compile -q

# 前端构建
cd gateway-admin-ui && cmd /c "npm run build"

# 后端启动（需要先编译）
# 启动类: com.muxin.gateway.MuxinGatewayApplication (gateway-main 模块)
# 默认 profile: sqlite
# Admin API: http://localhost:9191
# Gateway Netty: http://localhost:9292
```

> **注意**: PowerShell 执行策略可能阻止 `npm` 命令，使用 `cmd /c "npm run build"` 替代。

## 模块结构

```
muxin-gateway/
├── gateway-bom/         # 依赖版本管理 (BOM)
├── gateway-core/        # 网关引擎 (纯 Netty，不依赖 Spring)
├── gateway-admin/       # 管理后台 (Spring Boot + MyBatis-Flex)
├── gateway-main/        # 启动器 (组合 admin + core，Provider 桥接)
├── gateway-cloud/       # 云服务发现 (当前为空壳)
├── gateway-admin-ui/    # 前端 (Vue 3 + Element Plus)
├── openspec/            # OpenSpec 规范文档
│   ├── project.md       # 项目完整规格
│   ├── config.yaml      # OpenSpec 配置
│   ├── specs/           # 能力规格
│   └── changes/         # 变更提案
└── docs/                # 传统文档
```

## 关键架构约束

### gateway-core 与 gateway-admin 的边界

```
gateway-core (无 Spring)          gateway-admin (Spring Boot)
─────────────────────────          ──────────────────────────
Route, Predicate, Filter          GwRoute, GwPredicate, GwPlugin
RouteDefinition                   RouteCreateDTO, RouteVO
PredicateFactory                  PredicateMapper
FilterFactory                     RoutePluginMapper
LoadBalanceStrategy               ServiceNodeMapper
ConnectionPoolManager             ConfigRefreshService

桥接层 (gateway-main):
  DatabaseRouteConfigProvider → 从 DB 读取 → 转为 RouteDefinition
  DatabaseServiceConfigProvider → 从 DB 读取 → 转为 ServiceDefinition
```

**重要**: 修改 gateway-admin 的数据结构时，必须同步检查 `DatabaseRouteConfigProvider` 和 `DatabaseServiceConfigProvider` 中的转换逻辑。修改 gateway-core 的 Predicate/Filter 时，不应影响 admin 模块。

### 数据流向

```
Admin UI → REST API → RouteServiceImpl
  → gw_route + gw_predicate + gw_route_plugin (持久化)
  → ConfigRefreshService.refreshRoutes()
  → DatabaseRouteConfigProvider.refresh()
  → RouteConfigConverter.convert()
  → DefaultRouteManager (路由生效)
```

## 代码约定

### 后端 (Java)

- **ORM**: MyBatis-Flex（非 MyBatis-Plus），使用 `@Table` 注解 + `BaseMapper<T>`
- **实体类**: 使用 Lombok `@Data`，字段名用驼峰
- **MyBatis-Flex 表定义**: 自动生成的 `*TableDef` 类在 `entity/table/` 下，使用 `static final` 实例
- **查询构造**: `QueryWrapper.create().select().from(TABLE).where(...)`
- **逻辑删除**: 所有实体有 `deleted` 字段，查询时手动过滤 `WHERE deleted = 0`
- **时间字段**: `create_time` 和 `update_time` 为 `LocalDateTime`，手动设置
- **异常处理**: 抛出 `BusinessException`，由 `GlobalExceptionHandler` 统一处理
- **认证**: Sa-Token + JWT，权限格式 `{module}:{entity}:{action}`
- **API 前缀**: `/api/` + 资源名复数形式（如 `/api/routes`, `/api/plugins`）
- **不要添加注释**，除非用户要求

### 前端 (Vue 3)

- **Composition API**: `<script setup lang="ts">`
- **UI 库**: Element Plus，使用 `unplugin-auto-import` 和 `unplugin-vue-components` 自动导入
- **状态管理**: Pinia
- **HTTP 请求**: Axios，封装在 `src/utils/request.ts`
- **API 文件**: `src/api/` 下按模块组织
- **视图组件**: `src/views/` 下按功能模块组织
- **暗黑模式**: 使用 `.dark` 类 + 自定义 CSS 变量 (`--bg-primary`, `--card-bg` 等)，**不要使用** Element Plus 的 `var(--el-bg-color)` 或硬编码 `#fff`
- **不要添加注释**，除非用户要求

### 数据库

- **当前使用**: SQLite（通过 `application-sqlite.yml` profile 激活）
- **Schema 文件**: `gateway-admin/src/main/resources/sql/sqlite/schema.sql`
- **数据初始化**: `gateway-admin/src/main/resources/sql/sqlite/data.sql`
- **自动初始化**: `SqliteInitializer` 检测数据库文件不存在时自动执行 schema + data SQL
- **表命名**: 网关表 `gw_` 前缀，系统表 `sys_` 前缀
- **字段命名**: 下划线风格 (`route_id`, `create_time`)

## OpenSpec 工作流

本项目使用 OpenSpec 进行规格驱动开发。

```bash
# 查看当前变更
openspec list --json

# 查看规格
openspec spec list

# 创建新变更提案
openspec new change <name>

# 查看变更详情
openspec show <name>
```

### 变更流程

1. **探索** → `/opsx:explore` 讨论需求和方案
2. **提案** → `/opsx:propose` 创建变更提案 (proposal → specs → design → tasks)
3. **实施** → `/opsx:apply` 按 tasks 逐步实现
4. **验证** → `/opsx:verify` 检查实现是否匹配规格
5. **归档** → `/opsx:archive` 完成归档，同步到主规格

## 已知问题与注意事项

| 问题 | 说明 | 行动建议 |
|------|------|---------|
| Predicate 注册不全 | gateway-core 仅注册了 PATH/METHOD 两种 PredicateFactory，其余 6 种有代码但未注册 | 修改 `RouteConfigConverter.initPredicateFactories()` 注册剩余 Factory |
| 路由更新不清理旧断言 | `updateRoute()` 不清理旧的 `gw_route_predicate` 关联 | 需在更新时先删除旧关联再重建 |
| PowerShell npm 问题 | Windows PowerShell 执行策略可能阻止 npm | 使用 `cmd /c "npm run ..."` |
| SQLite NOT NULL 约束 | `gw_predicate.update_time` 等字段有 NOT NULL 约束，插入时必须设置 | 确保 `create_time` 和 `update_time` 都设置 |
| 暗黑模式白块 | 使用 `#fff` 或 Element Plus CSS 变量会导致暗黑模式下出现白色块 | 使用自定义 CSS 变量 `var(--card-bg)` 等 |

## 文件导航速查

### 后端关键文件

| 文件 | 路径 | 说明 |
|------|------|------|
| 启动类 | `gateway-main/.../MuxinGatewayApplication.java` | Spring Boot 入口 |
| 路由配置桥接 | `gateway-main/.../provider/DatabaseRouteConfigProvider.java` | DB → RouteDefinition 转换 |
| 服务配置桥接 | `gateway-main/.../provider/DatabaseServiceConfigProvider.java` | DB → ServiceDefinition 转换 |
| 配置刷新 | `gateway-main/.../DefaultConfigRefreshService.java` | Admin → Core 刷新触发 |
| 路由转换器 | `gateway-core/.../RouteConfigConverter.java` | Definition → Route 实例 |
| 网关启动器 | `gateway-core/.../GatewayBootstrap.java` | Core 生命周期管理 |
| 请求处理器 | `gateway-core/.../GatewayProcessor.java` | 请求处理流水线 |
| 路由服务 | `gateway-admin/.../service/impl/RouteServiceImpl.java` | 路由 CRUD + 断言持久化 |
| 插件服务 | `gateway-admin/.../service/impl/PluginServiceImpl.java` | 插件 CRUD |
| 数据库 Schema | `gateway-admin/.../resources/sql/sqlite/schema.sql` | 16 张表定义 |

### 前端关键文件

| 文件 | 路径 | 说明 |
|------|------|------|
| 入口 | `gateway-admin-ui/src/main.ts` | Vue 应用入口 |
| 路由 | `gateway-admin-ui/src/router/` | 静态 + 动态路由 |
| 用户 Store | `gateway-admin-ui/src/stores/user.ts` | 登录态 + Token |
| 菜单 Store | `gateway-admin-ui/src/stores/menu.ts` | 动态菜单 + 权限 |
| 全局样式 | `gateway-admin-ui/src/styles/index.scss` | 暗黑模式变量 |
| CSS 变量 | `gateway-admin-ui/src/styles/variables.scss` | 亮/暗主题变量定义 |
| 路由 API | `gateway-admin-ui/src/api/routes.ts` | 路由管理接口 |
| 插件 API | `gateway-admin-ui/src/api/plugins.ts` | 插件管理接口 |
| 路由表单 | `gateway-admin-ui/src/views/routes/list/components/RouteFormDialog.vue` | 四步向导对话框 |
