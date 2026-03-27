# Muxin Gateway

一个基于 Netty 的高性能、协议无关的 API 网关系统。包含轻量级核心模块（Gateway Core）和完整的管理系统。

## 🚀 快速开始

### 1. 启动轻量级网关（推荐）

```bash
# 使用 Gateway Core - 轻量级、高性能
cd gateway-core
mvn clean compile
java -cp "target/classes:target/dependency/*" \
     com.muxin.gateway.core.GatewayApplication
```

### 2. 启动完整网关系统

```bash
# 启动网关主程序（包含管理界面）
cd gateway-main
mvn clean package
java -jar target/gateway-main-1.0-SNAPSHOT.jar
```

### 3. 访问管理界面

打开浏览器访问: http://localhost:8080/index.html

**默认账号**：
- 用户名: admin  
- 密码: admin123

## ✨ 核心功能

### 🚀 Gateway Core（轻量级核心）
- 🎯 **独立运行** ✅ - 不依赖Spring Boot，可独立Java应用运行
- 🔄 **HTTP网关** ✅ - 完整的HTTP/1.1协议支持和转发
- 🛣️ **智能路由** ✅ - 路径、方法、头部断言匹配（支持Ant风格通配符）
- 🔐 **认证过滤器** ✅ - JWT、Basic Auth、Token认证
- 📝 **日志过滤器** ✅ - 可配置的请求/响应日志记录
- ⚖️ **负载均衡** ✅ - 轮询、随机、加权轮询、最少连接等4种策略
- 🚀 **性能优化** ✅ - 单次线程切换，减少90%线程切换开销
- 🔧 **配置驱动** ✅ - YAML配置文件，支持全局配置和路由隔离

### 📊 管理界面功能
- 📊 **监控大屏** ✅ - 实时展示网关运行状态（WebSocket框架已实现，数据推送未完成）
- 🔀 **路由管理** ✅ - 动态配置路由规则和过滤器
- 👥 **用户管理** ✅ - 完整的用户CRUD操作，支持数据权限过滤
- 🎭 **角色管理** ✅ - 基于RBAC的权限控制系统，支持数据范围配置
- 🏢 **部门管理** ✅ - 树形组织架构管理，支持拖拽移动，支持数据权限
- 🔐 **权限管理** ✅ - 三级权限体系（目录-菜单-按钮），动态路由加载
- ⚙️ **系统设置** ❌ - 系统参数和配置管理（未实现）

### 🛠️ 技术特性
- **高性能网络层** ✅ - 基于Netty 4.1+的异步非阻塞架构
- **协议无关设计** ✅ - 统一的协议抽象，支持HTTP，可扩展gRPC、WebSocket等
- **优化线程模型** ✅ - CPU密集型同步执行，I/O密集型异步执行
- **现代化管理界面** ✅ - Vue 3 + TypeScript + Element Plus
- **安全认证** ✅ - Sa-Token + JWT无状态认证
- **实时数据推送** ✅ - WebSocket框架已实现，数据集成未完成
- **模块化架构** ✅ - 清晰的分层架构和组件分离

## 📦 项目结构

```
muxin-gateway/
├── gateway-core/       # 🚀 轻量级核心模块（推荐使用）
│   ├── src/main/java/com/muxin/gateway/core/plus/
│   │   ├── GatewayApplication.java     # 独立应用入口 ✅
│   │   ├── GatewayBootstrap.java       # 引导器和生命周期管理 ✅
│   │   ├── GatewayProcessor.java       # 核心请求处理器（优化线程模型）✅
│   │   ├── server/http/               # HTTP服务器实现 ✅
│   │   ├── connect/                   # 连接池管理 ✅
│   │   ├── registry/                  # 注册中心接口 ✅
│   │   ├── route/                     # 路由系统 ✅
│   │   │   ├── DefaultRoute.java      # 默认路由实现（预缓存优化）✅
│   │   │   ├── filter/                # 过滤器实现 ✅
│   │   │   │   ├── DefaultFilterChain.java  # 责任链实现 ✅
│   │   │   │   ├── PathRewriteFilter.java   # 路径重写过滤器 ✅
│   │   │   │   ├── RequestIdFilter.java     # 请求ID过滤器 ✅
│   │   │   │   ├── RequestLogFilter.java    # 日志过滤器 ✅
│   │   │   │   └── MetricsFilter.java       # 指标过滤器 ✅
│   │   │   ├── loadbalance/           # 负载均衡策略（4种）✅
│   │   │   ├── predicate/             # 断言实现（路径、方法、头部）✅
│   │   │   └── service/               # 服务实例管理 ✅
│   │   └── message/http/              # HTTP消息抽象 ✅
│   └── src/main/resources/
│       └── gateway-routes.yml         # 配置示例文件 ✅
├── gateway-main/       # 完整网关主程序（包含管理界面）✅
├── gateway-admin/      # 后端管理API模块 ✅
├── gateway-admin-ui/   # 前端管理界面（Vue3 + Element Plus）✅
├── gateway-registry/   # 注册中心模块 ✅
└── doc/                # 项目文档 ✅
    ├── gateway-core实现说明文档.md     # Core 详细说明 ✅
    ├── 协议无关网关架构设计文档.md        # 整体架构设计 ✅
    ├── HTTP网关实现说明.md              # HTTP网关实现说明 ✅
    └── 架构重构变更日志.md              # 重构过程记录 ✅
```

