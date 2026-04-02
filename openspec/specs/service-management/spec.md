## Purpose
Service node management REST API in gateway-admin, providing CRUD operations for service instances with grouping, health check configuration, and registry discovery.

## Requirements

### Requirement: Service node CRUD
The system SHALL provide REST API for managing service node instances. Each node belongs to a service identified by `service_name`.

#### Scenario: Create service node
- **WHEN** `POST /api/nodes` is called with `ServiceNodeCreateDTO` containing `serviceName`, `address`, `port`
- **THEN** the system SHALL insert a `gw_service_node` record and return the new node ID

#### Scenario: Delete node
- **WHEN** `DELETE /api/nodes/{id}` is called
- **THEN** the system SHALL remove the node and trigger service config refresh

### Requirement: Service grouping with statistics
The system SHALL group nodes by `service_name` and provide aggregated statistics (total nodes, healthy count).

#### Scenario: List service groups
- **WHEN** `GET /api/nodes/services` is called
- **THEN** the system SHALL return a list of `ServiceStatsVO` grouped by service name, each containing total node count and healthy node count

#### Scenario: List nodes for a service
- **WHEN** `GET /api/nodes/services/{serviceName}/nodes` is called
- **THEN** the system SHALL return all nodes belonging to that service

### Requirement: Service creation from registry
The system SHALL support creating a service by discovering nodes from a Nacos registry.

#### Scenario: Discover and create from Nacos
- **WHEN** `POST /api/nodes/services` is called with `ServiceCreateDTO` containing `serviceName` and discovery config
- **THEN** the system SHALL query Nacos, create a `gw_service_node` for each discovered instance, and trigger config refresh

#### Scenario: Test discovery connection
- **WHEN** `POST /api/nodes/discovery/test` is called with Nacos server address
- **THEN** the system SHALL test the connection and return success/failure result

### Requirement: Service deletion with all nodes
The system SHALL support deleting an entire service and all its associated nodes.

#### Scenario: Delete service cascade
- **WHEN** `DELETE /api/nodes/services/{serviceName}` is called
- **THEN** the system SHALL delete all `gw_service_node` records with that `service_name` and trigger config refresh

### Requirement: Node health check configuration
The system SHALL allow configuring health check parameters per node: interval, timeout, path, expected status.

#### Scenario: Health check configuration
- **WHEN** a node is created with `healthCheckEnabled: true`, `healthCheckPath: "/health"`, `healthCheckInterval: 30`
- **THEN** the system SHALL store these parameters in `gw_service_node` for future health monitoring

### Requirement: Node status management
The system SHALL support enabling, disabling, and setting maintenance mode for individual nodes.

#### Scenario: Maintenance mode
- **WHEN** `POST /api/nodes/{id}/maintenance` is called
- **THEN** the node's status SHALL be set to maintenance and config refresh SHALL be triggered
