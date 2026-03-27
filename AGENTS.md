# Muxin Gateway - Development Guide

## Project Overview

Muxin Gateway is a high-performance API gateway built with Netty and Spring Boot.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                      gateway-main 进程                               │
│  ┌─────────────────────┐         ┌───────────────────────────────┐  │
│  │   gateway-admin     │         │        gateway-core           │  │
│  │                     │         │                               │  │
│  │  RouteController    │ 调用     │  RouteConfigProvider          │  │
│  │  applyChanges()  ───┼────────▶│  (DatabaseProvider)           │  │
│  │                     │ refresh │    .refresh()                 │  │
│  └─────────────────────┘         │    .getRoutes()               │  │
│                                  └───────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                    gateway-core 单独进程                             │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                       gateway-core                             │  │
│  │                                                                │  │
│  │   RouteConfigProvider (YamlProvider)                          │  │
│  │     .getRoutes() ──→ 从 yml 加载                               │  │
│  │     .refresh() ────→ 重新加载 yml                              │  │
│  │     WatchService ──→ 文件变更自动 refresh()                     │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

## Module Structure

| Module | Description |
|--------|-------------|
| `gateway-core` | Core gateway engine (Netty-based, no Spring dependency) |
| `gateway-admin` | Admin management module (Spring Boot, database) |
| `gateway-main` | Main application combining admin + core |
| `gateway-cloud` | Cloud discovery module (Nacos) |

## Configuration Providers

### RouteConfigProvider Interface

```java
public interface RouteConfigProvider {
    List<RouteDefinition> getRoutes();
    Optional<RouteDefinition> getRoute(String routeId);
    void refresh();
    void addChangeListener(ConfigChangeListener listener);
    String getSource();
}
```

### ServiceConfigProvider Interface

```java
public interface ServiceConfigProvider {
    List<ServiceDefinition> getServices();
    Optional<ServiceDefinition> getService(String serviceId);
    void refresh();
    void addChangeListener(ConfigChangeListener listener);
    String getSource();
}
```

### Implementations

| Implementation | Module | Source | Refresh Trigger |
|----------------|--------|--------|-----------------|
| `YamlRouteConfigProvider` | gateway-core | YAML file | WatchService |
| `YamlServiceConfigProvider` | gateway-core | YAML file | WatchService |
| `DatabaseRouteConfigProvider` | gateway-main | Database | Admin API call |
| `DatabaseServiceConfigProvider` | gateway-main | Database | Admin API call |

## Key Files

### Core Interfaces
- `gateway-core/src/main/java/com/muxin/gateway/core/config/provider/RouteConfigProvider.java`
- `gateway-core/src/main/java/com/muxin/gateway/core/config/provider/ServiceConfigProvider.java`
- `gateway-core/src/main/java/com/muxin/gateway/core/config/provider/ConfigChangeListener.java`
- `gateway-core/src/main/java/com/muxin/gateway/core/config/provider/ConfigChangedEvent.java`

### YAML Providers
- `gateway-core/src/main/java/com/muxin/gateway/core/config/provider/YamlRouteConfigProvider.java`
- `gateway-core/src/main/java/com/muxin/gateway/core/config/provider/YamlServiceConfigProvider.java`

### Database Providers
- `gateway-main/src/main/java/com/muxin/gateway/config/provider/DatabaseRouteConfigProvider.java`
- `gateway-main/src/main/java/com/muxin/gateway/config/provider/DatabaseServiceConfigProvider.java`

### Auto Configuration
- `gateway-main/src/main/java/com/muxin/gateway/config/GatewayAutoConfiguration.java`
- `gateway-main/src/main/java/com/muxin/gateway/config/YamlProviderAutoConfiguration.java`

## API Endpoints

### Route Apply API

```
POST /api/routes/apply
Permission: route:update
Description: Apply all configuration changes from database to gateway-core
```

```
POST /api/routes/refresh-routes
Permission: route:update
Description: Refresh route configuration only
```

```
POST /api/routes/refresh-services
Permission: route:update
Description: Refresh service configuration only
```

## Dynamic Router System

### Frontend Router Configuration

The frontend uses a dynamic router system where routes are loaded from the backend menu API.

