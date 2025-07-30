# Muxin Gateway 管理系统开发指南

## 项目概述
你正在开发一个基于Spring Boot的API网关管理系统。前端使用Vue3 + Element Plus，采用前后端一体化部署方式（前端打包为静态资源嵌入Spring Boot）。

## 技术架构

### 前端技术栈
- **框架**: Vue 3.4+ (使用Composition API)
- **UI组件库**: Element Plus 2.4+
- **路由**: Vue Router 4
- **状态管理**: Pinia 2
- **HTTP客户端**: Axios
- **图表库**: ECharts 5.4+
- **国际化**: Vue I18n 9
- **构建工具**: Vite 5
- **开发语言**: TypeScript 5
- **代码规范**: ESLint + Prettier

### 后端技术栈
- **框架**: Spring Boot 3.3.5
- **网关核心**: Netty 4.1+
- **数据库**: MySQL 8.0 + Redis
- **ORM**: MyBatis-Plus 3.5.7
- **认证**: JWT (Spring Security)
- **注册中心**: Nacos 2.0+
- **实时通信**: Spring WebSocket

## 项目结构

### 前端目录结构
```
gateway-admin-ui/
├── src/
│   ├── api/              # API接口定义
│   ├── assets/           # 静态资源
│   ├── components/       # 通用组件
│   ├── composables/      # 组合式函数
│   ├── directives/       # 自定义指令
│   ├── i18n/            # 国际化配置
│   ├── layouts/          # 布局组件
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia状态管理
│   ├── styles/           # 全局样式
│   ├── types/            # TypeScript类型定义
│   ├── utils/            # 工具函数
│   └── views/            # 页面组件
│       ├── monitor/      # 监控大屏
│       ├── routes/       # 路由管理
│       ├── auth/         # 鉴权规则
│       └── system/       # 系统管理
├── public/               # 公共资源
├── .env.development      # 开发环境配置
├── .env.production       # 生产环境配置
├── vite.config.ts        # Vite配置
└── package.json          # 项目配置
```

### 后端集成路径
```
gateway-admin/src/main/resources/
├── static/               # 前端打包输出目录
└── templates/            # 模板文件（如需要）
```

## 核心功能实现指南

### 1. 监控大屏模块
```typescript
// 实时数据更新使用WebSocket
interface MonitorData {
  requestCount: number;      // 实时请求数
  avgResponseTime: number;   // 平均响应时间
  errorRate: number;         // 错误率
  statusDistribution: {      // 状态码分布
    '2xx': number;
    '4xx': number;
    '5xx': number;
  };
}

// ECharts配置示例
const requestTrendOption = {
  title: { text: '请求趋势' },
  xAxis: { type: 'time' },
  yAxis: { type: 'value' },
  series: [{
    type: 'line',
    smooth: true,
    data: [] // 动态更新
  }]
};
```

### 2. 路由管理模块
```typescript
// 路由配置接口
interface RouteConfig {
  id: number;
  routeId: string;
  name: string;
  uri: string;
  predicates: RoutePredicate[];
  filters: RouteFilter[];
  order: number;
  enabled: boolean;
  grayscaleConfig?: GrayscaleConfig; // 灰度配置
  templateId?: number;               // 配置模板ID
}

// 配置模板
interface RouteTemplate {
  id: number;
  name: string;
  description: string;
  config: Partial<RouteConfig>;
  variables: TemplateVariable[];     // 模板变量
}
```

### 3. WebSocket实时通信
```typescript
// WebSocket连接管理
class WebSocketManager {
  private ws: WebSocket | null = null;
  
  connect() {
    this.ws = new WebSocket('ws://localhost:8080/ws/monitor');
    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      // 更新监控数据
    };
  }
  
  disconnect() {
    this.ws?.close();
  }
}
```

