## MODIFIED Requirements

### Requirement: Route CRUD operations
The system SHALL provide REST API for creating, reading, updating, and deleting route configurations. All mutations SHALL be transactional. The `GET /api/routes/{id}` endpoint SHALL return the complete route detail including associated predicates and plugins. Permission checks SHALL use consistent permission strings matching the database seed data.

#### Scenario: Create route with unique ID
- **WHEN** `POST /api/routes` is called with `RouteCreateDTO` containing a unique `routeId`
- **THEN** the system SHALL create a `gw_route` record and return HTTP 200 with the new route ID

#### Scenario: Reject duplicate route ID
- **WHEN** `POST /api/routes` is called with a `routeId` that already exists
- **THEN** the system SHALL return a 400 error with message "路由ID已存在"

#### Scenario: Soft delete route
- **WHEN** `DELETE /api/routes/{id}` is called
- **THEN** the system SHALL remove the route and its plugin associations, then trigger config refresh

#### Scenario: Get route detail with predicates
- **WHEN** `GET /api/routes/{id}` is called and the user has `route:view` permission
- **THEN** the system SHALL return `RouteVO` including `predicates` list (each with `predicateType` and `args`), `plugins` list, and all route fields

#### Scenario: Permission string matches database
- **WHEN** a controller method requires permission via `@SaCheckPermission`
- **THEN** the permission string SHALL exactly match the corresponding `sys_menu.perms` value in the database seed data

### Requirement: Route update cleans up old associations
The system SHALL clean up old plugin associations AND predicate associations before saving new ones during route update. If `matching` is provided, new predicates SHALL be created.

#### Scenario: Update replaces plugins
- **WHEN** `PUT /api/routes/{id}` is called with new plugin list
- **THEN** the system SHALL first delete all existing `gw_route_plugin` records for the route, then insert new associations

#### Scenario: Update replaces predicates
- **WHEN** `PUT /api/routes/{id}` is called with `matching` data
- **THEN** the system SHALL first delete all existing `gw_route_predicate` + associated `gw_predicate` records for the route, then insert new predicate records via `saveRouteMatching()`

## ADDED Requirements

### Requirement: Route detail includes predicates
The `RouteVO` returned by `GET /api/routes/{id}` SHALL include a `predicates` field containing all associated predicates with their types and args, enabling the frontend to populate the edit form with existing matching rules.

#### Scenario: Predicates loaded for route detail
- **WHEN** `RouteServiceImpl.getRouteDetail()` is called for a route that has 3 predicates (PATH, METHOD, HOST)
- **THEN** the returned `RouteVO.predicates` SHALL contain 3 entries with `predicateType` and `args` fields

#### Scenario: No predicates for route
- **WHEN** `RouteServiceImpl.getRouteDetail()` is called for a route with no predicates
- **THEN** the returned `RouteVO.predicates` SHALL be an empty list (not null)

### Requirement: Load balance strategy synced to gateway-core
The `DatabaseRouteConfigProvider` SHALL read `load_balance_strategy` from `gw_route` and set it on the `RouteDefinition.loadBalance` field so that the configured strategy takes effect at runtime.

#### Scenario: Custom load balance strategy applied
- **WHEN** a route has `load_balance_strategy: "WEIGHTED"` in the database
- **THEN** `DatabaseRouteConfigProvider.convertToRouteDefinition()` SHALL create a `LoadBalanceDefinition` with `strategy: "WEIGHTED"` and set it on the `RouteDefinition`

#### Scenario: Default load balance when not set
- **WHEN** a route has `load_balance_strategy: null` or empty
- **THEN** the `RouteDefinition.loadBalance` SHALL be null, and gateway-core SHALL use its default "ROUND_ROBIN"

### Requirement: Path rewrite and timeout auto-converted to plugins
The `RouteServiceImpl` SHALL automatically convert `pathRewrite` and `timeouts` from the route DTO into `request-rewrite` and `timeout` plugin entries respectively, ensuring these configurations are persisted and synced to gateway-core.

#### Scenario: Path rewrite creates request-rewrite plugin
- **WHEN** `createRoute()` is called with `pathRewrite: {from: "/api/(.*)", to: "/$1"}`
- **THEN** the system SHALL create a `gw_route_plugin` entry with plugin name `request-rewrite` and config `{"pathFrom": "/api/(.*)", "pathTo": "/$1", "rewriteType": "REGEX"}`

#### Scenario: Timeouts create timeout plugin
- **WHEN** `createRoute()` is called with `timeouts: {connect: 5000, response: 30000}`
- **THEN** the system SHALL create a `gw_route_plugin` entry with plugin name `timeout` and config `{"connectTimeout": 5000, "responseTimeout": 30000}`

#### Scenario: No path rewrite or timeout
- **WHEN** `createRoute()` is called without `pathRewrite` or `timeouts`
- **THEN** no additional plugin entries SHALL be created

### Requirement: Permission seed data consistency
The database seed data (`data.sql`) SHALL use permission strings that exactly match the `@SaCheckPermission` annotations in all controllers. Any existing database SHALL provide a migration script to update existing permission records.

#### Scenario: Permission strings match controllers
- **WHEN** the application initializes with seed data
- **THEN** `sys_menu.perms` values SHALL be `route:view`, `route:plugin:view`, `system:user:view`, `system:role:view` — matching the controller annotations exactly

#### Scenario: Migration for existing databases
- **WHEN** an existing database has old permission strings (`route:detail`, etc.)
- **THEN** a migration SQL script SHALL be available to update those values to match the new convention