```
┌─────────────────────────────────────────────────────────────────┐
│                    Dynamic Router Architecture                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Login → Fetch User Menus → Register Routes → Render Sidebar  │
│              ↓                ↓                ↓                │
│           API returns    router.addRoute()    Menu Component    │
│                                                                 │
│   Static Routes (only basic):                                   │
│   ┌─────────────────┐                                           │
│   │ /login          │  ← No permission required                │
│   │ /404            │                                           │
│   │ /403            │                                           │
│   └─────────────────┘                                           │
│                                                                 │
│   Dynamic Routes (loaded by permission):                        │
│   ┌─────────────────┐                                           │
│   │ /system/users   │  ← Registered based on user permission    │
│   │ /system/roles   │                                           │
│   │ /routes/*       │                                           │
│   └─────────────────┘                                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Key Router Files
- `gateway-admin-ui/src/router/index.ts` - Static routes configuration
- `gateway-admin-ui/src/router/utils.ts` - Route generation utilities
- `gateway-admin-ui/src/router/permission.ts` - Route guards
- `gateway-admin-ui/src/stores/menu.ts` - Dynamic route registration

## Data Permission System

### Data Scope Configuration

Roles can be configured with different data scopes:

| Data Scope | Code | Description |
|------------|------|-------------|
| 全部数据 | 1 | Access all data (Super Admin) |
| 自定义数据 | 2 | Access selected departments only |
| 本部门数据 | 3 | Access user's department only |
| 本部门及以下 | 4 | Access user's department and sub-departments (default) |
| 仅本人数据 | 5 | Access only own data |

### Database Tables

```sql
-- Role table with data_scope field
sys_role.data_scope INTEGER DEFAULT 4

-- Role-Department association (for custom data scope)
sys_role_dept (role_id, dept_id)
```

### Key Data Permission Files
- `gateway-admin/src/main/java/com/muxin/gateway/admin/annotation/DataScope.java` - Annotation
- `gateway-admin/src/main/java/com/muxin/gateway/admin/aspect/DataScopeAspect.java` - AOP aspect
- `gateway-admin/src/main/java/com/muxin/gateway/admin/context/DataScopeContext.java` - Context
- `gateway-admin/src/main/java/com/muxin/gateway/admin/util/DataScopeHelper.java` - Utility

### Data Permission Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    Data Permission Flow                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. User requests user list                                     │
│     GET /api/users                                              │
│                                                                 │
│  2. UserService.pageQueryWithDataScope() applies @DataScope     │
│                                                                 │
│  3. DataScopeAspect intercepts                                  │
│     ├── Get current user's data scope                           │
│     ├── Generate SQL condition:                                 │
│     │   ├── dataScope=1: No filter                              │
│     │   ├── dataScope=2: dept_id IN (custom depts)              │
│     │   ├── dataScope=3: dept_id = user's dept                  │
│     │   ├── dataScope=4: dept_id IN (user's dept + children)    │
│     │   └── dataScope=5: id = current user                      │
│     └── Inject into QueryWrapper                                │
│                                                                 │
│  4. Return filtered user list                                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Department-Role-User Relationship

```
┌─────────────────────────────────────────────────────────────────┐
│                    Entity Relationships                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  User → Department (belonging)                                  │
│     sys_user.dept_id → sys_dept.id                              │
│                                                                 │
│  User → Roles (permissions)                                     │
│     sys_user_role → sys_role                                    │
│                                                                 │
│  Role → Data Scope (data boundary)                              │
│     sys_role.data_scope                                         │
│                                                                 │
│  Role → Departments (custom permission)                         │
│     sys_role_dept → sys_dept (when data_scope=2)                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Development Notes

### Configuration Refresh Flow

1. User modifies route/service in admin UI
2. User clicks "Apply" button
3. Frontend calls `POST /api/routes/apply`
4. `ConfigRefreshService.refreshAll()` is called
5. `RouteConfigProvider.refresh()` and `ServiceConfigProvider.refresh()` are called
6. `GatewayBootstrap.refreshRoutes()` is triggered via listener
7. Routes are reloaded from source (database or YAML)

### Auto-configuration Priority

1. `DatabaseProvider` (if available) - when gateway-main is used
2. `YamlProvider` (fallback) - when gateway-core is used standalone

The `@ConditionalOnMissingBean` annotation ensures YAML provider is only created when Database provider is not available.

### Database to RouteDefinition Mapping

- `GwRoute` → `RouteDefinition`
- `GwServiceNode` → `ServiceDefinition` (grouped by serviceName)
- `GwPredicate` → `PredicateDefinition`
- `GwFilter` → `FilterDefinition`

## Build & Run

```bash
# Compile
mvn compile

# Run
java -jar gateway-main/target/gateway-main-1.0-SNAPSHOT.jar
```

## Configuration

### application.yml (gateway-main)

```yaml
muxin:
  gateway:
    netty:
      server:
        port: 8081  # Netty gateway port
    config-file: gateway-routes.yml  # YAML config file
    config-watch-enabled: true  # Enable file watching

server:
  port: 8080  # Tomcat admin port
```