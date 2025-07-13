# Gateway Core Plus 实现说明文档

## 📖 概述

Gateway Core Plus 是 Muxin Gateway 的核心重构模块，采用轻量级设计，不依赖 Spring Boot，提供了高性能、协议无关的网关核心功能。该模块基于 Netty 4.1+ 实现，支持多种协议转换和高度可扩展的架构设计。

## 🎯 设计目标

### 核心特性
- **轻量级独立运行**：不依赖 Spring Boot，可作为独立 Java 应用运行
- **协议无关设计**：支持 HTTP、gRPC、WebSocket、TCP 等多种协议
- **高性能架构**：基于 Netty 异步非阻塞 I/O，优化的线程模型
- **简化的架构**：移除了冗余的 Manager 层，采用直接的组件交互
- **完全的配置隔离**：每个路由的配置完全独立，避免状态共享
- **灵活的扩展性**：插件化的过滤器、负载均衡、协议转换机制

### 技术栈
- **网络框架**：Netty 4.1.115.Final
- **缓存**：Caffeine 3.1.8（高性能本地缓存）
- **日志**：SLF4J API 2.0.9
- **代码生成**：Lombok 1.18.30
- **Java版本**：Java 17+

## 🏗️ 核心架构

### 整体架构图
```
┌─────────────────────────────────────────────────────────────────┐
│                      GatewayApplication                          │
│                    (独立应用程序入口)                              │
└───────────────────────┬─────────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────────┐
│                    GatewayBootstrap                              │
│              (引导器：组件初始化和生命周期管理)                     │
├─────────────────────────────────────────────────────────────────┤
│  • 配置管理 (GatewayConfig, GlobalRouteConfig)                  │
│  • 核心组件初始化                                                │
│  • 服务器启动管理                                                │
└───────────────────────┬─────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┬──────────────┐
        ▼               ▼               ▼              ▼
┌───────────────┐ ┌─────────────┐ ┌──────────────┐ ┌─────────────┐
│ GatewayProcessor │ │RouteManager │ │ConnectionPool│ │MessageCodec │
│ (请求处理核心)  │ │(路由管理)    │ │Manager       │ │Manager      │
│               │ │             │ │(连接池管理)   │ │(协议转换)    │
└───────────────┘ └─────────────┘ └──────────────┘ └─────────────┘
        │               │               │              │
        └───────────────┴───────────────┴──────────────┘
                                │
                        ┌───────▼────────┐
                        │ NettyHttpServer │
                        │  (HTTP服务器)   │
                        └────────────────┘
```

### 请求处理流程

```
客户端请求 → NettyHttpServer → GatewayProcessor → 请求处理流水线
                                      │
                    ┌─────────────────┼─────────────────┐
                    ▼                 ▼                 ▼
             1. 请求验证        2. 协议转换      3. 路由匹配
                    │                 │                 │
                    ▼                 ▼                 ▼
             4. 前置过滤        5. 负载均衡      6. 连接管理
                    │                 │                 │
                    ▼                 ▼                 ▼
             7. 后端调用        8. 后置过滤      9. 协议转换
                    │                 │                 │
                    └─────────────────┴─────────────────┘
                                      │
                                      ▼
                                 响应返回客户端
```

## 🔧 核心组件详解

### 1. GatewayApplication - 应用入口
```java
// 独立运行的入口类，不依赖Spring Boot
public class GatewayApplication {
    public static void main(String[] args) {
        GatewayApplication app = new GatewayApplication();
        app.start();
    }
    
    public void start() {
        // 1. 创建引导器
        GatewayBootstrap bootstrap = new GatewayBootstrap();
        // 2. 初始化组件
        bootstrap.init();
        // 3. 启动服务
        bootstrap.start();
        // 4. 注册优雅关闭
        Runtime.getRuntime().addShutdownHook(...);
    }
}
```

**特点**：
- 简单的 main 方法启动
- 支持命令行运行：`java -cp "target/classes:target/dependency/*" com.muxin.gateway.core.plus.GatewayApplication`
- 自动注册关闭钩子，确保优雅停机

### 2. GatewayBootstrap - 引导器
```java
@Slf4j
public class GatewayBootstrap implements LifeCycle {
    // 配置
    private GatewayConfig gatewayConfig;
    private GlobalRouteConfig globalRouteConfig;
    
    // 核心组件
    private ConnectionPoolManager connectionPoolManager;
    private RouteManager routeManager;
    private InstanceManager instanceManager;
    private MessageCodecManager messageCodecManager;
    private GatewayProcessor gatewayProcessor;
    
    // 服务器
    private NettyHttpServer httpServer;
}
```

