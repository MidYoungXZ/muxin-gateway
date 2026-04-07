## 背景

MyBatis-Flex 提供了强大的 QueryWrapper 链式查询能力，相比注解方式更灵活、类型安全、易于维护。当前项目 7 个 Mapper 文件中的 15 个方法使用了 @Select/@Delete 注解，需要统一重构为 QueryWrapper 方式。

同时，Service 层和 Provider 层大量使用 `Map<String, Object>` 传递查询结果，存在 20+ 处字符串 key 硬编码（魔法值），降低了代码可维护性并埋下了潜在的运行时错误风险。

## 目标 / 非目标

**Goals:**
- 消除所有 Mapper 层的 @Select/@Delete/@Update/@Insert 注解，改用 QueryWrapper 或 BaseMapper 内置方法
- 消除 Service 层和 Provider 层的魔法值 Map 访问，改用强类型 VO 或常量类
- 修复发现的 SQL 别名与 key 不匹配 bug
- 保持所有公共 API 不变

**Non-Goals:**
- 不重构 Controller 层（Controller 层未发现魔法值问题）
- 不重构 Entity 和 DTO 定义
- 不改变数据库 schema
- 不优化性能（仅重构代码结构）

## 决策

### D1: Mapper 注解重构策略 — 纯 QueryWrapper

**选择**: 所有 Mapper 注解方法改用 MyBatis-Flex QueryWrapper 链式查询

**理由**:
- 用户明确选择纯 QueryWrapper 方式
- QueryWrapper 提供类型安全和 IDE 自动补全支持
- 避免引入 XML 文件增加复杂度

**替代方案**: 混合方式（简单查询用 QueryWrapper，复杂关联用 XML）—— 未选择

**实现方式**:
- 简单单表查询：直接使用 BaseMapper 内置方法
- 多表关联查询：在 Service 层使用 QueryWrapper + stream 组装
- 删除操作：使用 BaseMapper 的 deleteByMap 或 deleteById

### D2: Map 返回类型替换 — 创建专用 VO 类

**选择**: 为所有 Mapper 返回 `Map<String, Object>` 的方法创建专用 VO 类

**新增 VO 类**:
| VO 类名 | 用途 | 原方法 |
|---------|------|--------|
| RoutePluginDetailVO | 路由插件详情（含插件基础信息） | RoutePluginMapper.findPluginsByRouteId |
| RoutePredicateDetailVO | 路由断言详情 | RoutePredicateMapper.findPredicatesByRouteId |
| RouteSimpleVO | 路由简要信息 | RoutePredicateMapper.findRoutesByPredicateId, RouteMapper.findRoutesByServiceName |
| ServiceStatsVO | 服务统计信息 | ServiceNodeMapper.selectServiceStats |

**理由**:
- 强类型替代 Map，编译期检查 key 正确性
- IDE 自动补全支持
- 便于后续扩展和维护

### D3: 配置参数 key 常量化

**选择**: 创建 `PluginConfigKeys` 常量类，统一管理插件配置参数 key

**常量类定义**:
```java
public final class PluginConfigKeys {
    // 谓词配置
    public static final String PATTERN = "pattern";
    public static final String MATCH_TYPE = "matchType";
    public static final String IGNORE_CASE = "ignoreCase";
    public static final String METHODS = "methods";
    public static final String HOSTS = "hosts";
    public static final String HEADERS = "headers";
    public static final String QUERIES = "queries";
    
    // 插件配置
    public static final String PATH_REGEX = "pathRegex";
    public static final String PATH_REPLACEMENT = "pathReplacement";
    public static final String CONNECT_TIMEOUT = "connectTimeout";
    public static final String RESPONSE_TIMEOUT = "responseTimeout";
    // ... 其他配置参数
}
```

**理由**: 配置参数 key 属于跨层使用的字符串常量，集中管理便于维护

### D4: 多表关联查询实现 — Service 层组装

**选择**: 复杂多表关联查询在 Service 层通过多次查询 + stream 组装实现

**示例**: RoutePluginMapper.findPluginsByRouteId 改造
```java
// 原注解 SQL 关联 gw_route_plugin 和 gw_plugin 表
// 改造后：Service 层先查 route_plugin 列表，再批量查 plugin，stream 组装
```

**理由**:
- 避免 XML 映射文件
- 保持代码可读性和可维护性
- MyBatis-Flex 的 QueryWrapper 不直接支持 JOIN，需手动组装

## Risks / Trade-offs

- **[R1]** 多表关联改用 Service 层组装可能增加数据库查询次数 → 使用 IN 批量查询优化，N+1 问题可控
- **[R2]** 大规模重构可能引入新 bug → 每个改造点编写单元测试验证，分批提交
- **[R3]** 常量类过度膨胀 → 按模块拆分多个常量类（PredicateConfigKeys、FilterConfigKeys 等）

## Implementation Notes

### 已发现的 Bug

1. **DatabaseRouteConfigProvider:180** — `map.get("default_config")` 访问不存在的 key
   - RoutePluginMapper SQL 未返回 `default_config` 字段
   - 修复：修改 SQL 或删除该访问

2. **ServiceNodeServiceImpl:376-379** — SQL 别名与 key 不匹配
   - SQL 使用 `route_id as routeId`，但 key 访问使用驼峰
   - 实际 SQLite 返回的是 `routeId`（取决于 JDBC 实现）
   - 修复：改用 VO 后自动解决