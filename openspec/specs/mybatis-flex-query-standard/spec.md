## 目的

定义 Muxin Gateway 项目中 MyBatis-Flex 查询操作的标准实现规范，确保所有数据库查询代码遵循统一的最佳实践，提高代码质量、可维护性和安全性。

## 需求

### 需求：QueryWrapper 构建必须使用 TableDef 字段定义
所有 QueryWrapper 的 select、from、where、orderBy 等方法必须使用自动生成的 TableDef 类中的字段定义，禁止使用字符串拼接。

#### 场景：使用 TableDef 字段进行单表查询
- **WHEN** 构建单表查询 QueryWrapper
- **THEN** 必须使用 `GW_ROUTE.ROUTE_NAME.like(value)` 而非 `wrapper.where("route_name like ?", value)`

#### 场景：使用 TableDef 字段进行多表关联
- **WHEN** 构建多表关联查询
- **THEN** 必须使用 `leftJoin(SYS_USER_ROLE).on(SYS_USER.ID.eq(SYS_USER_ROLE.USER_ID))` 而非字符串拼接 SQL

### 需求：动态条件必须使用内置机制
所有动态查询条件必须使用 MyBatis-Flex 内置的动态条件机制（第二个参数为 boolean 表达式控制条件生效），禁止使用 if 语句后手动添加条件。

#### 场景：动态条件参数为 null 时跳过条件
- **WHEN** 构建查询时 `dto.getName() == null`
- **THEN** 条件 `USER.NAME.like(dto.getName(), dto.getName() != null)` 不应被添加到 SQL 中

#### 场景：动态条件参数有值时应用条件
- **WHEN** 构建查询时 `dto.getName() == "test"`
- **THEN** 条件 `USER.NAME.like(dto.getName(), dto.getName() != null)` 应生成 `WHERE name LIKE 'test'`

#### 场景：多个动态条件组合
- **WHEN** 构建查询时有多个可选条件
- **THEN** 所有条件应使用链式调用：`.where(USER.NAME.like(name, name != null)).and(USER.AGE.gt(age, age != null))`

### 需求：优先使用 Lambda 写法（简单场景）
在单表简单查询场景，应优先使用 Lambda 写法而非静态 TableDef 字段引用，提高代码可读性和类型安全。

#### 场景：单表简单查询使用 Lambda
- **WHEN** 构建单表查询且无复杂条件组合
- **THEN** 应使用 `QueryWrapper.create().where(User::getName).like("test")` 而非 `USER.NAME.like("test")`

#### 场景：多表关联查询使用 TableDef
- **WHEN** 构建多表关联查询或有复杂条件组合
- **THEN** 应使用静态 TableDef 字段引用而非 Lambda 写法

### 需求：分页查询必须使用 paginate 方法
所有分页查询必须统一使用 Mapper 的 `paginate(pageNo, pageSize, query)` 方法，禁止使用其他分页方式。

#### 场景：分页查询使用 paginate
- **WHEN** 需要分页查询数据
- **THEN** 必须使用 `userMapper.paginate(pageNo, pageSize, query)` 而非手动计算 offset/limit

#### 场景：分页查询返回 Page 对象
- **WHEN** 执行分页查询
- **THEN** 返回的 `Page<User>` 对象必须包含 `records`、`totalRow`、`totalPage` 等完整信息

### 需求：逻辑删除字段必须手动过滤
所有查询必须手动添加 `WHERE deleted = 0` 条件（或使用 TableDef 的 `DELETED.eq(false)`），MyBatis-Flex 不会自动处理逻辑删除。

#### 场景：查询时过滤已删除记录
- **WHEN** 执行任何查询操作
- **THEN** 必须添加条件 `.where(USER.DELETED.eq(0)` 或 `.where(USER.DELETED.eq(false))`

#### 场景：统计记录数时过滤已删除记录
- **WHEN** 执行 count 查询
- **THEN** 必须添加逻辑删除过滤条件

### 需求：Mapper 层禁止使用注解 SQL（特殊情况除外）
Mapper 接口层禁止使用 `@Select`、`@Update`、`@Insert`、`@Delete` 注解定义 SQL，除非遇到复杂多表关联且无法用 QueryWrapper 表达的特殊情况。

#### 场景：禁止使用 @Select 注解
- **WHEN** 定义 Mapper 方法
- **THEN** 应继承 `BaseMapper<T>` 并使用 QueryWrapper 查询，而非使用 `@Select("SELECT * FROM user")`

#### 场景：特殊情况允许注解 SQL
- **WHEN** 需要执行无法用 QueryWrapper 表达的复杂 SQL（如动态表名、复杂聚合）
- **THEN** 允许使用注解 SQL，但必须在代码注释中说明理由

### 需求：时间字段必须手动设置
所有实体的 `create_time` 和 `update_time` 字段必须手动设置为 `LocalDateTime.now()`，MyBatis-Flex 不会自动填充。

#### 场景：创建记录时设置时间
- **WHEN** 插入新记录
- **THEN** 必须手动设置 `entity.setCreateTime(LocalDateTime.now())` 和 `entity.setUpdateTime(LocalDateTime.now())`

#### 场景：更新记录时设置时间
- **WHEN** 更新记录
- **THEN** 必须手动设置 `entity.setUpdateTime(LocalDateTime.now())`

### 需求：禁止字符串拼接 SQL
所有 SQL 查询必须使用 QueryWrapper 或 QueryChain 构建，禁止手动拼接 SQL 字符串（包括字段名、表名、条件等）。

#### 场景：禁止拼接字段名
- **WHEN** 构建查询
- **THEN** 必须使用 `USER.NAME.like(value)` 而非 `wrapper.where("name = '" + value + "'")`

#### 场景：禁止拼接表名
- **WHEN** 构建 from 子句
- **THEN** 必须使用 `.from(USER)` 而非 `.from("user")`

#### 场景：禁止拼接排序字段
- **WHEN** 构建排序条件
- **THEN** 必须使用 `.orderBy(USER.CREATE_TIME.desc())` 而非 `.orderBy("create_time desc")`

### 需求：like 查询通配符处理
like 查询不自动添加通配符，需要前后通配符时由调用方在参数中添加，保持灵活性。

#### 场景：前缀匹配
- **WHEN** 需要前缀匹配查询
- **THEN** 调用方应传递 `"test%"` 作为参数而非依赖框架自动添加

#### 场景：包含匹配
- **WHEN** 需要包含匹配查询
- **THEN** 调用方应传递 `"%" + keyword + "%" ` 作为参数

#### 场景：精确匹配
- **WHEN** 需要精确匹配查询
- **THEN** 可直接传递精确值 `"test"` 作为参数

### 需求：复杂 SQL 使用 join + wrapper
复杂多表关联查询应使用 QueryWrapper 的 join 语法而非 XML SQL 文件，保持代码统一性。

#### 场景：多表关联使用 inner join
- **WHEN** 需要关联多表查询
- **THEN** 应使用 `.innerJoin(ORDER).on(USER.ID.eq(ORDER.USER_ID))` 而非 XML SQL

#### 场景：使用 Db + Row 处理简单返回类型
- **WHEN** 多表关联查询只需返回部分字段且无需 Entity 映射
- **THEN** 应使用 `Db.selectListByQuery(wrapper)` 返回 `List<Row>` 并手动提取字段