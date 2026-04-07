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
- **查询构造**: `QueryWrapper.create().select().from(TABLE).where(...)`
- **逻辑删除**: 所有实体有 `deleted` 字段，查询时手动过滤 `WHERE deleted = 0`
- **时间字段**: `create_time` 和 `update_time` 为 `LocalDateTime`，手动设置
- **异常处理**: 抛出 `BusinessException`，由 `GlobalExceptionHandler` 统一处理
- **认证**: Sa-Token + JWT（有状态），权限格式 `{module}:{entity}:{action}`
- **API 前缀**: `/api/` + 资源名复数形式（如 `/api/routes`, `/api/plugins`）
- **不要添加注释**，除非用户要求

### MyBatis-Flex 使用规范

> 详细规范参见 `openspec/specs/mybatis-flex-query-standard/spec.md`

核心原则（AI生成代码必须遵循）：
1. 所有查询必须用 `QueryWrapper` 或 `QueryChain`
2. 所有条件必须支持 `null 判断`（动态开关）
3. 禁止字符串拼接 SQL
4. 优先使用 Lambda 写法
5. 分页统一用 `paginate`
6. 复杂 SQL 用 join + wrapper，不要 XML
7. Mapper 层禁止使用 @Select/@Delete/@Update/@Insert 注解（特殊情况除外）

### 前端 (Vue 3)

- **Composition API**: `<script setup lang="ts">`
- **UI 库**: Element Plus，使用 `unplugin-auto-import` 和 `unplugin-vue-components` 自动导入
- **状态管理**: Pinia
- **HTTP 请求**: Axios，封装在 `src/utils/request.ts`
- **暗黑模式**: 使用 `.dark` 类 + 自定义 CSS 变量 (`--bg-primary`, `--card-bg` 等)，**不要使用** Element Plus 的 `var(--el-bg-color)` 或硬编码 `#fff`
- **不要添加注释**，除非用户要求

### 数据库

- **当前使用**: SQLite（通过 `application-sqlite.yml` profile 激活）
- **自动初始化**: `SqliteInitializer` 检测数据库文件不存在时自动执行 schema + data SQL
- **表命名**: 网关表 `gw_` 前缀，系统表 `sys_` 前缀

## OpenSpec 工作流

```bash
openspec list --json          # 查看当前变更
openspec spec list            # 查看规格
openspec new change <name>    # 创建新变更提案
openspec show <name>          # 查看变更详情
```

变更流程：探索(`/opsx:explore`) → 提案(`/opsx:propose`) → 实施(`/opsx:apply`) → 验证(`/opsx:verify`) → 归档(`/opsx:archive`)

## 文件导航速查

| 文件 | 路径 | 说明 |
|------|------|------|
| 启动类 | `gateway-main/.../MuxinGatewayApplication.java` | Spring Boot 入口 |
| 路由桥接 | `gateway-main/.../provider/DatabaseRouteConfigProvider.java` | DB → RouteDefinition |
| 服务桥接 | `gateway-main/.../provider/DatabaseServiceConfigProvider.java` | DB → ServiceDefinition |
| 配置刷新 | `gateway-main/.../DefaultConfigRefreshService.java` | Admin → Core 刷新 |
| 路由转换 | `gateway-core/.../RouteConfigConverter.java` | Definition → Route |
| 路由服务 | `gateway-admin/.../service/impl/RouteServiceImpl.java` | 路由 CRUD |
| 路由表单 | `gateway-admin-ui/.../RouteFormDialog.vue` | 四步向导 |