### 4. 登录注册功能实现
```typescript
// 登录页面数据结构
interface LoginForm {
  username: string;
  password: string;
  rememberMe: boolean;
  captcha?: string;        // 验证码
}

// 注册页面数据结构
interface RegisterForm {
  username: string;
  password: string;
  confirmPassword: string;
  email: string;
  phone?: string;
  captcha: string;         // 验证码
  agreement: boolean;      // 同意用户协议
}

// 登录响应
interface LoginResponse {
  token: string;
  refreshToken: string;
  user: {
    id: number;
    username: string;
    nickname: string;
    avatar: string;
    roles: string[];
    permissions: string[];
  };
  expiresIn: number;
}

// 登录功能实现
const useAuth = () => {
  const login = async (form: LoginForm) => {
    const { data } = await axios.post('/api/auth/login', form);
    // 存储Token
    localStorage.setItem('token', data.token);
    localStorage.setItem('refreshToken', data.refreshToken);
    // 存储用户信息
    userStore.setUser(data.user);
    // 记住用户名
    if (form.rememberMe) {
      localStorage.setItem('rememberedUsername', form.username);
    }
  };
  
  const register = async (form: RegisterForm) => {
    await axios.post('/api/auth/register', form);
    // 注册成功后跳转登录页
    ElMessage.success('注册成功，请登录');
    router.push('/login');
  };
  
  const logout = async () => {
    try {
      await axios.post('/api/auth/logout');
    } finally {
      // 清除本地存储
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      userStore.clearUser();
      router.push('/login');
    }
  };
  
  return { login, register, logout };
};
```

### 5. JWT认证集成
```typescript
// Token管理
class TokenManager {
  private refreshing = false;
  
  // 获取Token
  getToken(): string | null {
    return localStorage.getItem('token');
  }
  
  // 刷新Token
  async refreshToken(): Promise<void> {
    if (this.refreshing) return;
    
    this.refreshing = true;
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      const { data } = await axios.post('/api/auth/refresh', { refreshToken });
      localStorage.setItem('token', data.token);
      localStorage.setItem('refreshToken', data.refreshToken);
    } finally {
      this.refreshing = false;
    }
  }
  
  // 检查Token是否过期
  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}

const tokenManager = new TokenManager();

// 请求拦截器
axios.interceptors.request.use(async config => {
  // 登录和注册接口不需要Token
  if (config.url?.includes('/auth/')) {
    return config;
  }
  
  // 检查Token是否过期
  if (tokenManager.isTokenExpired()) {
    await tokenManager.refreshToken();
  }
  
  const token = tokenManager.getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器处理401
axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        await tokenManager.refreshToken();
        return axios(originalRequest);
      } catch {
        // 刷新失败，跳转登录
        router.push('/login');
      }
    }
    
    return Promise.reject(error);
  }
);

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = tokenManager.getToken();
  const whiteList = ['/login', '/register', '/forgot-password'];
  
  if (whiteList.includes(to.path)) {
    // 已登录用户访问登录页，重定向到首页
    if (token && !tokenManager.isTokenExpired()) {
      next('/');
    } else {
      next();
    }
  } else {
    // 需要登录的页面
    if (!token || tokenManager.isTokenExpired()) {
      next(`/login?redirect=${to.fullPath}`);
    } else {
      next();
    }
  }
});
```

### 6. 国际化配置
```typescript
// i18n配置
const messages = {
  'zh-CN': {
    menu: {
      monitor: '监控大屏',
      routes: '路由管理',
      auth: '鉴权规则',
      system: '系统管理'
    }
  },
  'en-US': {
    menu: {
      monitor: 'Monitor Dashboard',
      routes: 'Route Management',
      auth: 'Auth Rules',
      system: 'System Management'
    }
  }
};
```

### 7. 灰度发布配置
```typescript
interface GrayscaleConfig {
  enabled: boolean;
  type: 'percentage' | 'whitelist' | 'header';
  percentage?: number;         // 百分比灰度
  whitelist?: string[];       // 白名单
  headerRules?: HeaderRule[]; // Header规则
}
```

## API接口规范

### 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1704985200000
}
```

### 核心API端点
```
# 认证相关
POST   /api/auth/login          # 用户登录
POST   /api/auth/register       # 用户注册
POST   /api/auth/logout         # 退出登录
POST   /api/auth/refresh        # 刷新Token
GET    /api/auth/captcha        # 获取验证码
POST   /api/auth/check-username # 检查用户名是否可用
POST   /api/auth/send-code      # 发送邮箱/手机验证码
POST   /api/auth/reset-password # 重置密码

