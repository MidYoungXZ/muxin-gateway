## ADDED Requirements

### Requirement: Service 查询必须符合 MyBatis-Flex 规范
ServiceNodeServiceImpl 中的所有数据库查询操作必须遵循 `mybatis-flex-query-standard` 规格定义的标准实现方式。

#### Scenario: 查询服务节点使用动态条件
- **WHEN** 根据服务名称或其他条件查询服务节点
- **THEN** 必须使用 `.and(GW_SERVICE_NODE.SERVICE_NAME.like(dto.getServiceName(), dto.getServiceName() != null))` 而非 if 语句手动添加条件

#### Scenario: 服务节点查询过滤逻辑删除
- **WHEN** 查询任何服务节点数据
- **THEN** 必须添加条件 `.where(GW_SERVICE_NODE.DELETED.eq(0))` 过滤已删除记录

### Requirement: Service 时间字段手动设置
服务节点实体（GwServiceNode）的创建和更新操作必须手动设置 `create_time` 和 `update_time` 字段。

#### Scenario: 创建服务节点时设置时间
- **WHEN** 注册新的服务节点
- **THEN** 必须手动设置 `serviceNode.setCreateTime(LocalDateTime.now())` 和 `serviceNode.setUpdateTime(LocalDateTime.now())`