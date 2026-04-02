## MODIFIED Requirements

### Requirement: Service node Mapper implementation
ServiceNodeMapper SHALL use QueryWrapper or BaseMapper methods instead of @Select annotations.

#### Scenario: Service stats query
- **WHEN** querying service statistics
- **THEN** the implementation SHALL use QueryWrapper with groupBy in Service layer, returning ServiceStatsVO

#### Scenario: Service names query
- **WHEN** querying distinct service names
- **THEN** the implementation SHALL use QueryWrapper.select(SERVICE_NAME).distinct() in Service layer

## ADDED Requirements

### Requirement: ServiceStatsVO class
A dedicated VO class SHALL be created for service statistics results.

#### Scenario: ServiceStatsVO fields
- **WHEN** creating ServiceStatsVO
- **THEN** it SHALL contain fields: serviceName, totalNodes, healthyNodes, unhealthyNodes, enabledNodes, disabledNodes, maintenanceNodes

### Requirement: No magic values in service statistics
ServiceNodeServiceImpl SHALL use ServiceStatsVO instead of Map<String, Object> for statistics results.

#### Scenario: getServiceStats returns VO
- **WHEN** getServiceStats() is called
- **THEN** it SHALL return List<ServiceStatsVO> instead of List<Map<String, Object>>

### Requirement: Bug fix for route query by service
The findRoutesByServiceName query result access SHALL use correct keys matching SQL aliases.

#### Scenario: Route simple VO
- **WHEN** querying routes by service name
- **THEN** results SHALL be mapped to RouteSimpleVO to avoid key mismatch issues