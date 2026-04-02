## Why

当前后端代码中的 MyBatis-Flex 使用方式不完全符合 AGENTS.md 中定义的最新规范。主要问题包括：字符串拼接 SQL、动态条件使用传统的 if 判断而非 QueryWrapper 内置机制、未充分利用 Lambda 写法等。这些问题会导致代码可维护性降低、SQL 注入风险增加、性能优化受限。需要重构以确保所有数据库查询操作严格遵循规范，提高代码质量和一致性。

## What Changes

- 重构所有 Service 实现类中的 QueryWrapper 构建方式
- 移除字符串拼接 SQL，改为使用 TableDef 字段定义
- 使用 MyBatis-Flex 内置动态条件机制（第二个参数控制条件生效）
- 优先使用 Lambda 写法替代静态字段引用
- 优化 like 查询，使用自动通配符处理
- 确保所有分页查询统一使用 `paginate` 方法
- 确保所有查询都正确处理逻辑删除（deleted = 0）
- 统一时间字段的手动设置方式

## Capabilities

### New Capabilities

- `mybatis-flex-query-standard`: 定义 MyBatis-Flex 查询操作的标准实现规范，包括 QueryWrapper 构建模式、动态条件处理、Lambda 写法等最佳实践

### Modified Capabilities

- `route-management`: 重构 RouteServiceImpl 的查询方法，使其符合最新的 MyBatis-Flex 规范
- `service-management`: 重构 ServiceNodeServiceImpl 的查询方法，使其符合最新的 MyBatis-Flex 规范
- `plugin-management`: 重构 PluginServiceImpl 的查询方法，使其符合最新的 MyBatis-Flex 规范
- `auth-rbac`: 重构 UserServiceImpl、RoleServiceImpl、MenuServiceImpl、DeptServiceImpl 等认证授权相关服务的查询方法，使其符合最新的 MyBatis-Flex 规范

## Impact

- 受影响代码：gateway-admin 模块下的所有 Service 实现类（约 10+ 个文件）
- 主要涉及的类：
  - RouteServiceImpl
  - ServiceNodeServiceImpl
  - PluginServiceImpl
  - UserServiceImpl
  - RoleServiceImpl
  - MenuServiceImpl
  - DeptServiceImpl
  - AuthServiceImpl
  - ConfigServiceImpl
  - OperationLogServiceImpl
- 数据库层面：无需修改，查询逻辑不变，仅重构代码实现方式
- API 层面：无影响，对外接口保持不变
- 性能影响：正面影响，动态条件处理更高效，避免不必要的 SQL 片段拼接
- 破坏性变更：无，纯代码重构，不影响功能