## Context

Muxin Gateway 的路由管理核心流程存在5类问题：登录状态异常持久化、暗色模式多处样式缺陷、权限字符串不匹配导致403、列表布局操作按钮不完整、以及前端↔数据库↔gateway-core多层数据不同步。这些问题相互独立但都需要在本次变更中统一解决。

当前架构状态：
- 认证：Sa-Token + Stateless JWT，硬编码密钥，30天过期
- 前端样式：自定义CSS变量体系（`--bg-primary`等）+ Element Plus变量混用
- 权限：Controller用`@SaCheckPermission("route:view")`，数据库存`route:detail`
- 数据同步：前端表单→DTO→DB→Provider→gateway-core 存在多处断裂

## Goals / Non-Goals

**Goals:**
- 修复登录后重启后端仍保持登录状态的问题
- 修复暗色模式下路由创建向导的所有样式问题
- 修复查看/编辑路由的权限403错误
- 优化路由列表操作列的布局
- 确保前端表单数据能完整保存到数据库并正确同步到gateway-core

**Non-Goals:**
- 不重构认证架构（仍使用Sa-Token Stateless JWT）
- 不改变gateway-core的Predicate/Filter设计
- 不实现JWT secret动态轮换
- 不添加metadata的前端编辑UI

## Decisions

### D1: 登录验证 — 前端主动校验方案

**选择**: 前端启动时调用 `GET /api/auth/user-info` 验证token有效性，失败则清除本地状态并跳转登录页

**理由**: 
- 方案A（动态生成JWT secret）会破坏水平扩展场景
- 方案B（切换到有状态JWT）改动过大
- 前端主动校验是最小侵入方案，仅修改前端bootstrap流程

**替代方案**: 在路由守卫`permission.ts`中，检测到有token时先调用后端验证接口

### D2: 暗色模式 — 统一使用自定义CSS变量

**选择**: 所有组件的`section-title`、`field-tip`、背景色统一替换为自定义变量（`--text-primary`、`--text-tertiary`、`--bg-tertiary`等），不引入Element Plus dark CSS vars

**理由**: 项目已有完整的自定义CSS变量体系，引入Element Plus dark CSS vars会造成两套变量体系并存，增加维护复杂度

**涉及文件**: StepBasicInfo、StepRouteMatching、StepPlugins、StepTargetService、SchemaField 的 scoped styles

### D3: 权限字符串 — 修改数据库种子数据对齐Controller

**选择**: 修改`data.sql`中4条权限记录 `:detail` → `:view`，并提供SQL migration脚本修正已有数据库

**理由**: Controller注解是代码层面的契约，修改种子数据比修改4个Controller注解更集中

**影响范围**: `route:detail`→`route:view`、`route:plugin:detail`→`route:plugin:view`、`system:user:detail`→`system:user:view`、`system:role:detail`→`system:role:view`

### D4: 路由列表布局 — 改固定宽度为min-width

**选择**: 将中间信息列（负载均衡、优先级、断言、插件、状态）的`width=`改为`min-width=`，让操作列有足够空间

**理由**: Element Plus table的`fixed="right"`在小屏幕下有已知渲染问题，改为min-width让列自然压缩更可靠

### D5: 数据同步 — 多层修复

**选择**: 
1. `RouteVO`增加`predicates`字段 + `getRouteDetail()`加载谓词 → 解决编辑时匹配规则丢失
2. `DatabaseRouteConfigProvider.convertToRouteDefinition()`读取`loadBalanceStrategy` → 解决负载均衡不生效
3. `pathRewrite`和`timeouts`在Service层自动转换为插件条目 → 解决静默丢弃
4. 前端`loadRouteData()`修复key映射（`hosts`/`queries`） → 解决编辑时加载错误

**理由**: 每个修复点都是独立的数据断裂，需要在对应层补齐

## Risks / Trade-offs

- **[R1]** 修改`data.sql`不影响已有数据库 → 提供 migration SQL 脚本，并在文档中说明手动执行
- **[R2]** `pathRewrite`/`timeouts`转插件条目可能影响已有路由 → 这两个功能之前从未生效，不存在兼容性问题
- **[R3]** 前端token验证增加一次API调用 → 仅在首次加载时执行，对用户体验无影响
- **[R4]** 列宽改为min-width可能在极窄屏幕下信息列被压缩过多 → 可接受，至少操作按钮始终可见
