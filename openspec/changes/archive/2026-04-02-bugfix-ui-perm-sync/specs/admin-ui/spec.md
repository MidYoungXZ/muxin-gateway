## 修改需求

### 需求：暗黑模式 CSS 变量系统
前端应通过 `.dark` CSS 类切换支持暗黑模式。所有组件应使用自定义 CSS 变量（如 `--bg-primary`、`--card-bg`、`--text-primary`、`--border-primary`、`--text-tertiary`），而非 Element Plus 主题变量（`--el-text-color-primary`、`--el-fill-color-light` 等）。

#### 场景：切换暗黑模式
- **WHEN** 用户切换暗黑模式
- **THEN** 根元素应添加/移除 `.dark` 类，所有 CSS 变量应切换为暗色值

#### 场景：组件使用正确的 CSS 变量
- **WHEN** 组件设置背景色
- **THEN** 应使用 `var(--card-bg)` 而非硬编码的 `#fff` 或 `var(--el-bg-color)`

#### 场景：暗黑模式下章节标题可读
- **WHEN** 路由向导步骤组件渲染章节标题
- **THEN** 标题应使用 `var(--text-primary)`，边框应使用 `var(--border-primary)`，确保亮色和暗黑模式下都可读

#### 场景：暗黑模式下复选框按钮样式
- **WHEN** 暗黑模式激活且路由匹配步骤渲染 `el-checkbox-button` 组件（HTTP 方法）
- **THEN** 按钮应具有 `var(--input-bg)` 背景、`var(--text-primary)` 文字、`var(--border-primary)` 边框，选中状态应使用 `var(--primary-color)` 背景

#### 场景：暗黑模式下预览框背景
- **WHEN** 暗黑模式激活且路由匹配步骤渲染匹配预览区域
- **THEN** 预览框背景应使用 `var(--bg-tertiary)` 而非 `var(--el-fill-color-light)`

### 需求：登录状态持久化
前端应通过 Pinia 用户 store 将认证状态（token、tokenType）持久化到 localStorage。页面刷新时，store 应从 localStorage 恢复状态，并主动向后端验证 token 后才认为用户已认证。

#### 场景：刷新后恢复会话
- **WHEN** 页面刷新且 localStorage 中包含 token
- **THEN** 用户 store 应恢复 token，调用 `GET /api/auth/user-info` 验证 token 仍然有效，仅在后端确认有效时设置 `isLoggedIn: true`

#### 场景：后端拒绝 token 时重定向到登录
- **WHEN** 页面刷新时存在存储的 token，但 `GET /api/auth/user-info` 返回 401 或失败
- **THEN** 用户 store 应清除 localStorage 并重定向到 `/login`

#### 场景：token 过期时重定向到登录
- **WHEN** API 调用返回 401
- **THEN** 请求拦截器应清除用户 store 并重定向到 `/login`

### 需求：路由创建向导
前端应提供 4 步向导对话框用于创建/编辑路由：步骤 1（基本信息）→ 步骤 2（路由匹配）→ 步骤 3（目标服务）→ 步骤 4（插件配置）。编辑时，向导应加载现有路由数据（包括断言）并正确填充所有表单字段。

#### 场景：步骤导航
- **WHEN** 用户点击导航中的步骤 3
- **THEN** 步骤 1 和 2 应显示完成指示器（如果之前已完成），内容应切换到服务选择表单

#### 场景：从向导保存路由
- **WHEN** 用户完成所有 4 步并点击"保存"
- **THEN** 前端应提交包含 `matching`（来自步骤 2）和 `plugins`（来自步骤 4）的 `RouteCreateDTO` 到 `POST /api/routes`

#### 场景：编辑路由时加载断言
- **WHEN** 用户在路由上点击"编辑"且 API 返回断言
- **THEN** 表单应从断言数据中填充路径模式、方法、主机（来自 `config.hosts`）、请求头（来自 `config.headers`）和查询参数（来自 `config.queries`）

## 新增需求

### 需求：响应式路由列表布局
路由列表表格应对信息列（路由 ID、名称、URI、负载均衡、优先级、断言数量、插件数量、状态）使用 `min-width`，使操作列始终完全可见。当视口宽度不足时，列应压缩。

#### 场景：操作按钮完全可见
- **WHEN** 浏览器视口为标准宽度（1280px+）
- **THEN** 所有操作按钮（查看、编辑、删除）应完全可见，无需水平滚动

#### 场景：窄视口下列表压缩
- **WHEN** 浏览器视口窄于总列宽
- **THEN** 具有 `min-width` 的信息列应压缩，操作列保持可访问
