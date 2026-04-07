## 目的
管理后台前端（Vue 3 SPA），提供动态路由、暗黑模式、schema 驱动表单、路由创建向导和 Element Plus 自动导入。

## 需求

### 需求：带动态路由的 SPA
前端应为使用 Composition API 的 Vue 3 SPA。路由应根据已认证用户的权限从后端菜单 API 动态加载。

#### 场景：未认证访问的静态路由
- **WHEN** 应用在未认证状态下加载
- **THEN** 应仅注册静态路由 `/login`、`/403`、`/404`

#### 场景：登录后加载动态路由
- **WHEN** 用户登录成功
- **THEN** 菜单 store 应获取 `GET /api/menus/user-tree`，从 C 类型菜单注册 Vue Router 路由，并渲染侧边导航

### 需求：带 CSS 变量系统的暗黑模式
前端应通过 `.dark` CSS 类切换支持暗黑模式。所有组件应使用自定义 CSS 变量（如 `--bg-primary`、`--card-bg`、`--text-primary`、`--border-primary`），而非 Element Plus 主题变量。

#### 场景：切换暗黑模式
- **WHEN** 用户切换暗黑模式
- **THEN** 应从根元素添加/移除 `.dark` 类，所有 CSS 变量应切换为暗黑值

#### 场景：组件使用正确的 CSS 变量
- **WHEN** 组件设置背景色
- **THEN** 应使用 `var(--card-bg)` 而非硬编码的 `#fff` 或 `var(--el-bg-color)`

### 需求：Schema 驱动的表单渲染
前端应使用递归的 `SchemaField.vue` 组件，根据 JSON Schema 定义动态渲染插件配置表单。

#### 场景：渲染字符串字段
- **WHEN** schema 属性的 `type: "string"` 没有 `enum`
- **THEN** `SchemaField` 应渲染 `el-input` 组件

#### 场景：渲染选择字段
- **WHEN** schema 属性的 `type: "string"` 有 `enum: ["ANT", "REGEX", "EXACT"]`
- **THEN** `SchemaField` 应渲染 `el-select`，选项为 enum 值

#### 场景：递归渲染嵌套对象
- **WHEN** schema 属性的 `type: "object"` 有 `properties`
- **THEN** `SchemaField` 应为每个属性递归渲染嵌套的 `SchemaField` 组件

#### 场景：渲染对象数组
- **WHEN** schema 属性的 `type: "array"` 有 `items.type: "object"`
- **THEN** `SchemaField` 应渲染可添加/删除的对象表单列表

### 需求：路由创建向导
前端应提供 4 步向导对话框用于创建/编辑路由：步骤 1（基本信息）→ 步骤 2（路由匹配）→ 步骤 3（目标服务）→ 步骤 4（插件配置）。

#### 场景：步骤导航
- **WHEN** 用户点击导航中的步骤 3
- **THEN** 步骤 1 和 2 应显示完成指示器（如果之前已完成），内容应切换到服务选择表单

#### 场景：从向导保存路由
- **WHEN** 用户完成所有 4 步并点击"保存"
- **THEN** 前端应提交包含 `matching`（来自步骤 2）和 `plugins`（来自步骤 4）的 `RouteCreateDTO` 到 `POST /api/routes`

### 需求：登录状态持久化
前端应通过 Pinia user store 将认证状态（token、tokenType）持久化到 localStorage。页面刷新时，store 应从 localStorage 恢复状态。

#### 场景：刷新后恢复会话
- **WHEN** 页面刷新且 localStorage 包含有效 token
- **THEN** user store 应恢复 token 并设置 `isLoggedIn: true`

#### 场景：Token 过期重定向到登录
- **WHEN** API 调用返回 401
- **THEN** 请求拦截器应清除 user store 并重定向到 `/login`

### 需求：Element Plus 自动导入
前端应使用 `unplugin-auto-import` 和 `unplugin-vue-components` 自动导入 Element Plus 组件和 API，避免手动 import 语句。

#### 场景：使用 el-button 无需导入
- **WHEN** 组件模板包含 `<el-button>`
- **THEN** 组件应由构建插件自动解析，无需手动导入