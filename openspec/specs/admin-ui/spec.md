## Purpose
Admin UI frontend (Vue 3 SPA) providing dynamic routing, dark mode, schema-driven forms, route creation wizard, and Element Plus auto-import.

## Requirements

### Requirement: SPA with dynamic routing
The frontend SHALL be a Vue 3 SPA using Composition API. Routes SHALL be dynamically loaded from the backend menu API based on the authenticated user's permissions.

#### Scenario: Static routes for unauthenticated access
- **WHEN** the application loads without authentication
- **THEN** only static routes `/login`, `/403`, `/404` SHALL be registered

#### Scenario: Dynamic routes loaded after login
- **WHEN** a user logs in successfully
- **THEN** the menu store SHALL fetch `GET /api/menus/user-tree`, register Vue Router routes from C-type menus, and render the sidebar navigation

### Requirement: Dark mode with CSS variable system
The frontend SHALL support dark mode via `.dark` CSS class toggle. All components SHALL use custom CSS variables (e.g., `--bg-primary`, `--card-bg`, `--text-primary`, `--border-primary`), NOT Element Plus theme variables.

#### Scenario: Toggle dark mode
- **WHEN** the user toggles dark mode
- **THEN** the `.dark` class SHALL be added/removed from the root element, and all CSS variables SHALL switch to dark values

#### Scenario: Component uses correct CSS variables
- **WHEN** a component sets background color
- **THEN** it SHALL use `var(--card-bg)` instead of hardcoded `#fff` or `var(--el-bg-color)`

### Requirement: Schema-driven form rendering
The frontend SHALL render plugin configuration forms dynamically from JSON Schema definitions using a recursive `SchemaField.vue` component.

#### Scenario: Render string field
- **WHEN** a schema property has `type: "string"` without `enum`
- **THEN** `SchemaField` SHALL render an `el-input` component

#### Scenario: Render select field
- **WHEN** a schema property has `type: "string"` with `enum: ["ANT", "REGEX", "EXACT"]`
- **THEN** `SchemaField` SHALL render an `el-select` with the enum options

#### Scenario: Render nested object recursively
- **WHEN** a schema property has `type: "object"` with `properties`
- **THEN** `SchemaField` SHALL recursively render nested `SchemaField` components for each property

#### Scenario: Render array of objects
- **WHEN** a schema property has `type: "array"` with `items.type: "object"`
- **THEN** `SchemaField` SHALL render an addable/removable list of object forms

### Requirement: Route creation wizard
The frontend SHALL provide a 4-step wizard dialog for creating/editing routes: Step 1 (Basic Info) → Step 2 (Route Matching) → Step 3 (Target Service) → Step 4 (Plugin Config).

#### Scenario: Step navigation
- **WHEN** the user clicks step 3 in the navigation
- **THEN** steps 1 and 2 SHALL show a completion indicator (if previously completed) and the content SHALL switch to the service selection form

#### Scenario: Save route from wizard
- **WHEN** the user completes all 4 steps and clicks "Save"
- **THEN** the frontend SHALL submit a `RouteCreateDTO` with `matching` (from step 2) and `plugins` (from step 4) to `POST /api/routes`

### Requirement: Login state persistence
The frontend SHALL persist authentication state (token, tokenType) in localStorage via Pinia user store. On page refresh, the store SHALL restore state from localStorage.

#### Scenario: Restore session after refresh
- **WHEN** the page is refreshed and localStorage contains a valid token
- **THEN** the user store SHALL restore the token and set `isLoggedIn: true`

#### Scenario: Redirect to login when token expired
- **WHEN** an API call returns 401
- **THEN** the request interceptor SHALL clear the user store and redirect to `/login`

### Requirement: Element Plus auto-import
The frontend SHALL use `unplugin-auto-import` and `unplugin-vue-components` to auto-import Element Plus components and APIs, avoiding manual import statements.

#### Scenario: Use el-button without import
- **WHEN** a component template contains `<el-button>`
- **THEN** the component SHALL be auto-resolved by the build plugin without manual import
