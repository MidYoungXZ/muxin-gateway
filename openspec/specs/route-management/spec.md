## Purpose
Route management REST API in gateway-admin, providing CRUD operations for route configurations with predicate persistence, plugin association, and config refresh.

## Requirements

### Requirement: Route CRUD operations
The system SHALL provide REST API for creating, reading, updating, and deleting route configurations. All mutations SHALL be transactional.

#### Scenario: Create route with unique ID
- **WHEN** `POST /api/routes` is called with `RouteCreateDTO` containing a unique `routeId`
- **THEN** the system SHALL create a `gw_route` record and return HTTP 200 with the new route ID

#### Scenario: Reject duplicate route ID
- **WHEN** `POST /api/routes` is called with a `routeId` that already exists
- **THEN** the system SHALL return a 400 error with message "路由ID已存在"

#### Scenario: Soft delete route
- **WHEN** `DELETE /api/routes/{id}` is called
- **THEN** the system SHALL remove the route and its plugin associations, then trigger config refresh

### Requirement: Predicate persistence from matching config
The system SHALL convert the `matching` field from `RouteCreateDTO` into `gw_predicate` + `gw_route_predicate` records. Each matching type (path, methods, hosts, headers, queries) SHALL create a separate predicate record.

#### Scenario: Path matching creates PATH predicate
- **WHEN** a route is created with `matching.path.pattern: "/api/**"` and `matching.path.matchType: "ANT"`
- **THEN** the system SHALL insert a `gw_predicate` with `predicate_type: "PATH"` and `args: {"pattern":"/api/**","matchType":"ANT"}`, and link it via `gw_route_predicate`

#### Scenario: Method matching creates METHOD predicate
- **WHEN** a route is created with `matching.methods: ["GET", "POST"]`
- **THEN** the system SHALL insert a `gw_predicate` with `predicate_type: "METHOD"` and `args: {"methods":["GET","POST"]}`

#### Scenario: Optional matching fields
- **WHEN** `matching.hosts`, `matching.headers`, or `matching.queries` are not provided
- **THEN** the system SHALL skip creating predicates for those types

### Requirement: Plugin association with route
The system SHALL associate plugins with routes via `gw_route_plugin` records, storing per-route config overrides.

#### Scenario: Associate plugin with config override
- **WHEN** a route is created with `plugins: [{pluginId: 1, config: {rate: 100}, enabled: true}]`
- **THEN** the system SHALL insert a `gw_route_plugin` record with `config: {"rate":100}` linking the route to plugin ID 1

#### Scenario: Skip non-existent plugin
- **WHEN** a plugin ID in the request does not exist in `gw_plugin`
- **THEN** the system SHALL log a warning and skip that plugin association without failing the request

### Requirement: Config refresh after mutation
The system SHALL trigger gateway-core config refresh after any route mutation (create, update, delete, enable, disable).

#### Scenario: Refresh triggered after create
- **WHEN** `RouteServiceImpl.createRoute()` completes successfully
- **THEN** `configRefreshService.refreshRoutes()` SHALL be called to propagate changes to gateway-core

### Requirement: Route enable and disable
The system SHALL support enabling and disabling individual routes without deletion.

#### Scenario: Disable route
- **WHEN** `POST /api/routes/{id}/disable` is called
- **THEN** the route's `enabled` field SHALL be set to `false` and config refresh SHALL be triggered

### Requirement: Route update cleans up old associations
The system SHALL clean up old plugin associations before saving new ones during route update.

#### Scenario: Update replaces plugins
- **WHEN** `PUT /api/routes/{id}` is called with new plugin list
- **THEN** the system SHALL first delete all existing `gw_route_plugin` records for the route, then insert new associations

### Requirement: Paginated route listing
The system SHALL support paginated route listing with optional filters by route name, URI, and enabled status.

#### Scenario: Filter by route name
- **WHEN** `GET /api/routes?routeName=user` is called
- **THEN** the system SHALL return paginated results where `route_name` contains "user"
