# Muxin Gateway Plus 路由配置指南

## 概述

`gateway-routes.yml` 是 Muxin Gateway Plus 的主配置文件，用于定义网关的路由规则、服务发现、过滤器、负载均衡等核心功能。

---

## 配置文件结构

```yaml
# 1. 功能域配置 (domains)
# 2. 服务定义 (services)
# 3. 路由配置 (routes)
# 4. 全局过滤器 (global-filters)
# 5. 全局负载均衡配置 (global-route-config)
# 6. 监控配置 (monitoring)
# 7. 安全配置 (security)
# 8. 缓存配置 (cache)
```

---

## 1. 功能域配置 (domains)

### 核心配置域 (core)

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `default-timeout` | 整型(ms) | 30000 | 默认超时时间 |
| `max-request-size` | 字符串 | 10MB | 最大请求大小 |
| `max-response-size` | 字符串 | 50MB | 最大响应大小 |

### 线程池配置域 (thread-pools.business)

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `core-size` | 整型 | 16 | 核心线程数 |
| `max-size` | 整型 | 32 | 最大线程数 |
| `queue-capacity` | 整型 | 1000 | 队列容量 |
| `keep-alive` | 整型(ms) | 60000 | 线程空闲存活时间 |

### 服务器配置域 (servers)

**HTTP 服务器:**

```yaml
servers:
  http:
    port: 8080              # 网关监听端口
    max-content-length: 10MB
    keep-alive: true        # 启用 HTTP Keep-Alive
    compression: true      # 启用 GZIP 压缩
```

**管理端口:**

```yaml
servers:
  management:
    port: 8081
    enabled: true
```

### 连接池配置域 (connection-pools)

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `max-connections-per-host` | 整型 | 100 | 每个主机的最大连接数 |
| `max-idle-connections` | 整型 | 50 | 最大空闲连接数 |
| `connection-timeout` | 整型(ms) | 5000 | 连接超时时间 |
| `idle-timeout` | 整型(ms) | 60000 | 空闲连接超时时间 |

---

## 2. 服务定义 (services)

### 服务结构

```yaml
services:
  - id: service-unique-id          # 唯一标识符 (必需)
    name: "service-display-name"  # 显示名称 (必需)
    type: STATIC                   # 类型: STATIC (静态) 或 DISCOVERY (动态)
    supported-protocols: [HTTP]   # 支持的协议列表
    addresses:                    # 服务地址列表 (CONFIG 类型)
      - uri: "http://host:port"   # 服务地址 (必需)
        weight: 100              # 权重 (默认 100)
        metadata:                # 元数据 (可选)
          key: "value"
    health-check:                 # 健康检查 (可选)
      enabled: true
      interval: 30000            # 检查间隔 (ms)
      timeout: 5000              # 超时时间 (ms)
      path: "/health"           # 健康检查路径
```

### 服务类型

#### CONFIG 类型（静态配置）

```yaml
services:
  - id: user-service-001
    name: "user-service"
    type: CONFIG
    supported-protocols: [HTTP]
    addresses:
      - uri: "http://user-service-1:8080"
        weight: 100
        metadata:
          zone: "zone-a"
      - uri: "http://user-service-2:8080"
        weight: 100
        metadata:
          zone: "zone-b"
```

#### DISCOVERY 类型（服务发现）

```yaml
services:
  - id: user-service-discovery
    name: "user-service"
    type: DISCOVERY
    supported-protocols: [HTTP]
    discovery:
      type: NACOS                # 或 CONSUL, EUREKA
      namespace: "public"
      group: "DEFAULT_GROUP"
    health-check:
      enabled: true
```

### 多地址与权重

当一个服务有多个地址时，网关会根据权重进行负载均衡：

```yaml
services:
  - id: order-service-001
    name: "order-service"
    type: STATIC
    addresses:
      - uri: "http://order-v1:8080"   # 权重 100，占比 50%
        weight: 100
      - uri: "http://order-v2:8080"   # 权重 100，占比 50%
        weight: 100
```

