## 修改需求

### 需求：Mapper 层实现
Mapper 层应使用 MyBatis-Flex QueryWrapper 或 BaseMapper 内置方法进行数据库操作。不允许使用 @Select、@Delete、@Update、@Insert 注解，团队批准的特殊情况除外。

#### 场景：简单查询使用 BaseMapper
- **WHEN** 需要简单的单表查询（如按 ID 查找、按名称查找）
- **THEN** 实现应使用 BaseMapper 内置方法（selectOneById、selectListByQuery 等）

#### 场景：复杂查询使用 QueryWrapper
- **WHEN** 需要带条件或关联的查询
- **THEN** 实现应在 Service 层使用 QueryWrapper 链式方法

#### 场景：删除操作使用 BaseMapper
- **WHEN** 需要执行删除操作
- **THEN** 实现应使用 BaseMapper 的 deleteByMap、deleteById 或 deleteBatch 方法

### 需求：强类型返回值
Mapper 方法不应返回 `Map<String, Object>`。所有多表查询结果应定义为专用的 VO 类。

#### 场景：多表查询返回 VO
- **WHEN** Mapper 方法查询多表
- **THEN** 应返回专用 VO 类的列表（如 RoutePluginDetailVO、RoutePredicateDetailVO）

#### 场景：VO 类命名
- **WHEN** 为 Mapper 查询结果创建 VO 类
- **THEN** 类名应遵循 `<Entity>DetailVO` 或 `<Entity>SimpleVO` 模式

## 新增需求

### 需求：不使用魔法值字符串
Service 层和 Provider 层不应使用硬编码的字符串键来访问 Map 值。所有配置键应定义为常量。

#### 场景：使用常量作为配置键
- **WHEN** 从 Map 中访问配置值
- **THEN** 键应从常量类中引用（如 PluginConfigKeys.PATTERN）

#### 场景：数据库结果使用 VO
- **WHEN** 处理数据库查询结果
- **THEN** 应通过 VO getter 方法访问结果，而非 Map.get("key")

### 需求：配置键常量
所有配置参数键应集中在常量类中，以提高可维护性。

#### 场景：断言配置键
- **WHEN** 构建断言参数
- **THEN** "pattern"、"matchType"、"methods" 等键应定义在 PredicateConfigKeys 类中

#### 场景：插件配置键
- **WHEN** 构建插件配置
- **THEN** "pathRegex"、"connectTimeout" 等键应定义在 PluginConfigKeys 类中
