# Muxin Gateway

一个基于 Netty 的高性能 API 网关系统，提供轻量级核心引擎和完整的管理后台。

## 项目简介

Muxin Gateway 是一个现代化的 API 网关解决方案，采用分层架构设计：
- **gateway-core**: 轻量级核心引擎，基于 Netty 实现高性能转发
- **gateway-admin**: 完整的后台管理系统，支持路由、插件、用户、权限等管理
- **gateway-main**: 集成启动模块，开箱即用

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 启动步骤

1. **创建数据库**
```sql
CREATE DATABASE muxin_gateway DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **导入数据**
执行 `gateway-admin/src/main/resources/schema.sql` 和 `data.sql`

3. **修改配置**
编辑 `gateway-main/src/main/resources/application.yml`，配置数据库连接

4. **启动服务**
```bash
# 编译打包
mvn clean package -DskipTests

# 启动服务
java -jar gateway-main/target/gateway-main-1.0-SNAPSHOT.jar
```

5. **访问系统**
- 管理后台: http://localhost:9191/
- 默认账号: admin / admin123

## 项目结构

```
muxin-gateway/
├── gateway-bom/                    # 依赖版本管理
├── gateway-core/                   # 核心引擎模块
│   └── src/main/java/com/muxin/gateway/core/
│       ├── config/                 # 配置加载
│       ├── route/                  # 路由系统
│       │   ├── filter/             # 过滤器
│       │   ├── predicate/          # 断言
│       │   └── loadbalance/        # 负载均衡
│       ├── service/                # 服务注册发现
│       ├── connect/                # 连接池管理
│       └── server/                 # Netty HTTP服务器
├── gateway-admin/                  # 后台管理模块
│   └── src/main/java/com/muxin/gateway/admin/
│       ├── controller/             # REST接口
│       ├── service/                # 业务逻辑
│       ├── mapper/                 # 数据访问
│       ├── entity/                 # 实体类
│       └── model/                  # DTO/VO
├── gateway-admin-ui/               # 前端界面
│   └── src/
│       ├── views/                  # 页面组件
│       ├── stores/                 # 状态管理
│       ├── router/                 # 路由配置
│       └── api/                    # API接口
├── gateway-cloud/                  # 云原生模块
│   └── gateway-cloud-discovery/    # 服务发现
└── gateway-main/                   # 启动模块
```

## 核心功能

### 网关核心 (gateway-core)
- HTTP/1.1 协议支持
- 动态路由配置
- 断言匹配（Path、Method、Header、Query、Cookie、Host）
- 过滤器链（请求/响应重写、限流、熔断、CORS、超时控制）
- 负载均衡（轮询、随机、加权轮询）
- 连接池管理
- YAML 配置驱动

### 管理后台 (gateway-admin)

#### 路由管理
- 路由增删改查
- 路由启用/禁用
- 路由配置导入导出
- 路由匹配测试

#### 服务节点管理
- 服务注册发现
- 节点健康检查
- 节点状态管理
- 支持从 Nacos、Consul 导入

#### 插件管理
- 内置插件：限流、熔断、CORS、超时、请求/响应重写
- 插件配置管理
- 插件优先级设置
- 插件启用/禁用

#### 系统管理
- **用户管理**: 用户增删改查、角色分配、密码重置、状态管理
- **角色管理**: 角色权限配置、数据范围设置
- **部门管理**: 树形组织架构、拖拽排序
- **菜单管理**: 动态菜单配置、权限标识
- **配置管理**: 系统参数配置
- **操作日志**: 操作记录查询

#### 权限系统
- RBAC 权限模型
- 数据权限（全部、自定义、本部门、本部门及以下、仅本人）
- 动态路由加载
- 按钮/接口权限控制

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3.5 | 基础框架 |
| Netty | 4.1.x | 网络通信 |
| MyBatis-Flex | 1.9.7 | ORM框架 |
| MySQL | 8.0 | 数据库 |
| Sa-Token | 1.39.0 | 认证授权 |
| Hutool | 5.8.32 | 工具库 |
| Jackson | 2.17.x | JSON处理 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| Element Plus | 2.4.x | UI组件库 |
| Pinia | 2.1.x | 状态管理 |
| Vue Router | 4.2.x | 路由管理 |
| TypeScript | 5.x | 类型支持 |
| Vite | 5.x | 构建工具 |

## API 文档

启动服务后访问: http://localhost:9191/doc.html (Swagger UI)

## 配置说明

### 网关核心配置 (gateway-routes.yml)
```yaml
routes:
  - id: user-service
    name: 用户服务
    uri: lb://user-service
    predicates:
      - Path=/api/users/**
      - Method=GET,POST
    filters:
      - name: RequestRateLimiter
        args:
          rate: 10
          burst: 20
    load-balance:
      strategy: ROUND_ROBIN
```

### 应用配置 (application.yml)
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/muxin_gateway
    username: root
    password: root

# 网关配置
gateway:
  core:
    enabled: true
    port: 9090
  admin:
    enabled: true
```

## 开发指南

### 本地开发
```bash
# 启动后端
cd gateway-admin
mvn spring-boot:run

# 启动前端
cd gateway-admin-ui
npm install
npm run dev
```

### 构建部署
```bash
# 构建后端
mvn clean package

# 构建前端
cd gateway-admin-ui
npm run build

# 打包部署
java -jar gateway-main-1.0-SNAPSHOT.jar
```

## 版本历史

### v1.0.0 (2025-04)
- 完成核心网关功能
- 实现路由管理、服务节点管理、插件管理
- 完整的用户权限系统（用户、角色、部门、菜单）
- 数据权限支持
- 前后端一体化部署

## 贡献指南

欢迎提交 Issue 和 Pull Request。

## 许可证

[Apache License 2.0](LICENSE)