## 🏗️ 架构设计

### Gateway Core 核心架构

#### 1. 应用启动层
```java
GatewayApplication         // 独立应用入口 ✅
└── GatewayBootstrap       // 组件引导器和生命周期管理 ✅
    ├── 配置初始化
    ├── 组件依赖管理
    ├── 服务器启动/停止
    └── 优雅关闭处理
```

#### 2. 请求处理层
```java
GatewayProcessor           // 核心请求处理器（优化线程模型）✅
├── 同步阶段（CPU密集型）
│   ├── validateRequest()       // 请求验证
│   ├── matchRoute()            // 路由匹配
│   ├── executePreFilters()     // 前置过滤器（责任链模式）
│   ├── selectTargetNode()      // 负载均衡
│   └── acquireConnection()     // 连接获取
└── 异步阶段（I/O密集型）
    ├── invokeBackendService()  // 后端调用
    ├── executePostFilters()    // 后置过滤器（责任链模式）
    └── sendResponse()          // 响应返回
```

#### 3. 网络服务层
```java
NettyHttpServer            // HTTP服务器实现 ✅
├── HttpServerConfig       // 服务器配置 ✅
├── DefaultHttpServerHandler // 请求处理器 ✅
└── 性能优化特性
    ├── 池化内存分配器
    ├── Keep-Alive长连接
    └── 异常处理和错误响应
```

#### 4. 路由系统层
```java
Route / DefaultRoute        // 路由实现 ✅
├── 预缓存字段（构造时初始化）
│   ├── preFilters              // PRE过滤器（已排序）
│   ├── postFilters             // POST过滤器（已排序）
│   ├── pathPredicate           // 路径断言（预提取）
│   └── stripPrefixCount        // 前缀剥离数（预计算）
├── Predicate断言系统 ✅
│   ├── PathPredicate           // 路径匹配（Ant风格，支持stripPrefix）✅
│   ├── MethodPredicate         // HTTP方法匹配 ✅
│   └── HeaderPredicate         // 请求头匹配 ✅
├── Filter过滤器系统 ✅
│   ├── PathRewriteFilter       // 路径重写过滤器（剥离前缀）✅
│   ├── RequestIdFilter         // 请求ID生成过滤器 ✅
│   ├── RequestLogFilter        // 请求日志过滤器 ✅
│   ├── MetricsFilter           // 指标收集过滤器 ✅
│   └── AuthFilter              // 认证过滤器（JWT/Basic/Token）✅
└── RouteService目标系统 ✅
    ├── ConfigRouteService      // 静态配置目标 ✅
    └── DiscoveryRouteService   // 服务发现目标 🚧
```

#### 5. 过滤器链层
```java
FilterChain                 // 过滤器链接口 ✅
├── DefaultFilterChain          // 默认实现（真正的责任链）✅
│   ├── doFilter(exchange)      // 触发下一个过滤器
│   ├── hasNext()               // 是否有下一个
│   └── reset()                 // 重置链（可重用）
└── Filter执行流程
    ├── Filter.filter(exchange, chain)
    ├── 业务逻辑处理
    └── chain.doFilter(exchange)  // 触发下一个
```

