## MODIFIED Requirements

### Requirement: Dark mode with CSS variable system
The frontend SHALL support dark mode via `.dark` CSS class toggle. All components SHALL use custom CSS variables (e.g., `--bg-primary`, `--card-bg`, `--text-primary`, `--border-primary`, `--text-tertiary`), NOT Element Plus theme variables (`--el-text-color-primary`, `--el-fill-color-light`, etc.).

#### Scenario: Toggle dark mode
- **WHEN** the user toggles dark mode
- **THEN** the `.dark` class SHALL be added/removed from the root element, and all CSS variables SHALL switch to dark values

#### Scenario: Component uses correct CSS variables
- **WHEN** a component sets background color
- **THEN** it SHALL use `var(--card-bg)` instead of hardcoded `#fff` or `var(--el-bg-color)`

#### Scenario: Section title readable in dark mode
- **WHEN** a route wizard step component renders a section title
- **THEN** the title SHALL use `var(--text-primary)` and borders SHALL use `var(--border-primary)`, ensuring readability in both light and dark modes

#### Scenario: Checkbox button styled in dark mode
- **WHEN** dark mode is active and the route matching step renders `el-checkbox-button` components (HTTP methods)
- **THEN** the buttons SHALL have background `var(--input-bg)`, text `var(--text-primary)`, border `var(--border-primary)`, and checked state SHALL use `var(--primary-color)` background

#### Scenario: Preview box dark mode background
- **WHEN** dark mode is active and the route matching step renders the match preview section
- **THEN** the preview box background SHALL use `var(--bg-tertiary)` instead of `var(--el-fill-color-light)`

### Requirement: Login state persistence
The frontend SHALL persist authentication state (token, tokenType) in localStorage via Pinia user store. On page refresh, the store SHALL restore state from localStorage AND proactively validate the token with the backend before considering the user authenticated.

#### Scenario: Restore session after refresh
- **WHEN** the page is refreshed and localStorage contains a token
- **THEN** the user store SHALL restore the token, call `GET /api/auth/user-info` to verify the token is still valid with the backend, and set `isLoggedIn: true` only if the backend confirms validity

#### Scenario: Redirect to login when backend rejects token
- **WHEN** the page is refreshed with a stored token but `GET /api/auth/user-info` returns 401 or fails
- **THEN** the user store SHALL clear localStorage and redirect to `/login`

#### Scenario: Redirect to login when token expired
- **WHEN** an API call returns 401
- **THEN** the request interceptor SHALL clear the user store and redirect to `/login`

### Requirement: Route creation wizard
The frontend SHALL provide a 4-step wizard dialog for creating/editing routes: Step 1 (Basic Info) → Step 2 (Route Matching) → Step 3 (Target Service) → Step 4 (Plugin Config). When editing, the wizard SHALL load existing route data including predicates and populate all form fields correctly.

#### Scenario: Step navigation
- **WHEN** the user clicks step 3 in the navigation
- **THEN** steps 1 and 2 SHALL show a completion indicator (if previously completed) and the content SHALL switch to the service selection form

#### Scenario: Save route from wizard
- **WHEN** the user completes all 4 steps and clicks "Save"
- **THEN** the frontend SHALL submit a `RouteCreateDTO` with `matching` (from step 2) and `plugins` (from step 4) to `POST /api/routes`

#### Scenario: Load predicates when editing route
- **WHEN** the user clicks "Edit" on a route and the API returns predicates
- **THEN** the form SHALL populate path pattern, methods, hosts (from `config.hosts`), headers (from `config.headers`), and queries (from `config.queries`) from the predicate data

## ADDED Requirements

### Requirement: Responsive route list layout
The route list table SHALL use `min-width` for informational columns (route ID, name, URI, load balance, priority, predicates count, plugins count, status) so that the action column is always fully visible. Columns SHALL compress when viewport width is insufficient.

#### Scenario: Action buttons fully visible
- **WHEN** the browser viewport is at standard width (1280px+)
- **THEN** all action buttons (查看, 编辑, 删除) SHALL be fully visible without horizontal scrolling

#### Scenario: Columns compress on narrow viewport
- **WHEN** the browser viewport narrows below the total column width
- **THEN** informational columns with `min-width` SHALL compress while the action column remains accessible
