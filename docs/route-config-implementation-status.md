# 路由配置页面重构 - 实现状态文档

## 1. 项目概述

参考 APISIX Dashboard 设计理念，重新设计路由配置页面的交互流程，实现四步向导式配置。

## 2. 已完成工作

### 2.1 前端组件

| 文件 | 路径 | 状态 |
|------|------|------|
| StepNavigation.vue | `src/views/routes/list/components/` | ✅ 完成 |
| StepBasicInfo.vue | `src/views/routes/list/components/` | ✅ 完成 |
| StepRouteMatching.vue | `src/views/routes/list/components/` | ✅ 完成 |
| StepTargetService.vue | `src/views/routes/list/components/` | ✅ 完成 |
| StepPlugins.vue | `src/views/routes/list/components/` | ✅ 完成 |
| PluginConfigDrawer.vue | `src/views/routes/list/components/` | ✅ 完成 |
| RouteFormDialog.vue | `src/views/routes/list/components/` | ✅ 完成 |
| 路由页面 | `src/views/routes/list/index.vue` | ✅ 完成 |
| 插件页面 | `src/views/routes/plugins/index.vue` | ✅ 完成 |
| API 类型定义 | `src/api/routes.ts` | ✅ 完成 |
| 插件 API | `src/api/plugins.ts` | ✅ 完成（含 PluginInfo 类型定义） |

### 2.2 后端代码

| 文件 | 路径 | 状态 |
|------|------|------|
| GwPlugin.java | `entity/` | ✅ 完成 |
| GwRoutePlugin.java | `entity/` | ✅ 完成 |
| GwPluginTableDef.java | `entity/table/` | ✅ 完成 |
| GwRoutePluginTableDef.java | `entity/table/` | ✅ 完成 |
| RouteMatchingDTO.java | `model/dto/` | ✅ 完成 |
| RoutePluginDTO.java | `model/dto/` | ✅ 完成 |
| RouteCreateDTO.java | `model/dto/` | ✅ 完成 |
| RouteUpdateDTO.java | `model/dto/` | ✅ 完成 |
| PluginVO.java | `model/vo/` | ✅ 完成 |
| RouteVO.java | `model/vo/` | ✅ 完成（添加 plugins 字段） |
| PluginMapper.java | `mapper/` | ✅ 完成 |
| RoutePluginMapper.java | `mapper/` | ✅ 完成 |
| PluginService.java | `service/` | ✅ 完成 |
| PluginServiceImpl.java | `service/impl/` | ✅ 完成 |
| RouteServiceImpl.java | `service/impl/` | ✅ 完成 |
| PluginController.java | `controller/` | ✅ 完成 |

### 2.3 数据库

| 文件 | 路径 | 状态 |
|------|------|------|
| plugin_tables.sql | `resources/sql/` | ✅ 完成（含表结构和预置数据） |
| init_system_data.sql | `resources/sql/` | ✅ 完成（更新菜单配置） |

### 2.4 编译状态

| 项目 | 状态 |
|------|------|
| 前端 (npm run build) | ✅ 成功 |
| 后端 (mvn compile) | ✅ 成功 |

---

## 3. 新菜单设计

### 3.1 菜单结构

```
路由管理
├── 路由         /routes/list
├── 服务         /routes/nodes
└── 插件         /routes/plugins    [新增]

系统管理
├── 用户管理     /system/users
├── 角色管理     /system/roles
├── 部门管理     /system/departments
├── 权限管理     /system/permissions
├── 操作日志     /system/operation-logs
└── 系统配置     /system/config
```

### 3.2 已移除菜单

| 原菜单 | 处理方式 |
|--------|----------|
| 过滤器管理 | 已删除页面文件和 API 文件 |
| 断言管理 | 已删除页面文件和 API 文件 |

### 3.3 菜单权限配置