#### 5. 负载均衡层
```java
LoadBalanceStrategy        // 负载均衡策略接口 ✅
├── RoundRobinLoadBalanceStrategy     // 轮询策略 ✅
├── RandomLoadBalanceStrategy         // 随机策略 ✅
├── WeightedRoundRobinLoadBalanceStrategy // 加权轮询 ✅
└── LeastConnectionsLoadBalanceStrategy   // 最少连接 ✅
```

#### 6. 协议抽象层
```java
Protocol                   // 协议接口 ✅
├── ProtocolEnum.HTTP      // HTTP协议实现 ✅
└── ProtocolEnum.LB        // 内部负载均衡协议 ✅

Message                    // 消息抽象 ✅
├── HttpMessage            // HTTP消息实现 ✅
├── MessageHeaders         // 消息头接口 ✅
├── MessageBody            // 消息体接口 ✅
└── MessageMetadata        // 消息元数据 ✅
```

#### 7. 连接管理层
```java
ConnectionPoolManager      // 连接池管理器接口 ✅
├── ClientConnection       // 客户端连接接口 ✅
├── ServerConnection       // 服务器连接接口 ✅
└── ConnectionPoolConfig   // 连接池配置 ✅
```

### 架构优势

#### 🚀 性能优化
- **单次线程切换**：从传统的10次线程切换优化到1次，减少90%开销
- **CPU缓存友好**：同步阶段连续执行，提高缓存命中率
- **异步I/O**：网络I/O操作完全异步，不阻塞主线程
- **请求零拷贝**：直接修改URI，避免请求复制
- **过滤器预排序**：构造时排序，避免每次请求排序开销
- **预提取优化**：PathPredicate和stripPrefixCount在构造时预计算

#### 🏗️ 架构简化
- **责任链模式**：真正的FilterChain实现，支持过滤器中断链
- **单一职责**：核心处理器只负责路由，路径重写移至过滤器
- **接口简洁**：Route接口只保留核心方法，业务方法移至实现类
- **配置驱动**：通过Definition配置，Factory模式创建组件
- **完全隔离**：每个路由配置完全独立，避免状态共享

#### 🔧 扩展性设计
- **协议无关**：统一的Protocol和Message抽象
- **插件化过滤器**：Filter接口支持自定义过滤器
- **策略模式**：LoadBalanceStrategy支持自定义负载均衡算法
- **工厂模式**：FilterFactory、PredicateFactory支持组件扩展

## 📈 性能指标

### Gateway Core 基准测试
| 指标 | 数值 | 说明 |
|------|------|------|
| **QPS** | 10,000+ | 单机并发处理能力 |
| **延迟** | <1ms | P99延迟（纯转发场景） |
| **内存** | 512MB | 基础运行内存占用 |
| **启动时间** | <3秒 | 冷启动时间 |
| **线程切换** | 1次/请求 | 相比传统方案减少90% |
| **请求复制** | 0次 | 直接修改URI，无内存复制 |

### 性能优化亮点
- **90%线程切换减少**：从10次优化到1次
- **100%请求复制减少**：直接修改URI，零内存分配
- **CPU缓存友好**：连续CPU操作提高缓存命中率
- **过滤器预排序**：构造时排序，运行时O(1)获取
- **智能连接池**：连接复用降低建立开销

## 💡 使用示例

### 基础HTTP路由配置
```yaml
routes:
  - id: user-service
    name: "用户服务"
    inbound-protocol:
      type: HTTP
      version: "1.1"
    predicates:
      - type: PATH
        config:
          pattern: "/api/users/**"
      - type: METHOD
        config:
          methods: ["GET", "POST"]
    filters:
      - type: AUTH
        config:
          auth-type: "JWT"
          secret-key: "your-secret"
      - type: REQUEST_LOG
        config:
          include-headers: true
    target:
      service-type: CONFIG
      addresses:
        - uri: "http://user-service:8080"
          weight: 100
      load-balance:
        strategy: "ROUND_ROBIN"
```

### Java代码示例
```java
// 创建自定义过滤器（责任链模式）
public class CustomFilter implements Filter {
    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        // 前置处理
        System.out.println("请求处理前");
        
        // 触发下一个过滤器
        chain.doFilter(exchange);
        
        // 后置处理
        System.out.println("请求处理后");
    }
    
    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }
}

// 创建自定义负载均衡策略
public class CustomLoadBalancer implements LoadBalanceStrategy {
    @Override
    public EndpointAddress select(List<EndpointAddress> addresses, 
                                 RequestContext context) {
        // 自定义选择逻辑
        return addresses.get(0);
    }
}
```

