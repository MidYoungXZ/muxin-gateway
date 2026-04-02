## Why

路由管理流程存在多个阻断性缺陷：编辑/查看路由因权限字符串不匹配报403、暗色模式下创建路由页面样式不可用、登录状态在后端重启后不应保留却保留了、路由列表操作按钮显示不完整、以及前端表单数据与数据库/gateway-core之间存在多层不同步（谓词读取丢失、负载均衡策略未同步、pathRewrite和timeouts被静默丢弃）。这些问题导致路由管理核心流程不可用，必须统一修复。

## What Changes

- 修复登录持久化问题：前端启动时主动调用后端验证token有效性，后端重启后token失效则强制重新登录
- 修复暗色模式样式：统一使用自定义CSS变量替代Element Plus变量，修复section-title灰色、checkbox-button颜色突兀、匹配规则预览白色底色问题
- 修复权限字符串不匹配：将数据库 `data.sql` 中的 `:detail` 权限统一改为 `:view`，与Controller注解对齐
- 优化路由列表布局：将固定宽度列改为 `min-width`，操作列自适应显示
- 修复前端表单↔数据库↔gateway-core数据同步：
  - `RouteVO` 增加谓词列表字段，`getRouteDetail()` 加载并返回谓词数据
  - 修复前端加载谓词时的key不匹配（hosts/queries）
  - `DatabaseRouteConfigProvider` 加载 `loadBalanceStrategy` 到 `RouteDefinition`
  - `pathRewrite` 和 `timeouts` 转换为自动插件条目持久化

## Capabilities

### New Capabilities

_(无新增能力)_

### Modified Capabilities

- `admin-ui`: 修复暗色模式样式变量、登录状态验证、路由列表布局响应式
- `route-management`: 修复权限字符串、谓词读取返回、表单字段对齐、gateway-core数据同步

## Impact

- **前端** `gateway-admin-ui`：样式文件、路由列表组件、路由表单组件、用户store、权限路由守卫
- **后端** `gateway-admin`：`RouteController` 权限注解或 `data.sql` 权限数据、`RouteVO` 增加字段、`RouteServiceImpl.getRouteDetail()` 增加谓词加载
- **后端** `gateway-main`：`DatabaseRouteConfigProvider.convertToRouteDefinition()` 增加 loadBalance 和 timeouts 同步
- **数据库**：`data.sql` 权限种子数据修正、可能需要 migration 脚本修正已有数据
