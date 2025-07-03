# Muxin Gateway

一个基于 Netty 和 Spring Boot 的高性能 API 网关系统。

## 🚀 快速开始

### 1. 启动网关

```bash
cd muxin-gateway
mvn clean package
java -jar gateway/target/gateway-1.0-SNAPSHOT.jar
```

### 2. 访问管理界面

打开浏览器访问: http://localhost:8080/index.html

**默认账号**：
- 用户名: admin  
- 密码: admin123

## ✨ 核心功能

### 管理界面功能
- 📊 **监控大屏** ✅ - 实时展示网关运行状态（WebSocket框架已实现，数据推送未完成）
- 🔀 **路由管理** ✅ - 动态配置路由规则和过滤器
- 👥 **用户管理** ✅ - 完整的用户CRUD操作
- 🎭 **角色管理** ✅ - 基于RBAC的权限控制系统（权限验证暂时禁用）
- 🏢 **部门管理** ❌ - 树形组织架构管理，支持拖拽移动（未实现）
- 🔐 **权限管理** ✅ - 三级权限体系（目录-菜单-按钮）（基础结构已实现）
- ⚙️ **系统设置** ❌ - 系统参数和配置管理（未实现）

### 技术特性
- 基于 Netty 的高性能网关 ✅
- Vue 3 + Element Plus 的现代化管理界面 ✅
- JWT Token 认证机制 ❌（使用Sa-Token代替）
- 支持 WebSocket 实时数据推送 ✅（框架已实现，数据集成未完成）
- 模块化架构设计 ✅

## 📦 项目结构

```
muxin-gateway/
├── gateway/              # 网关主程序 ✅
├── gateway-core/         # 核心功能模块 ✅
│   ├── processor/        # 网关处理器 ✅
│   ├── refactory/        # 重构后的新架构 ❌（已移至gateway-core-plus）
│   │   ├── connect/      # 连接管理（连接工厂、Netty连接实现）❌
│   │   ├── message/      # 协议转换（转换器、管理器）❌
│   │   └── node/         # 节点和地址管理 ❌
│   └── common/           # 公共组件 ✅
├── gateway-core-plus/    # 新架构实现模块 🚧
│   ├── connect/          # 连接管理 ✅（HttpConnectionFactory已实现）
│   ├── message/          # 协议转换 ✅（ProtocolConverter已实现）
│   └── node/             # 节点管理 ❌（未实现）
├── gateway-admin/        # 管理界面模块 ✅
│   └── src/main/resources/static/  # 前端文件 ✅
├── gateway-registry/     # 注册中心模块 ✅
└── doc/                  # 项目文档 ✅
    └── 架构重构总结.md   # 详细的重构文档
```

## 🏗️ 架构设计

### 核心架构组件

#### 1. 协议转换体系
```java
ProtocolConverter          // 协议转换接口 ✅
├── HttpProtocolConverter  // HTTP协议转换器 ✅
└── ...                    // 其他协议转换器 ❌（未实现）

ProtocolConverterManager   // 转换管理器 ✅
└── DefaultProtocolConverterManager  // 支持转换链和统计 ✅
```

#### 2. 连接工厂体系
```java
ConnectionFactory          // 连接工厂接口 ✅
├── HttpConnectionFactory  // HTTP连接工厂 ✅
├── NettyConnectionFactory // Netty连接工厂 ❌（未实现）
└── ...                    // 其他连接工厂 ❌（未实现）

ConnectionFactoryManager   // 工厂管理器 ❌（未实现）
└── DefaultConnectionFactoryManager  // 支持事件监听 ❌（未实现）
```

#### 3. 连接接口层次
```java
BaseConnection             // 基础连接接口 ✅
├── ServerConnection       // 服务器端连接 ❌（未实现）
│   └── NettyServerConnection ❌（未实现）
└── ClientConnection       // 客户端连接 ✅
    ├── HttpClientConnection ✅
    └── NettyClientConnection ❌（未实现）
```

### 架构优势

- **🔄 职责分离**：协议转换、连接管理、业务处理完全分离 ✅
- **🚀 高性能**：基于Netty的异步IO和连接池化 ✅
- **📈 可扩展**：新协议和连接类型的轻松扩展 ✅（架构支持）
- **📊 可监控**：全面的事件监听和统计信息 🚧（部分实现）
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
