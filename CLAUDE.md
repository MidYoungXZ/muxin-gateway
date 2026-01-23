# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 在本仓库中工作时提供指导。

## 项目概述

Muxin Gateway Plus 是一个基于 Netty 的高性能、协议无关的 API 网关系统。由轻量级核心模块（Gateway Core Plus）和完整的 Spring Boot 集成管理系统组成。

**核心特性：**
- 轻量级核心模块，可不依赖 Spring Boot 独立运行
- 协议无关设计，支持 HTTP、gRPC、WebSocket、TCP
- 优化的线程模型，单次线程切换（相比传统方案减少 90% 开销）
- YAML 驱动配置，支持静态和动态路由
- 插件式过滤器和负载均衡策略系统

## 构建和运行命令

### 构建命令

```bash
# 构建整个项目
mvn clean install

# 构建特定模块
mvn clean install -pl gateway-core-plus

# 构建并运行测试
mvn clean install -DskipTests=false

# 打包部署
mvn clean package
```

### 运行命令

```bash
# 运行轻量级网关核心（推荐）
cd gateway-core-plus
mvn clean compile
java -cp "target/classes:target/dependency/*" com.muxin.gateway.core.plus.GatewayApplication

# 运行完整网关系统（含管理界面）
cd gateway
mvn clean package
java -jar target/gateway-1.0-SNAPSHOT.jar

# 使用自定义配置文件运行
java -jar target/gateway-1.0-SNAPSHOT.jar --spring.config.location=classpath:/application.yml
```

### 访问地址

- **网关 HTTP**: http://localhost:8080
- **管理界面**: http://localhost:8080/index.html
- **默认凭据**: admin / admin123

## 模块架构

### 核心模块

```
muxin-gateway-plus/
├── gateway-core-plus/     # 轻量级核心（独立运行，无 Spring）
├── gateway/                # 完整网关（Spring Boot）
├── gateway-admin/          # 管理后台 API
├── gateway-admin-ui/       # Vue 3 前端
├── gateway-registry/       # 服务发现（Nacos）
├── gateway-config/         # 配置中心
└── gateway-spring-boot-starter/  # Spring Boot 集成
```

### Gateway Core Plus 结构

核心模块按职责组织：