```sql
-- 插件菜单 (ID: 105)
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order)
VALUES (105, 1, '插件', 'menu.routes.plugins', 'C', '/routes/plugins', 'routes/plugins/index', 'route:plugin:list', 'Plug', 3);

-- 插件按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms) VALUES
(1051, 105, '插件查看', 'F', 'route:plugin:view'),
(1052, 105, '插件新增', 'F', 'route:plugin:create'),
(1053, 105, '插件修改', 'F', 'route:plugin:update'),
(1054, 105, '插件删除', 'F', 'route:plugin:delete');
```

---

## 4. 未完成工作

### 4.1 数据库表初始化

**状态**: ❌ 未执行

**待执行SQL**:
1. `plugin_tables.sql` - 创建 gw_plugin 和 gw_route_plugin 表
2. `init_system_data.sql` - 更新菜单配置（已修改，需重新执行）

**执行命令**:
```bash
# MySQL
mysql -u root -p gateway_db < gateway-admin/src/main/resources/sql/plugin_tables.sql
mysql -u root -p gateway_db < gateway-admin/src/main/resources/sql/init_system_data.sql
```

### 4.2 插件 Schema 表单渲染器

**状态**: ✅ 递归 Schema 组件已完成

**当前实现**:
- `SchemaField.vue` - 递归组件，支持所有 JSON Schema 类型
- `PluginConfigDrawer.vue` - 使用 SchemaField 渲染插件配置

**已支持类型**:
| 类型 | 渲染组件 | 说明 |
|------|----------|------|
| string | el-input / el-select | 支持 enum、password format |
| number / integer | el-input-number | 支持 minimum/maximum 约束 |
| boolean | el-switch | - |
| array (string items) | el-tag + el-input | 标签式输入 |
| array (object items) | 递归 SchemaField | 可增删的对象列表 |
| object (有 properties) | 递归 SchemaField | 嵌套对象表单 |
| object (无 properties) | el-input textarea | JSON 文本编辑 |

### 4.3 后端 - 插件配置持久化

**状态**: ⚠️ 基础实现完成

**待完善**:
- 插件配置校验（基于 JSON Schema）
- 插件配置版本管理
- 插件配置导入/导出

---

## 5. 后续待办事项

### 优先级 P0（核心功能）

- [x] 创建插件页面 `src/views/routes/plugins/index.vue`
- [ ] 执行数据库初始化 SQL
- [ ] 验证端到端流程：创建路由 → 配置插件 → 保存 → 查看

### 优先级 P1（重要功能）

- [x] 优化 Schema 驱动表单渲染器（递归 SchemaField 组件）
- [ ] 插件配置预览和测试功能
- [ ] 路由配置复制功能
- [ ] 批量操作优化

### 优先级 P2（增强功能）

- [ ] 插件配置模板功能
- [ ] 路由配置版本对比
- [ ] 配置导入/导出
- [ ] 操作日志记录

### 优先级 P3（清理工作）

- [x] 删除旧的过滤器/断言管理页面和 API 文件
- [x] 清理 routes.ts 中重复的 pluginsApi 和 PluginInfo 定义
- [x] 更新设计文档

---

## 6. 技术要点

### 6.1 四步配置流程

```
Step 1: 基本信息
├── routeId (必填)
├── routeName (必填)
├── order
├── enabled
└── description

Step 2: 路由匹配
├── pathPattern (必填)
├── matchType (ANT/REGEX/EXACT)
├── methods (可选)
├── headers (可选)
├── hosts (可选)
└── queries (可选)

Step 3: 目标服务
├── serviceName (必填)
├── loadBalanceStrategy (必填)
├── pathRewrite (可选)
└── timeouts (可选)

Step 4: 插件配置
├── 插件类型筛选 (AUTH/FILTER)
├── 插件选择 + 配置
└── 优先级列表
```

### 6.2 插件类型定义

| 类型 | 阶段 | 优先级范围 | 说明 |
|------|------|-----------|------|
| AUTH | 认证阶段 | 8000-7000 | JWT、Basic、API Key 等 |
| FILTER | 请求处理 | 6000-1000 | 限流、跨域、熔断、重写等 |

