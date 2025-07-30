# Muxin Gateway 管理系统前端

## 项目简介

Muxin Gateway 管理系统前端是一个基于 Vue 3 + TypeScript + Element Plus 开发的现代化网关管理界面，提供了完整的路由管理、监控、鉴权配置等功能。

## 技术栈

- **框架**: Vue 3.4 + TypeScript 5
- **UI组件库**: Element Plus 2.6
- **路由**: Vue Router 4
- **状态管理**: Pinia 2
- **HTTP客户端**: Axios
- **图表**: ECharts 5
- **国际化**: Vue I18n 9
- **构建工具**: Vite 5
- **代码规范**: ESLint + Prettier

## 功能模块

### 📈 监控大屏
- **实时请求统计**: 展示网关的实时请求量、响应时间、错误率、状态码分布等核心指标
- **路由调用监控**: 查看每条路由的调用趋势、异常分布、慢接口排行
- **过滤器命中监控**: 限流、鉴权、熔断等过滤器命中次数与影响范围统计

### 📌 路由管理
- **路由列表**: 管理所有动态路由配置，支持新增/编辑、启用禁用、搜索、跳转配置页
- **路由测试工具**: 提供模拟请求工具，验证路由是否按配置转发
- **节点管理**: 维护注册中心的后端服务节点，包含节点状态监控、健康探测
- **负载均衡配置**: 配置负载均衡策略（轮询、加权轮询、最少连接、IP哈希等）
- **过滤器管理**: 配置请求处理链中的过滤器，支持URL截取、限流、鉴权等

### 🔒 鉴权规则
- **API权限控制**: 配置路由的访问权限规则，支持基于用户、角色、Token、IP等限制
- **白名单配置**: 配置无需鉴权的IP/Token/Header白名单列表

### 🧩 系统管理
- **用户管理**: 管理后台系统用户账号
- **角色管理**: 定义角色并关联权限
- **部门管理**: 构建多级组织结构
- **权限管理**: 管理权限点（菜单权限/接口权限/按钮权限）
- **操作日志**: 查看用户的系统操作日志、登录日志
- **系统配置**: 配置网关系统基础参数

## 快速开始

### 安装依赖
```bash
npm install
```

### 开发环境运行
```bash
npm run dev
```

### 生产环境构建
```bash
npm run build
```

构建产物将输出到 `../gateway-admin/src/main/resources/static/` 目录

### 代码检查
```bash
npm run lint
```

### 代码格式化
```bash
npm run format
```

## 目录结构

```
src/
├── api/              # API接口定义
├── assets/           # 静态资源
├── components/       # 通用组件
├── composables/      # 组合式函数
├── directives/       # 自定义指令
├── i18n/            # 国际化配置
├── layouts/          # 布局组件
├── router/           # 路由配置
├── stores/           # Pinia状态管理
├── styles/           # 全局样式
├── types/            # TypeScript类型定义
├── utils/            # 工具函数
└── views/            # 页面组件
```

## 环境变量

### 开发环境 (.env.development)
```
VITE_API_BASE_URL=/api              # API基础路径
VITE_WS_URL=ws://localhost:8080/ws  # WebSocket地址
VITE_PUBLIC_PATH=/                  # 公共路径
```

### 生产环境 (.env.production)
```
VITE_API_BASE_URL=/api              # API基础路径
VITE_WS_URL=/ws                     # WebSocket地址
VITE_PUBLIC_PATH=/                  # 公共路径
```

## 开发规范

1. **组件命名**: 使用 PascalCase，如 `RouteList.vue`
2. **组合函数**: 使用 `use` 前缀，如 `useWebSocket.ts`
3. **类型定义**: 集中在 `types` 目录管理
4. **API接口**: 按模块组织在 `api` 目录
5. **状态管理**: 使用 Pinia 的组合式 API

## 部署说明

前端采用前后端一体化部署方式：

1. 执行 `npm run build` 构建前端资源
2. 构建产物自动输出到 Spring Boot 的静态资源目录
3. 启动 Spring Boot 应用即可访问

## 浏览器兼容性

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## 更新日志

### 2024-12-19 请求工具统一
- **统一前端HTTP请求工具**: 所有API文件统一使用传统的axios + 拦截器方案
- **删除冗余工具**: 移除 `utils/api.ts`，避免代码混乱
- **优化项目结构**: 统一了10个API文件的请求方式，提升代码一致性
- **文档完善**: 新增《前端请求工具统一说明》文档

### 2024-12-18 操作日志功能
- **后端功能**: 完整的操作日志记录、查询、管理功能
- **前端界面**: 现代化的操作日志管理页面
- **权限控制**: 基于角色的操作权限管理
- **数据导出**: 支持日志数据批量导出

## 许可证

[MIT License](LICENSE) 