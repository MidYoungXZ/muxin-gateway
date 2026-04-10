## 新增需求

### 需求：基于路径的路由匹配
系统应使用 Ant 风格模式匹配将传入 HTTP 请求与配置的路径模式进行匹配。支持的模式：`**`（多段）、`*`（单段）、`?`（单字符）。

#### 场景：ANT 模式匹配嵌套路径
- **WHEN** 路由配置了断言类型 `PATH` 和模式 `/api/v1/**`
- **THEN** `/api/v1/users` 和 `/api/v1/orders/123` 的请求都应匹配

#### 场景：路径前缀剥离
- **WHEN** 路由断言设置了 `strip-prefix` 为 `1`
- **THEN** 转发到上游之前应移除第一个路径段

### 需求：HTTP 方法匹配
系统应按 HTTP 方法匹配请求。多种方法应使用 OR 语义匹配。

#### 场景：配置多种方法
- **WHEN** 路由配置了断言类型 `METHOD` 和方法 `["GET", "POST"]`
- **THEN** GET 和 POST 请求应匹配，但 PUT 请求不应匹配

### 需求：其他断言类型（已实现，未注册）
系统应有 HEADER、QUERY、COOKIE、HOST、REMOTE_ADDR 和 BETWEEN 断言的实现。这些实现已存在，但当前未在 `RouteConfigConverter` 中注册。

#### 场景：Header 断言实现已存在
- **WHEN** 创建 `HeaderPredicate`，`header: "X-Version"`，`regexp: "v1"`
- **THEN** 应匹配包含 `X-Version: v1` 头的请求

#### 场景：Host 断言实现已存在
- **WHEN** 创建 `HostPredicate`，模式为 `["*.example.com"]`
- **THEN** 应匹配 Host 头匹配 `*.example.com` 的请求

#### 场景：RemoteAddr 断言实现已存在
- **WHEN** 创建 `RemoteAddrPredicate`，来源为 `["192.168.1.0/24"]`
- **THEN** 应匹配来自 IP 范围 `192.168.1.0 - 192.168.1.255` 的请求

### 需求：令牌桶限流
系统应使用令牌桶算法实施每客户端限流。超过速率限制时，系统应返回 HTTP 429。

#### 场景：超过限流
- **WHEN** 路由有 `RequestRateLimiter` 过滤器，`replenishRate: 10`，`burstCapacity: 20`
- **THEN** 超过 20 突发或 10/s 持续速率的请求应收到 HTTP 429

### 需求：熔断器保护
系统应提供三态熔断器：CLOSED、OPEN、HALF_OPEN。熔断器打开时，系统应返回 HTTP 503。

#### 场景：高失败率时熔断器打开
- **WHEN** 环形缓冲区内失败率超过 `failureRateThreshold`（默认 50%）
- **THEN** 熔断器应转换为 OPEN 状态，后续请求返回 HTTP 503

#### 场景：等待后熔断器转为半开
- **WHEN** 熔断器为 OPEN 且 `waitDurationInOpenState` 已过
- **THEN** 熔断器应转换为 HALF_OPEN 并允许测试请求通过

### 需求：CORS 预检处理
系统应处理 CORS 预检 OPTIONS 请求并在响应中添加适当的 CORS 头部。

#### 场景：预检请求处理
- **WHEN** 收到 OPTIONS 请求且 `CorsFilter` 配置了 `allowOrigins: "*"`
- **THEN** 响应应包含 `Access-Control-Allow-Origin`、`Access-Control-Allow-Methods` 和 `Access-Control-Allow-Headers`

### 需求：请求和响应超时强制执行
系统应强制执行连接和响应超时。超时时，系统应返回 HTTP 504。

#### 场景：响应超时
- **WHEN** 上游在 `responseTimeout`（默认 30000ms）内未响应
- **THEN** 系统应向客户端返回 HTTP 504

### 需求：请求重写
系统应支持通过正则表达式重写请求路径，并在转发前添加/移除请求头。

#### 场景：路径正则重写
- **WHEN** `RequestRewriteFilter` 配置了 `pathRegex: "^/api/v1/(.*)"` 和 `pathReplacement: "/$1"`
- **THEN** 请求路径 `/api/v1/users` 应在转发前重写为 `/users`

### 需求：响应重写
系统应支持通过正则表达式重写响应体，并在返回客户端前添加/移除响应头。

#### 场景：添加响应头
- **WHEN** `ResponseRewriteFilter` 配置了 `headersToAdd: {"X-Response-Id": "abc123"}`
- **THEN** 响应应包含头 `X-Response-Id: abc123`

### 需求：跨服务实例的负载均衡
系统应支持 3 种负载均衡策略：ROUND_ROBIN（默认）、RANDOM、WEIGHTED_ROUND_ROBIN。

#### 场景：轮询选择
- **WHEN** 路由有 `loadBalanceStrategy: ROUND_ROBIN` 和 3 个健康实例
- **THEN** 请求应在 3 个实例之间顺序分配

#### 场景：默认使用轮询策略
- **WHEN** 未指定负载均衡策略
- **THEN** `LoadBalanceStrategyFactory` 应使用 ROUND_ROBIN 作为默认

### 需求：每端点连接池
系统应为每个目标端点维护 Netty `FixedChannelPool`，具有可配置的池大小、获取超时和空闲超时。

#### 场景：连接复用
- **WHEN** 请求转发到池中已有活跃连接的端点
- **THEN** 系统应复用现有连接而非创建新连接