**职责**：
- 管理所有组件的生命周期
- 按依赖顺序初始化组件
- 协调组件间的依赖关系
- 支持热重载和动态配置更新

### 3. GatewayProcessor - 核心处理器

#### 优化的线程模型
```java
public class GatewayProcessor implements LifeCycle {
    /**
     * 处理入站请求 - 核心处理方法（单次线程切换优化版）
     * 
     * 线程模型：
     * 1. 同步执行阶段（当前线程）: 步骤1-6（CPU密集型操作）
     * 2. 异步执行阶段（业务线程池）: 步骤7-10（I/O密集型操作）
     * 
     * 性能优势：
     * - 减少90%线程切换开销（从10次降到1次）
     * - CPU操作连续执行，缓存友好
     * - 线程池压力显著降低
     */
    public final void processRequest(RequestContext context) {
        // 同步阶段：CPU密集型操作
        validateRequest(context);
        convertInboundProtocol(context);
        Route matchedRoute = matchRoute(context);
        executePreFilters(context);
        ServiceInstance selectedNode = selectTargetNode(context);
        ClientConnection connection = acquireConnection(context);
        
        // 唯一的线程切换点
        CompletableFuture.supplyAsync(() -> {
            // 异步阶段：I/O密集型操作
            Message response = invokeBackendService(context);
            executePostFilters(context);
            convertOutboundProtocol(context);
            return sendResponseSync(context);
        }, businessExecutor);
    }
}
```

**核心特性**：
- 优化的线程模型，最小化线程切换
- 清晰的处理阶段划分
- 完善的错误处理和资源清理
- 支持请求追踪和监控

### 4. 协议系统

#### Protocol - 协议抽象
```java
public interface Protocol {
    String type();                    // 协议类型 (HTTP, TCP, UDP等)
    String getVersion();              // 协议版本
    boolean isConnectionOriented();   // 是否面向连接
    boolean isRequestResponseBased(); // 是否请求-响应模式
    boolean isStreamingSupported();   // 是否支持流式传输
}
```

#### Message - 消息抽象
```java
public interface Message {
    String getMessageId();        // 消息ID
    MessageType getType();        // 消息类型
    Protocol getProtocol();       // 协议信息
    URL url();                    // 请求URL
    String method();              // 请求方法
    MessageHeaders getHeaders();  // 消息头
    MessageBody getBody();        // 消息体
    MessageMetadata getMetadata(); // 元数据
}
```

**已实现的协议**：
- ✅ HTTP/1.1
- ✅ LB (内部负载均衡协议)
- 🚧 gRPC (计划中)
- 🚧 WebSocket (计划中)

### 5. 路由系统

#### Route - 路由接口
```java
public interface Route {
    String getId();                           // 路由ID
    Protocol getSupportedProtocol();          // 支持的协议（单协议设计）
    List<Predicate> getPredicates();          // 断言列表
    List<Filter> getFilters();                // 过滤器列表
    RouteTarget getTarget();                  // 目标服务配置
    boolean matches(RequestContext context);  // 匹配请求
}
```

#### EnhancedRoute - 增强路由实现
```java
@Data
@Builder
public class EnhancedRoute implements Route {
    // 基础配置
    private final String id;
    private final String name;
    private final int order;
    private final boolean enabled;
    
    // 单协议配置
    private final Protocol inboundProtocol;
    private final List<Predicate> predicates;
    private final List<Filter> filters;
    private final RouteTarget target;
    private final TimeoutConfig timeouts;
}
```

**特点**：
- 单协议设计，简化复杂性
- 支持完整的路由配置
- 路由间配置完全隔离

### 6. 过滤器系统

#### Filter - 过滤器接口
```java
public interface Filter {
    void filter(RequestContext context, FilterChain chain);
    String getName();
    FilterType getType();    // PRE, ROUTE, POST, ERROR
    int getOrder();
    boolean isEnabled();
    Set<Protocol> getSupportedProtocols();
}
```

#### 已实现的过滤器

##### HttpAuthFilter - HTTP认证过滤器
```java
@Builder
public class HttpAuthFilter implements Filter {
    // 支持的认证类型
    private final String authType;    // JWT, BASIC, TOKEN
    private final String secretKey;
    private final String tokenParam;
    
    // 认证方法
    private boolean performJwtAuthentication(...);
    private boolean performBasicAuthentication(...);
    private boolean performTokenAuthentication(...);
}
```