# 监控数据 (WebSocket)
WS     /ws/monitor

# 路由管理
GET    /api/routes
POST   /api/routes
PUT    /api/routes/{id}
DELETE /api/routes/{id}
POST   /api/routes/{id}/enable
POST   /api/routes/{id}/disable
POST   /api/routes/test

# 配置模板
GET    /api/templates
POST   /api/templates
PUT    /api/templates/{id}
DELETE /api/templates/{id}
POST   /api/routes/apply-template

# 节点管理
GET    /api/nodes
PUT    /api/nodes/{id}
POST   /api/nodes/{id}/health-check

# 过滤器管理
GET    /api/filters
POST   /api/filters
PUT    /api/filters/{id}
DELETE /api/filters/{id}
PUT    /api/filters/order

# 鉴权规则
GET    /api/auth-rules
POST   /api/auth-rules
PUT    /api/auth-rules/{id}
DELETE /api/auth-rules/{id}

# 系统管理
GET    /api/system/users
GET    /api/system/roles
GET    /api/system/departments
GET    /api/system/permissions
GET    /api/system/logs
GET    /api/system/config
PUT    /api/system/config
```

## 开发规范

### 1. 组件命名规范
- 页面组件: PascalCase (如 RouteList.vue)
- 通用组件: 带前缀 (如 GwTable.vue, GwDialog.vue)
- 组合函数: use开头 (如 useWebSocket.ts)

### 2. 状态管理规范
```typescript
// stores/monitor.ts
export const useMonitorStore = defineStore('monitor', () => {
  const stats = ref<MonitorStats>({});
  const wsManager = new WebSocketManager();
  
  const startMonitoring = () => {
    wsManager.connect();
  };
  
  return { stats, startMonitoring };
});
```

### 3. 错误处理规范
```typescript
// 统一错误处理
const handleApiError = (error: any) => {
  const message = error.response?.data?.message || '操作失败';
  ElMessage.error(message);
  
  // 记录错误日志
  console.error('[API Error]', error);
};
```

### 4. 性能优化要点
- 路由懒加载
- 组件按需导入
- 图表数据采样（大数据量时）
- 虚拟滚动（长列表）
- WebSocket重连机制
- 请求防抖/节流

## 部署配置

### 1. 环境变量配置
```env
# .env.production
VITE_API_BASE_URL=/api
VITE_WS_URL=/ws
VITE_PUBLIC_PATH=/
```

### 2. 打包配置
```typescript
// vite.config.ts
export default defineConfig({
  base: '/',
  build: {
    outDir: '../gateway-admin/src/main/resources/static',
    emptyOutDir: true,
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'echarts': ['echarts'],
          'vendor': ['vue', 'vue-router', 'pinia', 'axios']
        }
      }
    }
  }
});
```

### 3. Spring Boot配置
```yaml
spring:
  web:
    resources:
      static-locations: classpath:/static/
  mvc:
    static-path-pattern: /**
```

## 测试要求

### 1. 单元测试
- 组件测试使用 Vitest + Vue Test Utils
- 覆盖率要求 > 80%

### 2. E2E测试
- 使用 Cypress 或 Playwright
- 覆盖核心业务流程

## 注意事项

1. **安全性**
   - 所有API调用需要JWT认证
   - 敏感操作需要二次确认
   - 防止XSS和CSRF攻击

2. **兼容性**
   - 支持Chrome、Firefox、Safari最新版本
   - 响应式设计，支持1366x768以上分辨率

3. **性能要求**
   - 首屏加载时间 < 3秒
   - 页面切换响应 < 500ms
   - WebSocket断线自动重连

4. **扩展性**
   - 预留插件接口
   - 支持自定义主题
   - 模块化设计，便于功能扩展

## 开发流程

1. 需求分析 → 2. API设计 → 3. 前端开发 → 4. 集成测试 → 5. 部署上线

记住：代码质量优先，用户体验至上，保持简洁优雅。 