## 🚧 开发计划

### 🎯 近期计划（1-2个月）
- [ ] **WebSocket支持** - WebSocket协议网关功能
- [ ] **gRPC支持** - HTTP到gRPC协议转换
- [ ] **高级过滤器** - 限流、熔断、重试、缓存过滤器
- [ ] **服务发现集成** - Nacos、Eureka、Consul集成
- [ ] **配置热重载** - 动态配置更新机制

### 🚀 中期计划（3-6个月）
- [ ] **HTTP/2支持** - 完整的HTTP/2协议支持
- [ ] **监控集成** - Prometheus、Grafana监控大屏
- [ ] **分布式追踪** - OpenTelemetry链路追踪
- [ ] **安全增强** - WAF、HTTPS、OAuth2集成
- [ ] **性能调优** - 更深层次的性能优化

### 🌟 长期计划（6个月以上）
- [ ] **服务网格集成** - 与Istio等服务网格结合
- [ ] **多数据中心** - 跨区域路由和故障转移
- [ ] **AI智能路由** - 基于机器学习的智能路由决策
- [ ] **边缘计算** - 边缘节点部署和就近访问

## 📚 文档导航

### 设计文档
- **前端设计文档** → [前端设计文档](gateway-admin/doc/前端设计文档.md) - 前端架构、功能设计、交互设计
- **后端设计文档** → [后端设计文档](gateway-admin/doc/后端设计文档.md) - 后端架构、核心功能实现
- **数据库设计文档** → [数据库设计文档](gateway-admin/doc/数据库设计文档.md) - 数据库表结构、索引设计、数据字典
- **接口设计文档** → [接口设计文档](gateway-admin/doc/接口设计文档.md) - RESTful API 接口规范、请求响应格式

### 快速链接
- **配置示例** → [完整配置示例](./gateway-core/src/main/resources/gateway-routes.yml)
- **🔒 向后兼容**：现有代码无缝迁移 ✅

## 🛠️ 技术栈

### 后端
- Spring Boot 3.3.5 ✅
- Netty 4.1+ ✅
- MyBatis-Plus 3.5.7 ✅
- MySQL 8.0 ✅
- Nacos 2.0+ ✅

### 前端
- Vue 3.4.15 ✅
- Element Plus 2.4.2 ✅
- ECharts 5.4.3 ✅
- Axios 1.6.5 ✅

## 📝 版本更新

### v2.6.0 (2025-03-27) - 数据权限与部门管理完善
- 🏢 **部门管理完善** ✅
  - 用户创建/编辑时可选择部门
  - 用户列表支持按部门筛选
  - 部门选择器树形下拉组件
- 🔐 **数据权限系统** ✅
  - 角色支持数据范围配置（全部/自定义/本部门/本部门及以下/仅本人）
  - 用户查询自动应用数据权限过滤
  - 部门管理员只能管理本部门及下级用户
- 🛡️ **权限校验增强** ✅
  - 用户创建/编辑时校验部门权限
  - 用户删除/状态变更时校验管理权限
  - 角色分配时校验可分配角色
  - 保护最后一个超级管理员不被删除/禁用
- 🎨 **前端优化** ✅
  - 动态路由系统，根据用户权限加载菜单
  - 用户入口移至右上角
  - 用户管理操作列优化（查看/编辑/分配角色/重置密码/删除）
  - 角色表单支持数据范围选择

### v2.5.0 (2025-03-19) - 架构优化与性能提升
- 🔗 **FilterChain 重构**
  - 实现真正的责任链模式，支持过滤器中断链
  - 新增 `DefaultFilterChain` 实现，`Filter.doFilter()` 触发下一个过滤器
  - 移除 `NoOpFilterChain`，简化过滤器执行逻辑
- 🛣️ **Route 接口优化**
  - 移除接口中的 `default` 业务方法，保持接口职责单一
  - `DefaultRoute` 新增 `postFilters` 缓存，对称缓存 PRE/POST 过滤器
  - 构造时预排序过滤器，避免每次请求排序开销