### 6.3 数据提交格式

```typescript
// 前端提交
{
  routeId: "user-api-v1",
  routeName: "用户服务API v1",
  uri: "lb://user-service",
  matching: {
    path: { pattern: "/api/v1/**", matchType: "ANT" },
    methods: ["GET", "POST"]
  },
  plugins: [
    { pluginId: 1, config: { secret: "xxx" }, enabled: true },
    { pluginId: 4, config: { rate: 100 }, enabled: true }
  ]
}

// 后端转换
// matching → PredicateDefinition
// plugins → FilterDefinition
```

---

## 7. 文件清单

### 7.1 新增文件

```
前端:
├── src/views/routes/list/components/StepNavigation.vue
├── src/views/routes/list/components/StepBasicInfo.vue
├── src/views/routes/list/components/StepRouteMatching.vue
├── src/views/routes/list/components/StepTargetService.vue
├── src/views/routes/list/components/StepPlugins.vue
├── src/views/routes/list/components/SchemaField.vue  ← 递归 Schema 表单组件
├── src/views/routes/list/components/PluginConfigDrawer.vue
├── src/views/routes/list/components/RouteFormDialog.vue
├── src/api/plugins.ts (含 PluginInfo 类型定义)
└── src/views/routes/plugins/index.vue

后端:
├── entity/GwPlugin.java
├── entity/GwRoutePlugin.java
├── entity/table/GwPluginTableDef.java
├── entity/table/GwRoutePluginTableDef.java
├── mapper/PluginMapper.java
├── mapper/RoutePluginMapper.java
├── model/dto/RouteMatchingDTO.java
├── model/dto/RoutePluginDTO.java
├── model/vo/PluginVO.java
├── service/PluginService.java
├── service/impl/PluginServiceImpl.java
├── controller/PluginController.java
└── resources/sql/plugin_tables.sql

文档:
└── docs/route-config-design.md
```

### 7.2 修改文件

```
前端:
├── src/api/routes.ts (移除重复的 pluginsApi 和 PluginInfo)
├── src/views/routes/list/components/StepPlugins.vue (更新导入路径)
├── src/views/routes/list/components/PluginConfigDrawer.vue (更新导入路径)
├── src/views/routes/plugins/index.vue (更新导入路径)
└── src/views/routes/list/index.vue (重写)

后端:
├── model/dto/RouteCreateDTO.java
├── model/dto/RouteUpdateDTO.java
├── model/vo/RouteVO.java
├── service/impl/RouteServiceImpl.java
└── resources/sql/init_system_data.sql
```

### 7.3 已删除文件

```
前端:
├── src/views/routes/filters/index.vue        ← 已删除
├── src/views/routes/predicates/index.vue     ← 已删除
├── src/api/filters.ts                        ← 已删除
└── src/api/predicates.ts                     ← 已删除
```

---

## 8. 测试检查清单

### 8.1 功能测试

- [ ] 新增路由 - 四步流程
- [ ] 编辑路由 - 数据回显
- [ ] 删除路由
- [ ] 路由启用/禁用
- [ ] 插件选择 + 配置
- [ ] 插件优先级排序
- [ ] 表单校验（必填项）
- [ ] 路径匹配规则预览

### 8.2 兼容性测试

- [ ] 旧数据兼容（predicateIds/filterIds）
- [ ] 新数据格式（matching/plugins）
- [ ] API 版本兼容

### 8.3 性能测试

- [ ] 大量插件加载性能
- [ ] 表单渲染性能
- [ ] 列表分页性能

---

## 9. 联系与参考

- 设计文档: `docs/route-config-design.md`
- APISIX Dashboard: https://github.com/apache/apisix-dashboard
- MyBatis-Flex: https://mybatis-flex.com/

---

**最后更新**: 2026-04-01

**完成进度**: 约 93%

**下一步**: 执行数据库初始化 SQL，验证端到端流程