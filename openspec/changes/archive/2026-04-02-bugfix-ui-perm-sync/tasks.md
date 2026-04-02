## 1. 登录状态修复

- [x] 1.1 修改前端路由守卫 `permission.ts`：当检测到 localStorage 有 token 时，调用 `GET /api/auth/user-info` 验证 token 有效性，失败则清除 localStorage 并跳转 `/login`
- [x] 1.2 修改 `user.ts` 的 `init()` 方法：从 localStorage 恢复 token 后主动调用后端验证，而非仅读取本地缓存

## 2. 暗色模式样式修复

- [x] 2.1 修复 `StepBasicInfo.vue`：`section-title` 颜色改为 `var(--text-primary)`，边框改为 `var(--border-primary)`；`field-tip` 颜色改为 `var(--text-tertiary)`
- [x] 2.2 修复 `StepRouteMatching.vue`：同上 section-title/field-tip 修复；`preview-title` 改为 `var(--text-primary)`，`preview-tip` 改为 `var(--text-tertiary)`，`rule-item` 改为 `var(--text-primary)`
- [x] 2.3 修复 `StepRouteMatching.vue`：`.match-preview` 背景改为 `var(--bg-tertiary)` 替代 `var(--el-fill-color-light)`
- [x] 2.4 修复 `StepRouteMatching.vue`：添加 `el-checkbox-button` 暗色模式样式覆盖（背景 `var(--input-bg)`，文字 `var(--text-primary)`，边框 `var(--border-primary)`，选中态 `var(--primary-color)`）
- [x] 2.5 修复 `StepPlugins.vue`：section-title、plugin-name、plugin-desc、plugin-priority、config-summary、list-tip、list-footer 全部替换为自定义CSS变量
- [x] 2.6 修复 `SchemaField.vue`：field-desc、array-item-index 样式变量替换；`.array-item` 背景改为 `var(--bg-tertiary)`，`.object-field` 背景改为 `var(--bg-tertiary)`
- [x] 2.7 修复 `StepRouteMatching.vue` 和 `StepTargetService.vue` 的 inline style `color: var(--el-text-color-secondary)` 改为 `var(--text-tertiary)`

## 3. 权限字符串修复

- [x] 3.1 修改 `data.sql`：将 `route:detail` → `route:view`、`route:plugin:detail` → `route:plugin:view`、`system:user:detail` → `system:user:view`、`system:role:detail` → `system:role:view`
- [x] 3.2 创建 `migration-permissions.sql` 迁移脚本，用于修正已有数据库中的权限字符串

## 4. 路由列表布局优化

- [x] 4.1 修改 `index.vue` 路由列表：将负载均衡（width="100"）、优先级（width="70"）、断言（width="60"）、插件（width="60"）、状态（width="70"）列全部改为 `min-width`，让列可压缩

## 5. 数据同步修复 — 后端

- [x] 5.1 `RouteVO` 增加 `predicates` 字段（`List<PredicateInfo>`），包含 `predicateType` 和 `args`
- [x] 5.2 `RouteServiceImpl.getRouteDetail()` 加载路由关联的谓词数据并填充到 `RouteVO.predicates`
- [x] 5.3 `RouteServiceImpl.createRoute()` 将 `pathRewrite` 自动转换为 `request-rewrite` 插件条目
- [x] 5.4 `RouteServiceImpl.createRoute()` 将 `timeouts` 自动转换为 `timeout` 插件条目
- [x] 5.5 `RouteServiceImpl.updateRoute()` 同样处理 `pathRewrite` 和 `timeouts` 转插件条目

## 6. 数据同步修复 — Provider层

- [x] 6.1 `DatabaseRouteConfigProvider.convertToRouteDefinition()` 读取 `loadBalanceStrategy` 并构建 `LoadBalanceDefinition` 设置到 `RouteDefinition.loadBalance`

## 7. 数据同步修复 — 前端

- [x] 7.1 修复 `RouteFormDialog.vue` 的 `loadRouteData()` 中 hosts 的 key 映射：从 `config.patterns` 改为 `config.hosts`
- [x] 7.2 修复 `RouteFormDialog.vue` 的 `loadRouteData()` 中 queries 的 key 映射：从 `config.params` 改为 `config.queries`

## 8. 验证

- [x] 8.1 `mvn compile` 编译通过
- [x] 8.2 `cmd /c "npm run build"` 前端构建通过