---

## 3. 路由配置 (routes)

### 路由结构

```yaml
routes:
  - id: route-unique-id           # 唯一标识符 (必需)
    name: "路由显示名称"           # 显示名称 (必需)
    description: "路由描述"        # 描述信息
    order: 100                     # 优先级，数字越小越先匹配
    enabled: true                  # 是否启用
    
    service-ref: service-id       # 引用的服务 ID (必需)
    protocol: HTTP                # 协议类型 (必需)
    
    predicates:                   # 断言列表 (至少一个)
      - type: PATH
        config:
          pattern: "/api/**"
    
    load-balance:                 # 负载均衡配置
      strategy: ROUND_ROBIN
    
    filters:                      # 过滤器列表
      - type: REQUEST_LOG
        order: 100
        enabled: true
    
    timeouts:                     # 超时配置 (可选)
      connection: 5000
      request: 30000
      total: 60000
    
    metadata:                     # 自定义元数据
      business-domain: "user"
```

### 路由执行顺序

路由按 `order` 字段升序排列，数字越小越先匹配。建议：

- 用户路由: `order: 100`
- 业务路由: `order: 200-500`
- 系统路由: `order: 9000+`
- 兜底路由: `order: 9999`

### 断言 (Predicates)

#### PATH 断言

匹配请求路径，支持 Ant 风格路径模式：

```yaml
predicates:
  - type: PATH
    config:
      pattern: "/api/users/**"      # 匹配 /api/users/ 下的所有路径
      # 示例: /api/users/123 匹配
      # 示例: /api/users/profile 匹配
```

**Ant 风格模式：**

| 模式 | 说明 | 示例匹配 |
|------|------|----------|
| `/api/users/**` | 匹配 `/api/users/` 下的任意路径 | `/api/users/123`, `/api/users/abc/profile` |
| `/api/users/*` | 匹配单层路径 | `/api/users/123` 匹配，`/api/users/123/profile` 不匹配 |
| `/api/users/?` | 匹配单个字符 | `/api/users/1` 匹配 |

**路径前缀剥离 (strip-prefix):**

```yaml
predicates:
  - type: PATH
    config:
      pattern: "/sde/admin/event/subscription/**"
      strip-prefix: 1              # 剥离第1个路径段
```

**示例：**

| 原始请求路径 | strip-prefix | 转发到后端的路径 |
|--------------|--------------|------------------|
| `/sde/admin/event/subscription/list` | 1 | `/admin/event/subscription/list` |
| `/sde/admin/event/subscription/xyz` | 1 | `/admin/event/subscription/xyz` |
| `/sde/admin/event/subscription/a/b/c` | 2 | `/event/subscription/a/b/c` |

#### METHOD 断言

匹配 HTTP 方法：

```yaml
predicates:
  - type: PATH
    config:
      pattern: "/api/users/**"
  - type: METHOD
    config:
      methods: [GET, POST, PUT, DELETE]
```

#### HEADER 断言

匹配请求头：

```yaml
predicates:
  - type: HEADER
    config:
      name: "X-Custom-Header"
      pattern: "custom-value.*"
```

#### QUERY 断言

匹配查询参数：

```yaml
predicates:
  - type: QUERY
    config:
      name: "status"
      pattern: "active"
```

---

## 4. 过滤器 (Filters)

### 过滤器结构

```yaml
filters:
  - type: FILTER_TYPE           # 过滤器类型 (必需)
    order: 100                  # 执行顺序，数字越小越先执行
    enabled: true               # 是否启用
    config:                     # 过滤器配置 (可选)
      key: "value"
```

### 过滤器类型

| 类型 | 说明 | 阶段 | 主要配置项 |
|------|------|------|------------|
| `REQUEST_ID` | 生成请求追踪 ID | PRE | `header-name`, `generate-if-missing` |
| `CORS` | 跨域资源共享 | PRE | `allowed-origins`, `allowed-methods`, `allowed-headers`, `max-age` |
| `AUTH` | 认证授权 | PRE | `auth-type` (JWT/BASIC/TOKEN), `secret-key` |
| `REQUEST_LOG` | 请求日志 | PRE/POST | 日志级别配置 |
| `METRICS` | 性能指标收集 | PRE/POST | `collect-request-metrics`, `collect-response-metrics` |
| `RATE_LIMIT` | 限流 | PRE | `requests-per-second`, `burst-capacity` |