- **GatewayApplication.java** - 独立入口点
- **GatewayBootstrap.java** - 组件生命周期管理器
- **GatewayProcessor.java** - 请求处理编排器（混合同步/异步模型）
- **server/http/** - Netty HTTP 服务器实现
- **connect/** - 连接池管理
- **route/** - 路由系统（断言、过滤器、负载均衡、服务发现）
- **protocol/message/** - 协议抽象层
- **config/** - YAML 配置加载和解析

## 请求处理流程

系统采用**混合同步/异步处理模型**以获得最佳性能：

```
GatewayProcessor.processRequest(RequestContext)
│
├─ 同步阶段（CPU 密集型，无线程切换）
│   ├─ validateRequest()
│   ├─ matchRoute() → RouteManager.matchRoute()
│   ├─ executePreFilters()
│   ├─ selectTarget() → RouteService.selectTarget() + LoadBalanceStrategy.select()
│   └─ acquireConnection() → ConnectionPoolManager.getClientConnection()
│
└─ 异步阶段（I/O 密集型，单次线程切换）
    ├─ invokeBackendService() → ClientConnection.send()
    ├─ executePostFilters()
    └─ sendResponse() → ServerConnection.sendResponse()
```

**性能优势**：相比传统异步框架，单次线程切换减少 90% 开销。

## 路由系统

### 配置文件

**`gateway-core-plus/src/main/resources/gateway-routes.yml`** 是主路由配置：

```yaml
# 服务（静态 CONFIG 或动态 DISCOVERY）
services:
  - id: user-service-001
    name: "user-service"
    type: CONFIG  # 或 DISCOVERY
    supported-protocols: [HTTP]
    addresses:  # CONFIG 类型使用
      - uri: "http://user-service:8080"
        weight: 100

# 路由引用服务
routes:
  - id: user-service-route
    name: "用户服务"
    service-ref: user-service-001  # 引用服务 ID
    protocol: HTTP
    predicates:
      - type: PATH
        config:
          pattern: "/api/users/**"
    filters:
      - type: AUTH
        config:
          auth-type: "JWT"
```

### 路由匹配

- **RouteManager.matchRoute()** (DefaultRouteManager:78-159) - 按协议、优先级、断言匹配路由
- 路由按协议类型缓存并预排序
- 支持 PATH（Ant 风格）、METHOD、HEADER 断言

### 负载均衡

`gateway-core-plus/src/main/java/com/muxin/gateway/core/plus/route/loadbalance/` 中的策略：

- **RoundRobinLoadBalanceStrategy** - 顺序选择
- **RandomLoadBalanceStrategy** - 随机选择
- **WeightedRoundRobinLoadBalanceStrategy** - 权重选择
- **LeastConnectionsLoadBalanceStrategy** - 选择活动连接最少的端点

### 服务类型

- **ConfigRouteService** - YAML 静态地址配置
- **DiscoveryRouteService** - Nacos 动态服务发现（带缓存）

## 过滤器系统

过滤器定义于 `gateway-core-plus/src/main/java/com/muxin/gateway/core/plus/route/filter/`：

- **REQUEST_ID** - 添加 X-Request-ID 头
- **CORS** - 跨域资源共享
- **REQUEST_LOG** - 请求/响应日志
- **AUTH** - JWT/Basic/Token 认证
- **METRICS** - 性能指标收集
- **RATE_LIMIT** - 限流

过滤器生命周期类型：
- **PRE** - 后端调用前
- **POST** - 后端响应后
- **ERROR** - 处理错误时

## 协议抽象

系统使用统一的协议接口：

- **Protocol** 接口 - 定义协议类型、版本和特性
- **Message** 接口 - 协议无关的消息抽象
- **ServerExchange** - 请求/响应交换容器
- **ProtocolConverter** - 协议间转换（HTTP→gRPC 等）

当前实现：
- **HTTP** - 完整 HTTP/1.1 支持
- **Universal** - 内部负载均衡协议

## 连接管理

- **ConnectionPoolManager** - 管理 HTTP 连接池
- **ClientConnection** - 网关到后端连接
- **ServerConnection** - 客户端到网关连接
- 连接池化复用以提升性能

## 配置文件

关键配置位置：

| 文件 | 用途 |
|------|------|
| `gateway-core-plus/src/main/resources/gateway-routes.yml` | 路由、服务、过滤器、负载均衡 |
| `gateway/src/main/resources/application.yml` | Spring Boot 应用配置 |
| `gateway/src/main/resources/application-performance.yml` | 性能调优 |

## 当前实现状态

参考 **待办事项.md** 获取最新实现状态。

**最近完成（2025-01-23）：**
- RouteManager.matchRoute() - 路由匹配（协议过滤、优先级排序）
- RouteService.selectTarget() - 端点选择（ConfigRouteService/DiscoveryRouteService 已优化）

**进行中：**
- setResponseToExchange() - 设置后端响应到 Exchange
- 连接池管理
- ClientConnection.send() - 后端调用
- ServerConnection 发送方法 - 响应发送给客户端

## 扩展点

### 添加自定义过滤器

```java
public class CustomFilter implements Filter {
    @Override
    public void filter(ServerExchange exchange, FilterChain chain) {
        // 前置处理
        chain.filter(exchange);
        // 后置处理
    }
}
```

### 添加自定义负载均衡

```java
public class CustomLoadBalancer extends LoadBalanceStrategy {
    public CustomLoadBalancer(LoadBalanceDefinition definition) {
        super(definition);
    }

    @Override
    public EndpointAddress select(List<EndpointAddress> addresses, RequestContext context) {
        // 自定义选择逻辑
        return addresses.get(0);
    }
}
```

### 添加新协议

1. 实现 **Protocol** 接口
2. 创建 **Message** 实现
3. 实现转换用的 **ProtocolConverter**
4. 在 **ProtocolConverterManager** 中注册

## 技术栈

- **Java 17** - 基础语言版本
- **Spring Boot 3.3.5** - 完整网关应用框架
- **Netty 4.1+** - 高性能网络层
- **Jackson 2.15.2** - YAML/配置解析
- **Nacos 2.0.4** - 服务发现
- **MyBatis-Flex 3.9.8** - 数据库访问（管理模块）
- **Sa-Token 1.37.0** - 认证
- **Log4j2** - 日志（Disruptor 异步支持）
- **Lombok** - 代码生成

## 性能考量

- **线程模型**：混合同步/异步，单次线程切换
- **连接池化**：HTTP 连接跨请求复用
- **缓存**：路由按协议缓存，服务发现带 TTL 缓存
- **零拷贝**：Netty 零拷贝网络 I/O
- **路由匹配**：O(n)，n 为每个协议的路由数（非总路由数）

## 数据库结构

管理模块使用 MySQL 8.0，关键表：
- `sys_user`, `sys_role`, `sys_menu` - RBAC 模型
- `sys_dept` - 部门层级
- `gateway_route` - 路由配置
- `sys_oper_log` - 操作日志

## 代码风格约定

- **命名**：变量/方法用 camelCase，类用 PascalCase，常量用 UPPER_SNAKE_CASE
- **缩进**：Java 用 4 空格，Vue/TypeScript 用 2 空格
- **行长度**：建议 ≤ 120 字符
- **注释**：公共 API 需 Javadoc；中文注释可接受
- **日志**：使用 SLF4J + Log4j2；禁止使用 System.out.println
- **错误处理**：具体异常类型 + 有意义的错误消息
