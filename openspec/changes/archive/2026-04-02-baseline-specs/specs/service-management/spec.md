## 新增需求

### 需求：服务节点 CRUD
系统应提供管理服务节点实例的 REST API。每个节点属于由 `service_name` 标识的服务。

#### 场景：创建服务节点
- **WHEN** 使用包含 `serviceName`、`address`、`port` 的 `ServiceNodeCreateDTO` 调用 `POST /api/nodes`
- **THEN** 系统应插入 `gw_service_node` 记录并返回新的节点 ID

#### 场景：删除节点
- **WHEN** 调用 `DELETE /api/nodes/{id}`
- **THEN** 系统应移除节点并触发服务配置刷新

### 需求：服务分组与统计
系统应按 `service_name` 分组节点并提供聚合统计（总节点数、健康数量）。

#### 场景：列出服务分组
- **WHEN** 调用 `GET /api/nodes/services`
- **THEN** 系统应返回按服务名称分组的 `ServiceStatsVO` 列表，每项包含总节点数和健康节点数

#### 场景：列出服务的节点
- **WHEN** 调用 `GET /api/nodes/services/{serviceName}/nodes`
- **THEN** 系统应返回属于该服务的所有节点

### 需求：从注册中心创建服务
系统应支持通过从 Nacos 注册中心发现节点来创建服务。

#### 场景：从 Nacos 发现并创建
- **WHEN** 使用包含 `serviceName` 和发现配置的 `ServiceCreateDTO` 调用 `POST /api/nodes/services`
- **THEN** 系统应查询 Nacos，为每个发现的实例创建 `gw_service_node`，并触发配置刷新

#### 场景：测试发现连接
- **WHEN** 使用 Nacos 服务器地址调用 `POST /api/nodes/discovery/test`
- **THEN** 系统应测试连接并返回成功/失败结果

### 需求：删除服务及所有节点
系统应支持删除整个服务及其所有关联节点。

#### 场景：级联删除服务
- **WHEN** 调用 `DELETE /api/nodes/services/{serviceName}`
- **THEN** 系统应删除所有具有该 `service_name` 的 `gw_service_node` 记录并触发配置刷新

### 需求：节点健康检查配置
系统应允许为每个节点配置健康检查参数：间隔、超时、路径、预期状态码。

#### 场景：健康检查配置
- **WHEN** 创建节点时传入 `healthCheckEnabled: true`、`healthCheckPath: "/health"`、`healthCheckInterval: 30`
- **THEN** 系统应将这些参数存储在 `gw_service_node` 中，用于未来的健康监控

### 需求：节点状态管理
系统应支持对单个节点启用、禁用和设置维护模式。

#### 场景：维护模式
- **WHEN** 调用 `POST /api/nodes/{id}/maintenance`
- **THEN** 节点状态应设置为维护模式并触发配置刷新
