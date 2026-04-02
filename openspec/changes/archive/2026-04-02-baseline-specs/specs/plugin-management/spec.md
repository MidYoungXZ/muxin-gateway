## ADDED Requirements

### Requirement: Plugin definition with JSON Schema
The system SHALL store plugin definitions in `gw_plugin` with a `schema` field containing JSON Schema that describes the plugin's configuration structure.

#### Scenario: Plugin with schema definition
- **WHEN** a plugin is created with `schema: {"type":"object","properties":{"rate":{"type":"integer","minimum":1}}}`
- **THEN** the system SHALL store the schema and use it to validate plugin configurations

### Requirement: Plugin CRUD operations
The system SHALL provide REST API for creating, reading, updating, and deleting plugin definitions.

#### Scenario: Create plugin
- **WHEN** `POST /api/plugins` is called with `plugin_name`, `plugin_type` (AUTH or FILTER), and optional `schema`
- **THEN** the system SHALL create a `gw_plugin` record and return the new plugin ID

#### Scenario: Plugin name must be unique
- **WHEN** a plugin is created with a `plugin_name` that already exists
- **THEN** the database unique constraint SHALL reject the insertion

#### Scenario: Filter plugins by type
- **WHEN** `GET /api/plugins?type=FILTER` is called
- **THEN** the system SHALL return only plugins with `plugin_type: "FILTER"`

### Requirement: Plugin-to-filter mapping
The system SHALL map admin plugin names to gateway-core filter names via `DatabaseRouteConfigProvider.mapPluginToFilters()`. The mapping SHALL be:
- `rate-limit` → `RequestRateLimiter`
- `circuit-breaker` → `CircuitBreaker`
- `cors` → `CorsFilter`
- `timeout` → `TimeoutFilter`
- `request-rewrite` → `RequestRewriteFilter`
- `response-rewrite` → `ResponseRewriteFilter`

#### Scenario: Rate limit plugin maps to filter
- **WHEN** a route has plugin `rate-limit` with config `{"rate": 100, "burst": 200}`
- **THEN** `DatabaseRouteConfigProvider` SHALL produce a `FilterDefinition` with name `RequestRateLimiter` and args `{"replenishRate": 100, "burstCapacity": 200}`

#### Scenario: Non-FILTER plugins are skipped
- **WHEN** a plugin has `plugin_type: "AUTH"`
- **THEN** `mapPluginToFilters()` SHALL return an empty list and log a debug message

#### Scenario: Unknown plugin name is skipped
- **WHEN** a plugin has `plugin_name: "custom-plugin"` that has no filter mapping
- **THEN** `mapPluginToFilters()` SHALL log a warning and return an empty list

### Requirement: Per-route plugin config override
The system SHALL allow overriding plugin default configuration per route via `gw_route_plugin.config`.

#### Scenario: Override default config
- **WHEN** a route associates plugin `cors` with `config: {"allowOrigins": "https://specific.com"}`
- **THEN** the effective config SHALL merge default config with override, with override taking precedence

### Requirement: Plugin type classification
The system SHALL classify plugins into two types: AUTH (authentication phase, priority 8000-7000) and FILTER (request processing phase, priority 6000-1000).

#### Scenario: Plugin phase information
- **WHEN** a plugin is defined with `plugin_type: "AUTH"` and `phase: "AUTH"`
- **THEN** the system SHALL store both fields and use `plugin_type` for filter mapping decisions

### Requirement: Default priority inheritance
The system SHALL allow routes to inherit plugin default priority or override it via `priority_override`.

#### Scenario: Use default priority
- **WHEN** a route-plugin association has no `priority_override`
- **THEN** the effective priority SHALL be the plugin's `default_priority` (e.g., 5000)

#### Scenario: Override priority
- **WHEN** a route-plugin association has `priority_override: 6000`
- **THEN** the effective priority SHALL be 6000
