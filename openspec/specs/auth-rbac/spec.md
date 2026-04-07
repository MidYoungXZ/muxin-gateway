## 目的
使用 Sa-Token 和 JWT 实现认证、授权和 RBAC 系统，支持基于角色的访问控制、基于菜单的动态路由、数据范围过滤、部门层级和操作审计日志。

## 需求

### 需求：JWT token 认证
系统应通过 Sa-Token 和 JWT 认证用户。登录时应签发 token，后续请求应通过 `Authorization: Bearer <token>` 请求头验证。

#### 场景：登录成功
- **WHEN** `POST /api/auth/login` 使用有效的 `username` 和 `password` 调用
- **THEN** 系统应返回 JWT token，包含 `tokenType: "Bearer"` 和用户信息

#### 场景：凭证无效
- **WHEN** 使用错误密码尝试登录
- **THEN** 系统应返回 401 和错误信息

#### 场景：Token 刷新
- **WHEN** `POST /api/auth/refresh-token` 使用有效的 refresh token 调用
- **THEN** 系统应签发新的 access token

### 需求：基于角色的访问控制 (RBAC)
系统应执行 RBAC，关系为：用户 → 角色 → 菜单/权限。权限格式为 `{module}:{entity}:{action}`（例如 `route:create`、`system:user:list`）。

#### 场景：API 调用权限检查
- **WHEN** 用户调用 `POST /api/routes` 但没有 `route:create` 权限
- **THEN** 系统应返回 403 Forbidden

#### 场景：用户拥有多个角色
- **WHEN** 用户拥有角色 "admin" 和 "viewer"
- **THEN** 用户应拥有两个角色所有权限的并集

### 需求：基于菜单的动态路由
系统应加载分配给用户角色的菜单，并将其暴露为菜单树。菜单类型：M（目录）、C（页面）、F（按钮权限）。

#### 场景：加载用户菜单树
- **WHEN** 已认证用户调用 `GET /api/menus/user-tree`
- **THEN** 系统应仅返回分配给用户角色的菜单

#### 场景：按钮级权限
- **WHEN** 调用 `GET /api/menus/user-permissions`
- **THEN** 系统应返回当前用户所有 F 类型菜单权限（例如 `route:create`、`system:user:delete`）

### 需求：五级数据范围过滤
系统应支持每个角色的 5 种数据范围级别，通过 `@DataScope` 注解 + AOP 执行：

| 范围 | 规则 |
|-------|------|
| 1 - 全部数据 | 无过滤 |
| 2 - 自定义 | 按 `sys_role_dept` 中的部门过滤 |
| 3 - 本部门 | 按用户所在部门过滤 |
| 4 - 本部门及子部门 | 按用户部门及所有子部门过滤 |
| 5 - 仅本人 | 按用户自己的 ID 过滤 |

#### 场景：数据范围本部门过滤
- **WHEN** 拥有 `data_scope: 3` 的用户查询用户列表
- **THEN** SQL 应包含 `WHERE dept_id = {currentUser.deptId}`

#### 场景：数据范围自定义过滤
- **WHEN** 拥有 `data_scope: 2` 和自定义部门 `[1, 5, 8]` 的用户查询数据
- **THEN** SQL 应包含 `WHERE dept_id IN (1, 5, 8)`

### 需求：部门层级管理
系统应支持树形结构的部门，包含 `parent_id` 和 `ancestors`（逗号分隔路径）字段。

#### 场景：部门树查询
- **WHEN** 调用 `GET /api/dept/tree`
- **THEN** 系统应返回包含父子关系的完整部门树

#### 场景：移动部门
- **WHEN** 调用 `PUT /api/dept/{id}/move/{targetParentId}`
- **THEN** 部门应移动到新父级下，并更新 `ancestors` 路径

### 需求：操作审计日志
系统应通过 `@OperationLog` 注解记录管理操作，捕获：模块、操作、方法、请求 URL、参数、结果、耗时、操作人、IP、浏览器、操作系统。

#### 场景：操作后记录日志
- **WHEN** 用户创建路由
- **THEN** 应插入一条 `sys_operation_log` 记录，包含操作详情和当前用户信息