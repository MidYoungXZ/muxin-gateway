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
- 📊 **监控大屏** - 实时展示网关运行状态
- 🔀 **路由管理** - 动态配置路由规则
- 👥 **用户管理** - 管理系统用户
- 🔐 **鉴权配置** - 配置访问权限规则
- ⚙️ **系统设置** - 系统参数配置

### 技术特性
- 基于 Netty 的高性能网关
- Vue 3 + Element Plus 的现代化管理界面
- JWT Token 认证机制
- 支持 WebSocket 实时数据推送
- 模块化架构设计

## 📦 项目结构

```
muxin-gateway/
├── gateway/              # 网关主程序
├── gateway-core/         # 核心功能模块
├── gateway-admin/        # 管理界面模块
│   └── src/main/resources/static/  # 前端文件
├── gateway-registry/     # 注册中心模块
└── doc/                  # 项目文档
```

## 🛠️ 技术栈

### 后端
- Spring Boot 3.3.5
- Netty 4.1+
- MyBatis-Plus 3.5.7
- MySQL 8.0
- Nacos 2.0+

### 前端
- Vue 3.4.15
- Element Plus 2.4.2
- ECharts 5.4.3
- Axios 1.6.5

## 📝 版本更新

### v2.1.0 (2025-01-16) - 现代化界面升级
- 🎨 **全面现代化设计升级**
  - 新的色彩系统和设计规范
  - 现代化仪表板重新设计
  - 优化导航系统和用户体验
  - 完善响应式适配支持
- ⚡ **交互体验提升**
  - 数值动画和微交互效果
  - 全局搜索功能 (Ctrl+K)
  - 通知中心和消息系统
  - 多标签页面管理
- 📱 **移动端优化**
  - 完整的响应式断点系统
  - 触摸友好的交互设计
  - 移动端导航适配

### v2.0.0 (2025-01-16)
- ✅ 全新的 Vue 3 管理界面
- ✅ 实现登录注册功能
- ✅ 监控大屏数据可视化
- ✅ 路由管理增删改查
- ✅ JWT 认证和自动续期

## 📄 开源协议

本项目采用 [Apache License 2.0](LICENSE) 开源协议。