### REQUEST_ID 过滤器

```yaml
filters:
  - type: REQUEST_ID
    order: 10
    enabled: true
    config:
      header-name: "X-Request-ID"           # 请求头名称
      generate-if-missing: true             # 如果不存在是否生成
```

### CORS 过滤器

```yaml
filters:
  - type: CORS
    order: 200
    enabled: true
    config:
      allowed-origins: ["*"]                # 允许的来源
      allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
      allowed-headers: ["*"]                # 允许的请求头
      allow-credentials: true               # 是否允许携带凭证
      max-age: 3600                          # 预检请求缓存时间 (秒)
```

### AUTH 过滤器

**JWT 认证：**

```yaml
filters:
  - type: AUTH
    order: 200
    enabled: true
    config:
      auth-type: "JWT"                       # 认证类型
      secret-key: "${JWT_SECRET:muxin-gateway-secret}"  # 密钥，支持环境变量
```

**Basic 认证：**

```yaml
filters:
  - type: AUTH
    order: 200
    enabled: true
    config:
      auth-type: "BASIC"
```

### RATE_LIMIT 过滤器

```yaml
filters:
  - type: RATE_LIMIT
    order: 300
    enabled: true
    config:
      requests-per-second: 100              # 每秒请求数
      burst-capacity: 200                   # 突发容量
```

---

## 5. 负载均衡 (Load Balance)

### 配置位置

负载均衡可以在两个级别配置：

1. **全局默认配置** - `global-route-config.default-load-balance`
2. **路由级别配置** - `routes[].load-balance`

路由级别配置会覆盖全局配置。

### 负载均衡策略

| 策略 | 说明 | 配置示例 |
|------|------|----------|
| `ROUND_ROBIN` | 顺序轮询 | `strategy: ROUND_ROBIN` |
| `RANDOM` | 随机选择 | `strategy: RANDOM` |
| `WEIGHTED_ROUND_ROBIN` | 权重轮询 | `strategy: WEIGHTED_ROUND_ROBIN` |
| `LEAST_CONNECTIONS` | 最少连接数 | `strategy: LEAST_CONNECTIONS` |

### 权重配置

```yaml
services:
  - id: user-service-001
    addresses:
      - uri: "http://user-v1:8080"
        weight: 100                         # 权重 100
      - uri: "http://user-v2:8080"
        weight: 50                          # 权重 50，接收请求量是 v1 的一半
```

---

## 6. 全局过滤器 (global-filters)

全局过滤器作用于所有路由，在路由特定过滤器之前执行：

```yaml
global-filters:
  - type: REQUEST_ID
    order: 10
    enabled: true
    config:
      header-name: "X-Request-ID"
      generate-if-missing: true
  
  - type: CORS
    order: 20
    enabled: true
    config:
      allowed-origins: ["*"]
      allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
      allowed-headers: ["*"]
      allow-credentials: true
      max-age: 3600
  
  - type: METRICS
    order: 30
    enabled: true
    config:
      collect-request-metrics: true
      collect-response-metrics: true
```

---

## 7. 超时配置 (timeouts)

超时配置可以针对单个路由设置：

```yaml
timeouts:
  connection: 5000      # 连接超时 (ms)
  request: 30000        # 请求超时 (ms)
  total: 60000          # 總超时 (ms)
```

---

## 8. 监控配置 (monitoring)

