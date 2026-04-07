## 新增需求

### 需求：JWT token 认证
系统应通过 Sa-Token + JWT 认证用户。登录时颁发 token，后续请求通过 `Authorization: Bearer <token>` 头部验证。

#### 场景：登录成功
- **WHEN** 使用有效的 `username` 和 `password` 调用 `POST /api/auth/login`
- **THEN** 系统应返回 JWT token，包含 `tokenType: "Bearer"` 和用户信息

#### 场景：凭据无效
- **WHEN** 使用错误密码尝试登录
- **THEN** 系统应返回 401 并附带错误消息

#### 场景：刷新 token
- **WHEN** 使用有效的 refresh token 调用 `POST /api/auth/refresh-token`
- **THEN** 系统应颁发新的 access token

### 需求：基于角色的访问控制（RBAC）
系统应实施 RBAC：用户 → 角色 → 菜单/权限。权限格式为 `{module}:{entity}:{action}`（如 `route:create`、`system:user:list`）。

#### 场景：API 调用权限检查
- **WHEN** 用户在没有 `route:create` 权限的情况下调用 `POST /api/routes`
- **THEN** 系统应返回 403 Forbidden

#### 场景：具有多个角色的用户
- **WHEN** 用户拥有 "admin" 和 "viewer" 角色
- **THEN** 用户应拥有两个角色所有权限的并集

### 需求：基于菜单的动态路由
系统应加载分配给用户角色的菜单并以菜单树形式暴露。菜单类型：M（目录）、C（页面）、F（按钮权限）。

#### 场景：加载用户菜单树
- **WHEN** 认证用户调用 `GET /api/menus/user-tree`
- **THEN** 系统应仅返回分配给用户角色的菜单

#### 场景：按钮级权限
- **WHEN** 调用 `GET /api/menus/user-permissions`
- **THEN** 系统应返回当前用户的所有 F 类菜单权限（如 `route:create`、`system:user:delete`）

### 需求：五级数据范围过滤
系统应支持每个角色 5 个数据范围级别，通过 `@DataScope` 注解 + AOP 实施：

| 范围 | 规则 |
|-------|------|
| 1 - ALL | 无过滤 |
| 2 - CUSTOM | 按 `sys_role_dept` 中的部门过滤 |
| 3 - DEPT | 按用户所在部门过滤 |
| 4 - DEPT_AND_CHILDREN | 按用户部门及所有子部门过滤 |
| 5 - SELF_ONLY | 按用户自身 ID 过滤 |

#### 场景：数据范围 DEPT 过滤
- **WHEN** 具有 `data_scope: 3` 的用户查询用户列表
- **THEN** SQL 应包含 `WHERE dept_id = {currentUser.deptId}`

#### 场景：数据范围 CUSTOM 过滤
- **WHEN** 具有 `data_scope: 2` 且自定义部门 `[1, 5, 8]` 的用户查询数据
- **THEN** SQL 应包含 `WHERE dept_id IN (1, 5, 8)`

### 需求：部门层级管理
系统应支持树形结构的部门，使用 `parent_id` 和 `ancestors`（逗号分隔路径）字段。

#### 场景：部门树查询
- **WHEN** 调用 `GET /api/dept/tree`
- **THEN** 系统应返回包含父子关系的完整部门树

#### 场景：移动部门
- **WHEN** 调用 `PUT /api/dept/{id}/move/{targetParentId}`
- **THEN** 部门应移动到新父级下，`ancestors` 路径应更新

### 需求：操作审计日志
系统应通过 `@OperationLog` 注解记录管理操作，捕获：模块、操作、方法、请求 URL、参数、结果、耗时、操作者、IP、浏览器、操作系统。

#### 场景：操作后记录日志
- **WHEN** 用户创建路由
- **THEN** 应插入一条 `sys_operation_log` 记录，包含操作详情和当前用户信息
