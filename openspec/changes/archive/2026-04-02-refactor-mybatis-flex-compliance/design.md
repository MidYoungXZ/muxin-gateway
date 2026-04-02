## Context

当前 Muxin Gateway 的 gateway-admin 模块使用 MyBatis-Flex 作为 ORM 框架，但在实际代码实现中，部分查询操作未严格遵循 AGENTS.md 中定义的最新规范。主要表现在：

1. 使用字符串拼接构建 SQL（如 `.from("gw_service_node")`），而非使用 TableDef 字段定义
2. 动态条件使用传统的 `if` 判断后手动添加条件，而非 MyBatis-Flex 内置的动态条件机制
3. 部分代码未使用推荐的 Lambda 写法，降低了代码可读性
4. like 查询手动拼接通配符，而非使用自动处理机制

这些不规范的使用方式会导致：
- SQL 注入风险（虽然目前未发现安全问题）
- 代码可维护性降低，不利于团队统一理解
- 无法充分利用 MyBatis-Flex 的优化特性
- 与 AGENTS.md 规范不一致，影响 AI 辅助开发的准确性

涉及的主要服务类：
- RouteServiceImpl（路由管理）
- ServiceNodeServiceImpl（服务节点管理）
- PluginServiceImpl（插件管理）
- UserServiceImpl（用户管理）
- RoleServiceImpl（角色管理）
- MenuServiceImpl（菜单管理）
- DeptServiceImpl（部门管理）
- AuthServiceImpl（认证服务）
- ConfigServiceImpl（配置管理）
- OperationLogServiceImpl（操作日志）

## Goals / Non-Goals

**Goals:**

- 重构所有 Service 实现类的查询代码，使其完全符合 AGENTS.md 中 MyBatis-Flex 使用规范
- 移除所有字符串拼接 SQL，改用 TableDef 字段定义
- 使用 MyBatis-Flex 内置动态条件机制（条件的第二个参数）
- 在适用场景优先使用 Lambda 写法
- 统一 like 查询的通配符处理方式
- 确保所有分页查询使用 `paginate` 方法
- 保持现有功能完全不变，仅优化代码实现方式
- 提高代码可维护性和一致性

**Non-Goals:**

- 不修改数据库 schema 或表结构
- 不添加新功能或改变现有业务逻辑
- 不修改 REST API 接口签名
- 不修改前端代码
- 不进行性能优化的额外改动（如索引优化、缓存等）
- 不修改 gateway-core 或 gateway-main 模块（它们不使用 MyBatis-Flex）

## Decisions

### 决策 1：统一使用 TableDef 字段定义替代字符串拼接

**选择：** 所有 QueryWrapper 的 select、from、where 等方法都使用自动生成的 TableDef 类中的字段定义，禁止字符串拼接。

**理由：**
- 避免 SQL 注入风险（虽然参数化查询已处理，但字段名拼接仍有隐患）
- 类型安全，编译时即可发现字段名错误
- IDE 自动补全支持，提高开发效率
- 符合 MyBatis-Flex 最佳实践

**替代方案：** 继续使用字符串拼接 - 拒绝，因为不符合规范且存在潜在风险

### 决策 2：使用 MyBatis-Flex 内置动态条件机制

**选择：** 使用 `condition.eq(value, condition)` 形式的动态条件，第二个参数为 boolean 表达式控制条件是否生效。

**理由：**
- 简化代码，减少 if 语句嵌套
- QueryWrapper 链式调用更流畅
- MyBatis-Flex 内部优化更高效
- 符合规范推荐的代码模板

**替代方案：** 继续使用 if 判断后手动添加条件 - 拒绝，因为不符合规范且代码冗余

**示例对比：**

```java
// 原代码（不规范）
if (StringUtils.hasText(query.getRouteName())) {
    wrapper.and(GW_ROUTE.ROUTE_NAME.like("%" + query.getRouteName() + "%"));
}

// 新代码（规范）
wrapper.and(GW_ROUTE.ROUTE_NAME.like(query.getRouteName(), StringUtils.hasText(query.getRouteName())));
```

### 决策 3：优先使用 Lambda 写法（在适用场景）

**选择：** 在简单查询场景优先使用 Lambda 写法 `QueryWrapper.create().where(User::getAge).gt(18)`。

**理由：**
- 代码更简洁，类型安全
- 无需引入静态 TableDef 导入
- IDE 重构支持更好（字段重命名时自动更新）

**限制：** 
- 多表关联查询、复杂条件组合时仍需使用 TableDef
- Lambda 写法在某些复杂场景可读性不如 TableDef

**替代方案：** 全部使用 TableDef - 部分接受，在复杂场景使用 TableDef，简单场景使用 Lambda

### 决策 4：统一分页查询使用 `paginate` 方法

**选择：** 所有分页查询统一使用 Mapper 的 `paginate(pageNo, pageSize, query)` 方法。

**理由：**
- 符合规范要求
- 代码一致性
- MyBatis-Flex 自动处理分页逻辑

**替代方案：** 使用 `Page.of()` + `page(Page, wrapper)` - 可接受，但推荐直接使用 `paginate`