```yaml
monitoring:
  metrics:
    enabled: true
    export-interval: 60000
    tags:
      application: "muxin-gateway"
      environment: "${ENVIRONMENT:development}"
  
  tracing:
    enabled: false
    sampling-rate: 0.1
    trace-header: "X-Trace-ID"
    span-header: "X-Span-ID"
  
  logging:
    level:
      root: INFO
      "com.muxin.gateway": DEBUG
    pattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

---

## 9. 安全配置 (security)

```yaml
security:
  jwt:
    secret: "${JWT_SECRET:muxin-gateway-secret-key-2024}"
    expiration: 3600000
    refresh-expiration: 86400000
    issuer: "muxin-gateway"
  
  rate-limit:
    default:
      requests-per-second: 1000
      burst-capacity: 2000
```

---

## 10. 缓存配置 (cache)

```yaml
cache:
  enabled: true
  provider: "caffeine"           # 缓存提供者: caffeine, redis, guava
  default-ttl: 300000            # 默认 TTL (ms)
  max-size: 10000                # 最大缓存条目数
```

---

## 完整配置示例

### 用户服务路由

```yaml
services:
  - id: user-service-001
    name: "user-service"
    type: CONFIG
    supported-protocols: [HTTP]
    addresses:
      - uri: "http://user-service-1:8080"
        weight: 100
      - uri: "http://user-service-2:8080"
        weight: 100
    health-check:
      enabled: true
      interval: 30000
      timeout: 5000
      path: "/health"

routes:
  - id: user-service-route
    name: "用户服务"
    order: 100
    enabled: true
    service-ref: user-service-001
    protocol: HTTP
    predicates:
      - type: PATH
        config:
          pattern: "/api/users/**"
      - type: METHOD
        config:
          methods: [GET, POST, PUT, DELETE]
    load-balance:
      strategy: ROUND_ROBIN
    filters:
      - type: REQUEST_LOG
        order: 100
        enabled: true
      - type: AUTH
        order: 200
        enabled: true
        config:
          auth-type: "JWT"
          secret-key: "${JWT_SECRET:muxin-gateway-secret}"
    timeouts:
      connection: 5000
      request: 30000
      total: 60000
```

### 带路径前缀剥离的路由

```yaml
services:
  - id: sde-admin-service-001
    name: "sde-admin-service"
    type: CONFIG
    supported-protocols: [HTTP]
    addresses:
      - uri: "http://127.0.0.1:9180"
        weight: 100
    health-check:
      enabled: false

routes:
  - id: sde-admin-route
    name: "SDE管理服务"
    order: 400
    enabled: true
    service-ref: sde-admin-service-001
    protocol: HTTP
    predicates:
      - type: PATH
        config:
          pattern: "/sde/admin/event/subscription/**"
          strip-prefix: 1
    filters:
      - type: REQUEST_LOG
        order: 100
        enabled: true
      - type: CORS
        order: 200
        enabled: true
```

---

## 路由匹配流程

```
请求进入网关
    ↓
按 order 排序遍历路由
    ↓
依次检查每个路由的 predicates
    ↓
所有 predicates 匹配成功 → 执行该路由
    ↓
执行 PRE 过滤器
    ↓
负载均衡选择后端地址
    ↓
发送请求到后端服务
    ↓
执行 POST 过滤器
    ↓
返回响应给客户端
```

---

## 环境变量引用

配置文件中可以使用 `${ENV_VAR:default}` 语法引用环境变量：

```yaml
security:
  jwt:
    secret: "${JWT_SECRET:muxin-gateway-secret}"

monitoring:
  metrics:
    tags:
      environment: "${ENVIRONMENT:development}"
```

如果环境变量不存在，则使用默认值 `muxin-gateway-secret` 和 `development`。

---

## 注意事项

1. **路由顺序**: 确保高优先级路由的 `order` 值较小
2. **服务引用**: 每个路由必须通过 `service-ref` 引用一个已定义的服务
3. **协议一致性**: 路由的 `protocol` 必须与服务的 `supported-protocols` 匹配
4. **过滤器顺序**: 全局过滤器的 `order` 应小于路由过滤器的 `order`
5. **路径剥离**: `strip-prefix` 只在 PATH 断言中生效
6. **健康检查**: 生产环境建议启用健康检查
