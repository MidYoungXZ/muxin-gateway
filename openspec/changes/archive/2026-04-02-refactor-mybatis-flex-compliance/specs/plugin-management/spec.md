## ADDED Requirements

### Requirement: Plugin 查询必须符合 MyBatis-Flex 规范
PluginServiceImpl 中的所有数据库查询操作必须遵循 `mybatis-flex-query-standard` 规格定义的标准实现方式。

#### Scenario: 分页查询插件使用动态条件
- **WHEN** 分页查询插件列表
- **THEN** 必须使用 `.and(GW_PLUGIN.PLUGIN_NAME.like(dto.getPluginName(), dto.getPluginName() != null))` 而非 if 语句手动添加条件

#### Scenario: 插件查询过滤逻辑删除
- **WHEN** 查询任何插件数据
- **THEN** 必须添加条件 `.where(GW_PLUGIN.DELETED.eq(0))` 过滤已删除记录

### Requirement: Plugin 时间字段手动设置
插件实体（GwPlugin）的创建和更新操作必须手动设置 `create_time` 和 `update_time` 字段。

#### Scenario: 创建插件时设置时间
- **WHEN** 创建新插件记录
- **THEN** 必须手动设置 `plugin.setCreateTime(LocalDateTime.now())` 和 `plugin.setUpdateTime(LocalDateTime.now())`