### 册策 5：like 查询的通配符处理

**选择：** 使用 MyBatis-Flex 的 `like()` 方法，不手动添加 "%" 通配符。需要前后通配符时，调用方在 DTO 中添加。

**理由：**
- MyBatis-Flex 的 `like()` 方法不会自动添加通配符，保持灵活性
- 规范未明确要求自动添加，保持语义清晰
- 调用方控制匹配方式更灵活（前缀、后缀、包含）

**替代方案：** 统一自动添加前后通配符 - 拒绝，因为限制了匹配方式的灵活性

## Risks / Trade-offs

### 风险 1：重构过程中可能引入功能缺陷

**风险描述：** 大规模代码重构可能导致查询逻辑错误，影响现有功能。

**缓解措施：**
- 逐个 Service 类重构，每次重构后运行测试
- 保持查询逻辑不变，仅修改实现方式
- 对比重构前后的 SQL 语句（通过日志或调试）
- 重点测试核心业务功能（路由管理、用户认证）

### 风险 2：团队学习成本

**风险描述：** 团队成员可能不熟悉新的写法，短期内影响开发效率。

**缓解措施：**
- 在 AGENTS.md 中已有详细规范和示例，团队可参考
- 重构后的代码更规范，长期维护成本降低
- AI 辅助开发将生成符合规范的代码，减少人工编写

### 风险 3：部分复杂查询难以完全规范化

**风险描述：** 一些复杂的多表关联查询、动态排序等场景可能难以完全应用规范。

**缓解措施：**
- 复杂查询允许使用原生 SQL 或 XML（规范允许特殊情况）
- 优先重构简单查询，复杂查询逐步优化
- 记录无法完全规范化的场景，后续评估是否需要调整规范

### 权衡 1：性能 vs 可读性

**权衡描述：** Lambda 写法可读性更好，但在某些场景性能可能略低于 TableDef（反射开销）。

**决策：** 优先考虑可读性和规范一致性，性能差异在业务系统中影响极小。在性能敏感场景（如高并发查询）可使用 TableDef。

### 权衡 2：完全重构 vs 渐进式重构

**权衡描述：** 完全一次性重构风险较高，但能快速达到目标；渐进式重构风险低，但时间跨度长。

**决策：** 采用分批渐进式重构，按模块分批（route-management → auth-rbac → plugin-management → service-management），每批完成后测试验证。

## Migration Plan

### 阶段 1：准备工作（1-2 天）

1. 确认 AGENTS.md 中 MyBatis-Flex 规范的完整性
2. 创建重构模板和示例代码
3. 准备测试用例（现有测试 + 手动验证流程）

### 阶段 2：第一批重构 - Route Management（1 天）

1. 重构 RouteServiceImpl
2. 重点修复 `getServiceNames()` 中的字符串拼接 SQL
3. 优化 `pageQuery()` 的动态条件处理
4. 测试验证：路由创建、查询、更新、删除功能

### 阶段 3：第二批重构 - Auth & RBAC（2 天）

1. 重构 UserServiceImpl、RoleServiceImpl、MenuServiceImpl、DeptServiceImpl
2. 重点修复多表关联查询（如 `getUserPermissions()`）
3. 优化动态条件处理和数据权限过滤
4. 测试验证：用户登录、权限校验、菜单加载功能

### 阶段 4：第三批重构 - Plugin & Service（1 天）

1. 重构 PluginServiceImpl、ServiceNodeServiceImpl
2. 优化查询和关联操作
3. 测试验证：插件管理、服务节点管理功能

### 阶段 5：第四批重构 - Other Services（1 天）

1. 重构 AuthServiceImpl、ConfigServiceImpl、OperationLogServiceImpl
2. 测试验证：认证流程、配置管理、日志查询功能

### 阶段 6：全面测试和验收（1-2 天）

1. 运行完整测试套件（如有）
2. 手动验证核心业务流程
3. 检查 MyBatis-Flex 生成的 SQL 日志
4. 代码 review 和规范检查

### 回滚策略

- 使用 Git 分支进行重构，每个阶段完成后提交
- 出现问题时，可快速回退到前一个稳定版本
- 保持原有代码作为备份参考

## Open Questions

1. **是否需要更新单元测试？** 
   - 当前项目可能缺乏完整单元测试，重构后是否需要补充测试用例？
   - 建议：优先依赖手动功能验证，后续逐步补充自动化测试

2. **复杂多表查询的优化边界？**
   - 一些复杂查询（如权限树构建）是否需要完全重构，还是允许保留部分传统写法？
   - 建议：优先重构简单部分，复杂查询保持功能正确性，后续逐步优化

3. **是否需要添加 MyBatis-Flex 查询拦截器进行规范检查？**
   - 是否在开发环境添加拦截器，自动检测不符合规范的查询？
   - 建议：暂不添加，依赖代码 review 和 AI 辅助生成规范代码

4. **Lambda 写法与 TableDef 的使用边界？**
   - 具体哪些场景使用 Lambda，哪些场景使用 TableDef？
   - 建议：单表简单查询使用 Lambda，多表关联或复杂条件使用 TableDef