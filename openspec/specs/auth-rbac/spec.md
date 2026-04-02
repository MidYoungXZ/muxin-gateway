## Purpose
Authentication, authorization, and RBAC system using Sa-Token with JWT, supporting role-based access control, menu-based dynamic routing, data scope filtering, department hierarchy, and operation audit logging.

## Requirements

### Requirement: JWT token authentication
The system SHALL authenticate users via Sa-Token with JWT. Tokens SHALL be issued on login and validated on subsequent requests via `Authorization: Bearer <token>` header.

#### Scenario: Successful login
- **WHEN** `POST /api/auth/login` is called with valid `username` and `password`
- **THEN** the system SHALL return a JWT token with `tokenType: "Bearer"` and user info

#### Scenario: Invalid credentials
- **WHEN** login is attempted with incorrect password
- **THEN** the system SHALL return 401 with error message

#### Scenario: Token refresh
- **WHEN** `POST /api/auth/refresh-token` is called with a valid refresh token
- **THEN** the system SHALL issue a new access token

### Requirement: Role-based access control (RBAC)
The system SHALL enforce RBAC where: User → Role → Menu/Permission. Permissions follow format `{module}:{entity}:{action}` (e.g., `route:create`, `system:user:list`).

#### Scenario: Permission check on API call
- **WHEN** a user calls `POST /api/routes` without `route:create` permission
- **THEN** the system SHALL return 403 Forbidden

#### Scenario: User with multiple roles
- **WHEN** a user has roles "admin" and "viewer"
- **THEN** the user SHALL have the union of all permissions from both roles

### Requirement: Menu-based dynamic routing
The system SHALL load menus assigned to the user's roles and expose them as a menu tree. Menu types: M (directory), C (page), F (button permission).

#### Scenario: Load user menu tree
- **WHEN** `GET /api/menus/user-tree` is called by an authenticated user
- **THEN** the system SHALL return only menus assigned to the user's roles

#### Scenario: Button-level permissions
- **WHEN** `GET /api/menus/user-permissions` is called
- **THEN** the system SHALL return all F-type menu permissions (e.g., `route:create`, `system:user:delete`) for the current user

### Requirement: Five-level data scope filtering
The system SHALL support 5 data scope levels per role, enforced via `@DataScope` annotation + AOP:

| Scope | Rule |
|-------|------|
| 1 - ALL | No filtering |
| 2 - CUSTOM | Filter by departments in `sys_role_dept` |
| 3 - DEPT | Filter by user's own department |
| 4 - DEPT_AND_CHILDREN | Filter by user's department and all sub-departments |
| 5 - SELF_ONLY | Filter by user's own ID |

#### Scenario: Data scope DEPT filtering
- **WHEN** a user with `data_scope: 3` queries the user list
- **THEN** the SQL SHALL include `WHERE dept_id = {currentUser.deptId}`

#### Scenario: Data scope CUSTOM filtering
- **WHEN** a user with `data_scope: 2` and custom departments `[1, 5, 8]` queries data
- **THEN** the SQL SHALL include `WHERE dept_id IN (1, 5, 8)`

### Requirement: Department hierarchy management
The system SHALL support tree-structured departments with `parent_id` and `ancestors` (comma-separated path) fields.

#### Scenario: Department tree query
- **WHEN** `GET /api/dept/tree` is called
- **THEN** the system SHALL return the full department tree with parent-child relationships

#### Scenario: Move department
- **WHEN** `PUT /api/dept/{id}/move/{targetParentId}` is called
- **THEN** the department SHALL be moved under the new parent and `ancestors` path SHALL be updated

### Requirement: Operation audit logging
The system SHALL log management operations via `@OperationLog` annotation, capturing: module, operation, method, request URL, params, result, duration, operator, IP, browser, OS.

#### Scenario: Operation logged after action
- **WHEN** a user creates a route
- **THEN** an `sys_operation_log` record SHALL be inserted with operation details and the current user's info
