## 修改需求

### 需求：Service node Mapper 实现
ServiceNodeMapper 应使用 QueryWrapper 或 BaseMapper 方法，而非 @Select 注解。

#### 场景：服务统计查询
- **WHEN** 查询服务统计数据
- **THEN** 实现应在 Service 层使用带 groupBy 的 QueryWrapper，返回 ServiceStatsVO

#### 场景：服务名称查询
- **WHEN** 查询去重的服务名称
- **THEN** 实现应在 Service 层使用 QueryWrapper.select(SERVICE_NAME).distinct()

## 新增需求

### 需求：ServiceStatsVO 类
应为服务统计结果创建专用的 VO 类。

#### 场景：ServiceStatsVO 字段
- **WHEN** 创建 ServiceStatsVO
- **THEN** 应包含字段：serviceName、totalNodes、healthyNodes、unhealthyNodes、enabledNodes、disabledNodes、maintenanceNodes

### 需求：服务统计中不使用魔法值
ServiceNodeServiceImpl 应使用 ServiceStatsVO 而非 Map<String, Object> 作为统计结果。

#### 场景：getServiceStats 返回 VO
- **WHEN** 调用 getServiceStats()
- **THEN** 应返回 List<ServiceStatsVO> 而非 List<Map<String, Object>>

### 需求：修复按服务查询路由的 Bug
findRoutesByServiceName 查询结果访问应使用与 SQL 别名匹配的正确键。

#### 场景：路由简单 VO
- **WHEN** 按服务名称查询路由
- **THEN** 结果应映射为 RouteSimpleVO 以避免键不匹配问题
