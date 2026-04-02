## Why

当前代码存在以下问题：
1. **Mapper 层使用注解方式**：15 个方法使用 @Select/@Delete 注解，不符合 MyBatis-Flex 最佳实践，难以维护和扩展
2. **魔法值 Map 泛滥**：Service 层和 Provider 层大量使用 `Map<String, Object>` 传递数据，存在 20+ 处字符串 key 硬编码，容易出错且难以重构
3. **潜在 bug**：发现 SQL 别名与 Java 代码 key 不匹配的问题，可能导致运行时空指针或取不到值

这些问题增加了代码维护成本和出错风险，需要系统性地重构。

## What Changes

- **Mapper 层重构**：将所有 @Select/@Delete 注解方法改为 MyBatis-Flex QueryWrapper 链式查询或 BaseMapper 内置方法
- **新增 VO 类**：为多表关联查询结果创建专用 VO 类，替代 `Map<String, Object>` 返回类型
- **Service 层优化**：消除所有数据库结果 Map 的魔法值访问，改为强类型 VO
- **Provider 层优化**：DatabaseRouteConfigProvider 使用常量类管理配置参数 key
- **Bug 修复**：修复 SQL 别名与 key 不匹配的问题

## Capabilities

### New Capabilities

_(无新增能力)_

### Modified Capabilities

- `route-management`: Mapper 注解重构、Service 魔法值消除
- `service-management`: Mapper 注解重构、Service 魔法值消除
- `plugin-management`: Mapper 注解重构
- `auth-rbac`: PermissionMapper 注解重构

## Impact

- **gateway-admin 模块**：
  - 7 个 Mapper 文件重构
  - 3 个 Service 实现类优化
  - 新增 5+ 个 VO 类
  
- **gateway-main 模块**：
  - DatabaseRouteConfigProvider 优化
  - 新增常量类

- **向后兼容**：所有公共 API 保持不变，仅重构内部实现