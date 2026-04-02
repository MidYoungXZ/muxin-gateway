## Purpose
Route engine capabilities for the gateway-core module, including request matching, filtering, load balancing, and connection management.

## Requirements

### Requirement: Path-based route matching
The system SHALL match incoming HTTP requests against configured path patterns using Ant-style pattern matching. Supported patterns: `**` (multi-segment), `*` (single segment), `?` (single character).

#### Scenario: ANT pattern matches nested paths
- **WHEN** a route is configured with predicate type `PATH` and pattern `/api/v1/**`
- **THEN** requests to `/api/v1/users` and `/api/v1/orders/123` SHALL both match

#### Scenario: Path prefix stripping
- **WHEN** a route predicate has `strip-prefix` set to `1`
- **THEN** the first path segment SHALL be removed before forwarding to upstream

### Requirement: HTTP method matching
The system SHALL match requests by HTTP method. Multiple methods SHALL be matched with OR semantics.

#### Scenario: Multiple methods configured
- **WHEN** a route is configured with predicate type `METHOD` and methods `["GET", "POST"]`
- **THEN** both GET and POST requests SHALL match, but PUT requests SHALL NOT match

### Requirement: Additional predicate types (implemented, not registered)
The system SHALL have implementations for HEADER, QUERY, COOKIE, HOST, REMOTE_ADDR, and BETWEEN predicates. These implementations exist but are currently NOT registered in `RouteConfigConverter`.

#### Scenario: Header predicate implementation exists
- **WHEN** a `HeaderPredicate` is created with `header: "X-Version"` and `regexp: "v1"`
- **THEN** it SHALL match requests containing header `X-Version: v1`

#### Scenario: Host predicate implementation exists
- **WHEN** a `HostPredicate` is created with patterns `["*.example.com"]`
- **THEN** it SHALL match requests with Host header matching `*.example.com`

#### Scenario: RemoteAddr predicate implementation exists
- **WHEN** a `RemoteAddrPredicate` is created with sources `["192.168.1.0/24"]`
- **THEN** it SHALL match requests from IP range `192.168.1.0 - 192.168.1.255`

### Requirement: Token bucket rate limiting
The system SHALL enforce per-client rate limiting using a token bucket algorithm. When rate is exceeded, the system SHALL return HTTP 429.

#### Scenario: Rate limit exceeded
- **WHEN** a route has `RequestRateLimiter` filter with `replenishRate: 10` and `burstCapacity: 20`
- **THEN** requests exceeding 20 burst or 10/s sustained rate SHALL receive HTTP 429

### Requirement: Circuit breaker protection
The system SHALL provide circuit breaker with three states: CLOSED, OPEN, HALF_OPEN. When open, the system SHALL return HTTP 503.

#### Scenario: Circuit opens on high failure rate
- **WHEN** failure rate exceeds `failureRateThreshold` (default 50%) within the ring buffer
- **THEN** the circuit SHALL transition to OPEN state and return HTTP 503 for subsequent requests

#### Scenario: Circuit transitions to half-open after wait
- **WHEN** the circuit is OPEN and `waitDurationInOpenState` has elapsed
- **THEN** the circuit SHALL transition to HALF_OPEN and allow a test request through

### Requirement: CORS preflight handling
The system SHALL handle CORS preflight OPTIONS requests and add appropriate CORS headers to responses.

#### Scenario: Preflight request handling
- **WHEN** an OPTIONS request is received and `CorsFilter` is configured with `allowOrigins: "*"`
- **THEN** the response SHALL include `Access-Control-Allow-Origin`, `Access-Control-Allow-Methods`, and `Access-Control-Allow-Headers`

### Requirement: Request and response timeout enforcement
The system SHALL enforce connect and response timeouts. On timeout, the system SHALL return HTTP 504.

#### Scenario: Response timeout exceeded
- **WHEN** upstream does not respond within `responseTimeout` (default 30000ms)
- **THEN** the system SHALL return HTTP 504 to the client

### Requirement: Request rewriting
The system SHALL support rewriting request path via regex and adding/removing request headers before forwarding.

#### Scenario: Path regex rewrite
- **WHEN** `RequestRewriteFilter` is configured with `pathRegex: "^/api/v1/(.*)"` and `pathReplacement: "/$1"`
- **THEN** request path `/api/v1/users` SHALL be rewritten to `/users` before forwarding

### Requirement: Response rewriting
The system SHALL support rewriting response body via regex and adding/removing response headers before returning to client.

#### Scenario: Response header addition
- **WHEN** `ResponseRewriteFilter` is configured with `headersToAdd: {"X-Response-Id": "abc123"}`
- **THEN** the response SHALL include header `X-Response-Id: abc123`

### Requirement: Load balancing across service instances
The system SHALL support 4 load balancing strategies: ROUND_ROBIN (default), RANDOM, WEIGHTED_ROUND_ROBIN, LEAST_CONNECTIONS.

#### Scenario: Round robin selection
- **WHEN** a route has `loadBalanceStrategy: ROUND_ROBIN` and 3 healthy instances
- **THEN** requests SHALL be distributed sequentially across all 3 instances

#### Scenario: Strategy defaults to round robin
- **WHEN** no load balance strategy is specified
- **THEN** `LoadBalanceStrategyFactory` SHALL use ROUND_ROBIN as default

### Requirement: Per-endpoint connection pooling
The system SHALL maintain a Netty `FixedChannelPool` per target endpoint with configurable pool size, acquire timeout, and idle timeout.

#### Scenario: Connection reuse
- **WHEN** a request is forwarded to an endpoint that already has an active connection in pool
- **THEN** the system SHALL reuse the existing connection rather than creating a new one