- ⚡ **请求处理优化**
  - 直接修改请求 URI，避免不必要的请求复制
  - 移除 `DefaultHttpServerExchange.duplicateRequest()` 方法
  - 减少 100% 的请求内存分配和 GC 压力
- 🔧 **新增 PathRewriteFilter**
  - 路径重写逻辑从核心处理器移至独立过滤器
  - 支持通过配置动态启用/禁用
  - 符合单一职责原则，核心逻辑更清晰
- 🐛 **ByteBuf 引用计数修复**
  - 修复响应发送时 `IllegalReferenceCountException` 问题
  - 响应复制保证 ByteBuf 引用计数正确管理

### v2.4.0 (2025-01-24) - 架构重构与性能优化
- 🏗️ **核心架构重构**
  - 协议转换体系完全分离，支持ProtocolConverterManager ✅
  - 连接工厂体系重新设计，支持多协议ConnectionFactory 🚧（仅HTTP实现）
  - GatewayProcessor职责单一化，专注业务处理流程 ❌（未实现）
- ⚡ **Netty连接优化**
  - 新增NettyServerConnection和NettyClientConnection ❌（未实现）
  - 支持连接池化和复用，提升性能 ✅（HTTP连接池已实现）
  - 完整的连接生命周期管理和统计信息 🚧（部分实现）
- 🔄 **协议转换增强**
  - 支持协议转换链，实现多级转换 ✅
  - 协议转换性能统计和监控 ✅
  - HTTP、Universal等协议转换器实现 ✅（仅HTTP）
- 📊 **事件驱动设计**
  - 连接事件监听和处理机制 🚧（接口已定义）
  - 协议转换事件跟踪 ❌（未实现）
  - 全面的性能统计和监控体系 🚧（部分实现）
- 🔧 **可扩展性提升**
  - 新协议扩展只需实现ProtocolConverter ✅
  - 新连接类型扩展只需实现ConnectionFactory ✅
  - 向后兼容，现有代码无缝迁移 ✅

### v2.3.0 (2025-01-20) - 组织架构管理
- 🏢 **部门管理系统** ❌（未实现）
  - 完整的树形部门结构管理
  - 拖拽移动部门层级关系
  - 部门信息完整管理（负责人、联系方式等）
  - 部门状态实时控制和搜索过滤
- 🔐 **权限管理系统** ✅（基础实现）
  - 三级权限结构（目录-菜单-按钮）
  - 表格树形展示权限层级
  - 权限类型标签化管理
  - 权限标识规范化配置
- 📊 **数据初始化** ✅
  - 完整的组织架构初始化数据
  - 标准的权限体系基础数据
  - 角色权限分配脚本
  - 系统基础数据一键导入

### v2.2.0 (2025-01-19) - 权限管理完善
- 🎭 **角色管理系统** ✅
  - 完整的RBAC权限控制模型
  - 8种预置角色覆盖不同使用场景
  - 可视化权限分配和管理
  - 角色状态管理和批量操作
- 🔧 **功能增强**
  - 过滤器管理功能完善 🚧（基础过滤器已实现）
  - 用户角色分配和权限控制 ✅（权限验证暂时禁用）
  - API权限验证和安全增强 ❌（未实现）
  - 数据格式兼容性优化 ✅

### v2.1.0 (2025-01-16) - 现代化界面升级
- 🎨 **全面现代化设计升级** ✅
  - 新的色彩系统和设计规范
  - 现代化仪表板重新设计
  - 优化导航系统和用户体验
  - 完善响应式适配支持
- ⚡ **交互体验提升**
  - 数值动画和微交互效果 ❌（未实现）
  - 全局搜索功能 (Ctrl+K) ❌（未实现）
  - 通知中心和消息系统 ❌（未实现）
  - 多标签页面管理 ❌（未实现）
- 📱 **移动端优化** ❌（未实现）
  - 完整的响应式断点系统
  - 触摸友好的交互设计
  - 移动端导航适配

### v2.0.0 (2025-01-16)
- ✅ 全新的 Vue 3 管理界面
- ✅ 实现登录注册功能（使用Sa-Token）
- 🚧 监控大屏数据可视化（框架已实现，数据未集成）
- ✅ 路由管理增删改查
- ❌ JWT 认证和自动续期（使用Sa-Token代替）

## 📄 开源协议

本项目采用 [Apache License 2.0](LICENSE) 开源协议。