**功能**：
- JWT Token 验证
- Basic 认证
- Token 参数认证
- 认证结果存储在上下文中

##### HttpLoggingFilter - HTTP日志过滤器
```java
@Builder
public class HttpLoggingFilter implements Filter {
    // 配置参数
    private final boolean includeHeaders;
    private final boolean includeBody;
    private final int maxBodySize;
    
    // 日志方法
    private void logRequest(RequestContext context);
    private void logResponse(RequestContext context, long startTime);
    private void logError(RequestContext context, Exception e, long startTime);
}
```

**功能**：
- 请求日志记录
- 响应日志记录
- 错误日志记录
- 可配置的日志详细程度

### 7. 负载均衡系统

#### LoadBalanceStrategy - 负载均衡策略接口
```java
public interface LoadBalanceStrategy {
    EndpointAddress select(List<EndpointAddress> addresses, RequestContext context);
    String getStrategyName();
    boolean requiresWeight();
    boolean isStateful();
    void reset();
    LoadBalanceStats getStats();
}
```

#### 已实现的策略

| 策略名称 | 类名 | 特点 | 状态 |
|---------|------|------|------|
| 轮询 | RoundRobinLoadBalanceStrategy | 依次选择，简单高效 | 有状态 |
| 随机 | RandomLoadBalanceStrategy | 随机选择，负载分散 | 无状态 |
| 加权轮询 | WeightedRoundRobinLoadBalanceStrategy | 按权重分配请求 | 有状态 |
| 最少连接 | LeastConnectionsLoadBalanceStrategy | 选择连接数最少的节点 | 有状态 |

### 8. 配置系统

#### GatewayConfig - 统一配置
```java
@Data
@Builder
public class GatewayConfig {
    private GatewayCoreConfig coreConfig;        // 核心配置
    private ConnectionPoolConfig connectionPoolConfig; // 连接池配置
    private RouteSystemConfig routeConfig;       // 路由配置
    private FilterConfig filterConfig;           // 过滤器配置
    private LoadBalanceConfig loadBalanceConfig; // 负载均衡配置
}
```

#### GlobalRouteConfig - 全局路由配置
```java
public class GlobalRouteConfig {
    private List<FilterDefinition> globalFilters;     // 全局过滤器
    private LoadBalanceDefinition defaultLoadBalance; // 默认负载均衡
    private TimeoutConfig defaultTimeouts;            // 默认超时配置
    private Map<String, Object> globalMetadata;       // 全局元数据
}
```

**配置优先级**：
1. 路由级配置（最高优先级）
2. 全局默认配置
3. 系统默认值

## 💡 使用示例

### 1. 基础配置示例
```yaml
# gateway-routes.yml
routes:
  - id: user-service-route
    name: 用户服务路由
    order: 1
    enabled: true
    supportProtocol:
      type: HTTP
      version: "1.1"
    predicates:
      - type: PATH
        patterns: 
          - "/api/users/**"
      - type: METHOD
        methods: 
          - GET
          - POST
    filters:
      - type: AUTH
        order: 1
        config:
          authType: JWT
          secretKey: "your-secret-key"
      - type: REQUEST_LOG
        order: 2
        config:
          includeHeaders: true
          includeBody: false
    target:
      type: SERVICE_DISCOVERY
      serviceName: user-service
      loadBalance:
        strategy: ROUND_ROBIN
    timeouts:
      connect: 5s
      request: 30s
```

### 2. 代码配置示例
```java
// 创建路由
Route userRoute = EnhancedRoute.builder()
    .id("user-route")
    .name("用户服务路由")
    .order(1)
    .enabled(true)
    .inboundProtocol(ProtocolEnum.HTTP)
    .predicates(Arrays.asList(
        new PathPredicate("/api/users/**"),
        new MethodPredicate("GET", "POST")
    ))
    .filters(Arrays.asList(
        HttpAuthFilter.builder()
            .authType("JWT")
            .secretKey("secret")
            .order(1)
            .build(),
        HttpLoggingFilter.builder()
            .includeHeaders(true)
            .order(2)
            .build()
    ))
    .target(RouteTarget.builder()
        .type(TargetType.SERVICE_DISCOVERY)
        .serviceName("user-service")
        .build())
    .build();

// 添加到路由管理器
routeManager.addRoute(userRoute);
```

### 3. 启动网关
```bash
# 编译项目
mvn clean compile

# 运行网关
java -cp "target/classes:target/dependency/*" \
     com.muxin.gateway.core.plus.GatewayApplication

# 或使用Maven插件
mvn exec:java
```

## 🚀 性能优化

### 1. 线程模型优化
- **单次线程切换**：将原来的10次线程切换优化到1次
- **CPU亲和性**：同步操作连续执行，提高CPU缓存命中率
- **异步I/O**：I/O密集型操作异步执行，不阻塞主线程

### 2. 内存优化
- **对象池化**：复用频繁创建的对象
- **零拷贝**：使用Netty的零拷贝特性
- **缓存优化**：使用Caffeine高性能缓存

### 3. 网络优化
- **连接池**：复用后端连接，减少连接开销
- **TCP优化**：调整TCP参数，提高传输效率
- **背压控制**：防止内存溢出

## 📊 监控指标

### 已支持的指标
- 请求总数
- 响应时间分布
- 错误率统计
- 路由命中率
- 后端服务健康状态
- 负载均衡分布

### 监控集成
```java
// 通过LoadBalanceStats获取负载均衡统计
LoadBalanceStats stats = strategy.getStats();
double avgSelectionTime = stats.getAverageSelectionTimeMs();
double selectionsPerSecond = stats.getSelectionsPerSecond();

// 通过RequestContext记录请求指标
context.setAttribute("request.start", System.currentTimeMillis());
context.setAttribute("route.id", matchedRoute.getId());
```

## 🔄 扩展点

### 1. 自定义过滤器
```java
public class CustomFilter implements Filter {
    @Override
    public void filter(RequestContext context, FilterChain chain) {
        // 前置处理
        doPreProcess(context);
        
        // 继续过滤器链
        chain.filter(context);
        
        // 后置处理
        doPostProcess(context);
    }
}
```

### 2. 自定义负载均衡策略
```java
public class CustomLoadBalanceStrategy implements LoadBalanceStrategy {
    @Override
    public EndpointAddress select(List<EndpointAddress> addresses, 
                                 RequestContext context) {
        // 实现自定义选择逻辑
        return selectByCustomLogic(addresses);
    }
}
```

### 3. 自定义协议支持
```java
public class CustomProtocol implements Protocol {
    @Override
    public String type() {
        return "CUSTOM";
    }
    
    // 实现其他方法...
}

public class CustomMessageCodec implements MessageCodec {
    @Override
    public Message convertToMessage(ProtocolData data, RequestContext context) {
        // 实现协议转换逻辑
    }
}
```

## 🚧 待实现功能

### 近期计划
1. **WebSocket支持**：实现WebSocket协议适配器
2. **gRPC支持**：实现gRPC协议转换
3. **熔断器**：实现Circuit Breaker过滤器
4. **限流器**：实现Rate Limiter过滤器
5. **缓存过滤器**：实现响应缓存

### 中期计划
1. **配置中心集成**：支持Nacos、Apollo等配置中心
2. **服务发现集成**：完善服务发现机制
3. **分布式追踪**：集成OpenTelemetry
4. **安全增强**：WAF功能、防爬虫

### 长期计划
1. **多集群支持**：跨数据中心路由
2. **灰度发布**：支持多种灰度策略
3. **GraphQL支持**：GraphQL协议网关
4. **Service Mesh集成**：与Istio等集成

## 📝 最佳实践

### 1. 路由设计
- 使用清晰的路由ID和名称
- 合理设置路由优先级
- 避免过于复杂的断言组合

### 2. 过滤器使用
- 按功能分层配置过滤器
- 注意过滤器执行顺序
- 避免在过滤器中执行耗时操作

### 3. 性能调优
- 根据业务特点选择负载均衡策略
- 合理设置连接池大小
- 监控关键性能指标

### 4. 错误处理
- 实现全局错误处理
- 提供友好的错误信息
- 记录详细的错误日志

## 🔗 相关文档

- [协议无关网关架构设计文档](./协议无关网关架构设计文档.md)
- [网关系统核心概念设计文档](./网关系统核心概念设计文档.md)
- [架构重构变更日志](./架构重构变更日志.md)

---

**文档版本**: v1.0  
**创建日期**: 2025-01-20  
**最后更新**: 2025-01-20  
**作者**: Muxin Gateway Team 