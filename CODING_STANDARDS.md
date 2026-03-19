# Muxin Gateway 编程规范

本文档为 Muxin Gateway 项目提供 AI 辅助编程的标准规范，涵盖 Java 后端和 Node.js (Vue/TypeScript) 前端开发。

---

## 一、Java 后端规范

### 1.1 项目结构

```
gateway-core/src/main/java/com/muxin/gateway/core/plus/
├── config/          # 配置类（GatewayConfig, ServerConfig, NettyPoolConfig 等）
├── connect/         # 连接管理（ClientConnection, ServerConnection, ConnectionPool）
│   └── netty/      # Netty 实现
├── route/          # 路由系统
│   ├── filter/    # 过滤器
│   ├── predicate/ # 断言
│   ├── loadbalance/# 负载均衡策略
│   └── service/   # 服务相关
├── message/        # 消息抽象
│   └── http/      # HTTP 消息实现
├── server/         # 服务器实现
│   └── http/      # HTTP 服务器
├── registry/       # 服务注册中心
├── common/        # 公共组件（LifeCycle, Repository, ServiceRegistry）
└── utils/         # 工具类
```

**规范**：
- 核心模块 `gateway-core` 保持无 Spring 依赖
- 按功能模块划分包，不按层划分（避免贫血结构）
- 配置类统一放在 `config/` 包

### 1.2 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | UpperCamelCase | `DefaultRouteManager`, `NettyHttpServer` |
| 接口名 | UpperCamelCase，可选加 I 前缀 | `RouteManager`, `ConnectionPool` |
| 方法名 | lowerCamelCase | `matchRoute()`, `getClientConnection()` |
| 常量 | UPPER_SNAKE_CASE | `DEFAULT_CONNECTION`, `MAX_POOL_SIZE` |
| 成员变量 | lowerCamelCase | `connectionPoolManager`, `routeManager` |
| 包名 | 全小写 | `com.muxin.gateway.core.plus.route` |
| 枚举值 | UPPER_SNAKE_CASE | `CONNECTION`, `REQUEST`, `ROUND_ROBIN` |

**特殊规范**：
- 抽象类可加 `Abstract` 前缀：`AbstractLoadBalanceStrategy`
- 实现类加 `Default` 前缀：`DefaultRouteManager`
- Netty 实现类加 `Netty` 前缀：`NettyConnectionPool`

### 1.3 类设计规范

#### 1.3.1 接口与抽象类

```java
// ✅ 正确：接口定义核心抽象
public interface RouteManager extends Repository<String, Route>, LifeCycle {
    Route matchRoute(RequestContext context);
}

// ✅ 正确：抽象类提供基础实现
public abstract class LoadBalanceStrategy {
    protected final LoadBalanceDefinition definition;
    
    protected LoadBalanceStrategy(LoadBalanceDefinition definition) {
        this.definition = Objects.requireNonNull(definition, "definition不能为空");
    }
    
    public abstract EndpointAddress select(List<EndpointAddress> addresses, RequestContext context);
}

// ✅ 正确：实现类使用 @Slf4j + @Data/@Builder
@Slf4j
@Data
@Builder
public class DefaultRouteManager implements RouteManager {
    private final AtomicReference<Route> defaultRoute = new AtomicReference<>();
    private final Map<String, Route> routeStorage = new ConcurrentHashMap<>();
}
```

#### 1.3.2 构造函数与依赖注入

```java
// ✅ 正确：使用 Builder 模式创建复杂对象
@Service
public class RouteConfigConverter {
    public List<Route> convertToRoutes(List<RouteDefinition> definitions, 
                                       Map<String, ServiceDefinition> serviceMap) {
        return definitions.stream()
            .map(def -> convertSingleRoute(def, serviceMap))
            .toList();
    }
}

// ✅ 正确：简单对象直接构造函数
public class PathPredicate implements Predicate {
    private final String pattern;
    
    public PathPredicate(String pattern) {
        this.pattern = Objects.requireNonNull(pattern, "pattern不能为空");
    }
}
```

#### 1.3.3 枚举使用

```java
// ✅ 正确：枚举定义相关常量
public enum TimeoutType {
    CONNECTION("连接超时"),
    REQUEST("请求超时"),
    TOTAL("总超时"),
    READ("读取超时"),
    WRITE("写入超时"),
    CIRCUIT_BREAKER("熔断超时");
    
    private final String description;
    
    TimeoutType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

// ✅ 正确：枚举作为 switch 参数
public Long getTimeout(TimeoutType type) {
    switch (type) {
        case CONNECTION: return connection;
        case REQUEST: return request;
        default: return null;
    }
}
```

### 1.4 方法设计规范

#### 1.4.1 方法签名

```java
// ✅ 正确：清晰的方法签名，参数非空校验
public Route matchRoute(RequestContext context) {
    if (context == null || context.exchange() == null) {
        log.warn("[RouteManager] 请求上下文为空，返回默认路由");
        return defaultRoute.get();
    }
    // ...
}

// ✅ 正确：返回空集合而非 null
public List<Route> listRoutes() {
    return new ArrayList<>(routeStorage.values());  // 永远不返回 null
}

// ✅ 正确：返回不可变集合
public Map<String, RouteService> listRouteServices() {
    return Collections.unmodifiableMap(routeServiceStorage);
}

// ✅ 正确：Optional 用于可能不存在的返回值
public Optional<Route> findRoute(String routeId) {
    return Optional.ofNullable(routeStorage.get(routeId));
}
```

#### 1.4.2 方法长度

- 单个方法不超过 **50 行**
- 超过 20 行考虑拆分成私有方法
- 避免深层嵌套（不超过 3 层）

```java
// ✅ 正确：方法职责单一，按步骤划分
public void processRequest(RequestContext context) {
    validateContext(context);
    try {
        prepareRequest(context);      // Step 1
        invokeBackendService(context) // Step 2
            .whenComplete((r, e) -> handleCompletion(context, r, e));
    } catch (Exception e) {
        handleError(context, e);
    }
}

private void prepareRequest(RequestContext context) {
    Route route = routeManager.matchRoute(context);
    context.setMatchedRoute(route);
    executeFilters(context, FilterType.PRE);
    selectTargetAndAcquireConnection(context);
}
```

### 1.5 异常处理规范

#### 1.5.1 异常分类

| 类型 | 使用场景 | 示例 |
|------|----------|------|
| `IllegalArgumentException` | 参数校验失败 | `Objects.requireNonNull(param)` |
| `IllegalStateException` | 状态异常 | `if (!initialized) throw new IllegalStateException()` |
| `UnsupportedOperationException` | 不支持的操作 | `throw new UnsupportedOperationException("不支持的方法")` |
| `GatewayException` | 业务异常 | 自定义网关异常 |

#### 1.5.2 异常处理模式

```java
// ✅ 正确：使用 try-finally 确保资源释放
public void addRoute(Route route) {
    if (route == null) {
        throw new IllegalArgumentException("路由不能为空");
    }
    
    try {
        cacheLock.writeLock().lock();
        route.validate();
        routeStorage.put(route.getId(), route);
        refreshCache();
        log.info("[RouteManager] 路由添加成功: {}", route.getId());
    } catch (Exception e) {
        log.error("[RouteManager] 路由添加失败: {}", route.getId(), e);
        throw e;
    } finally {
        cacheLock.writeLock().unlock();
    }
}

// ✅ 正确：业务异常使用具体类型
public void validate() {
    if (sortedRoutes == null) {
        throw new IllegalStateException("sortedRoutes未初始化");
    }
}
```

### 1.6 日志规范

#### 1.6.1 日志级别使用

| 级别 | 使用场景 |
|------|----------|
| `ERROR` | 异常或错误情况，影响功能 |
| `WARN` | 潜在问题但不影响功能 |
| `INFO` | 重要业务操作，如启动、关闭、配置加载 |
| `DEBUG` | 调试信息，方法入参、出参 |

```java
// ✅ 正确：日志格式
log.info("[DefaultRouteManager] 路由添加成功: {} -> {}", route.getId(), route.getName());
log.warn("[DefaultRouteManager] 请求上下文为空，返回默认路由");
log.error("[DefaultRouteManager] 路由添加失败: {}", route.getId(), e);
log.debug("匹配路由: {} -> {}", context.requestId(), route.getId());

// ❌ 错误：字符串拼接
log.info("路由添加成功: " + route.getId());  // 错误

// ❌ 错误：敏感信息
log.info("用户密码: {}", password);  // 错误
```

### 1.7 并发与线程安全

#### 1.7.1 线程安全集合

```java
// ✅ 正确：使用线程安全集合
private final Map<String, Route> routeStorage = new ConcurrentHashMap<>();
private volatile List<Route> sortedRoutes = Collections.emptyList();
private final AtomicReference<Route> defaultRoute = new AtomicReference<>();

// ✅ 正确：读写锁用于读多写少场景
private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();

// ✅ 正确：volatile 用于状态标志
private volatile boolean initialized = false;
private volatile boolean running = false;
```

#### 1.7.2 锁的使用

```java
// ✅ 正确：读锁用于并发读取
public Route matchRoute(RequestContext context) {
    List<Route> routes = sortedRoutes;  // volatile 读取
    for (Route route : routes) {
        if (route.matches(context)) {
            return route;
        }
    }
    return defaultRoute.get();
}

// ✅ 正确：写锁用于修改
public void addRoute(Route route) {
    try {
        cacheLock.writeLock().lock();
        routeStorage.put(route.getId(), route);
        refreshCache();
    } finally {
        cacheLock.writeLock().unlock();
    }
}
```

### 1.8 配置与构建模式

#### 1.8.1 配置类模式

```java
// ✅ 正确：使用 @Data @Builder @Builder.Default
@Data
@Builder
public class NettyPoolConfig {
    @Builder.Default
    private int maxConnections = 10;
    
    @Builder.Default
    private long acquireTimeoutMs = 5000;
    
    public static NettyPoolConfig defaultConfig() {
        return NettyPoolConfig.builder().build();
    }
}

// ✅ 正确：配置验证
public void validate() {
    if (gatewayName == null || gatewayName.trim().isEmpty()) {
        throw new IllegalArgumentException("gatewayName 不能为空");
    }
    if (workerThreads <= 0) {
        throw new IllegalArgumentException("workerThreads 必须大于 0");
    }
}
```

### 1.9 生命周期管理

实现 `LifeCycle` 接口：

```java
public interface LifeCycle {
    void init();      // 初始化
    void start();     // 启动
    void shutdown();  // 关闭
}

// ✅ 正确：实现 LifeCycle
@Slf4j
public class DefaultRouteManager implements RouteManager {
    @Override
    public void init() {
        refreshCache();
        log.info("[DefaultRouteManager] 路由管理器初始化完成");
    }
    
    @Override
    public void start() {
        refreshCache();
    }
    
    @Override
    public void shutdown() {
        routeStorage.clear();
        sortedRoutes = Collections.emptyList();
        log.info("[DefaultRouteManager] 路由管理器已关闭");
    }
}
```

---

## 二、TypeScript/Vue 前端规范

### 2.1 项目结构

```
gateway-admin-ui/src/
├── api/            # API 接口（按业务模块划分）
├── components/     # 公共组件
├── composables/    # 组合式函数
├── directives/     # 自定义指令
├── i18n/          # 国际化
├── layouts/       # 布局组件
├── router/        # 路由配置
├── stores/        # Pinia 状态管理
├── types/         # TypeScript 类型定义
├── utils/         # 工具函数
└── views/         # 页面组件（按业务模块划分）
    ├── auth/
    ├── dashboard/
    ├── monitor/
    ├── routes/
    └── system/
```

### 2.2 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `UserFormDialog.vue`, `SidebarItem.vue` |
| 组合式函数 | use 前缀 | `useTheme.ts`, `usePermission.ts` |
| API 模块 | 小写或 kebab-case | `routes.ts`, `user-api.ts` |
| 类型/接口 | PascalCase | `RouteConfig`, `ApiResponse` |
| 枚举 | PascalCase | `RouteStatus`, `FilterType` |
| 常量 | UPPER_SNAKE_CASE | `API_BASE_URL`, `MAX_PAGE_SIZE` |
| 变量/函数 | camelCase | `routeList`, `getRouteDetail()` |

### 2.3 TypeScript 类型规范

#### 2.3.1 接口与类型定义

```typescript
// ✅ 正确：使用 interface 定义数据结构
export interface RouteConfig {
  id: string
  name: string
  order: number
  enabled: boolean
  predicates: PredicateDefinition[]
  filters: FilterDefinition[]
  serviceRef: string
}

// ✅ 正确：使用 type 定义联合类型或别名
export type ApiResponse<T = any> = {
  code: number
  message: string
  data: T
}

export type PageResult<T> = {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// ✅ 正确：可选属性用 ?
export interface UserInfo {
  id?: number
  username?: string
  nickname?: string
  email?: string
}

// ✅ 正确：只读属性
export interface RouteQuery {
  readonly page: number
  readonly pageSize: number
}
```

#### 2.3.2 API 函数定义

```typescript
// ✅ 正确：统一的 API 模块结构
import request from '@/utils/request'
import type { RouteConfig, RouteCreateRequest, RouteQueryParams } from '@/types/route'

export const routeApi = {
  // 分页查询
  getRoutes(params: RouteQueryParams) {
    return request({
      url: '/api/routes',
      method: 'get',
      params
    })
  },

  // 获取详情
  getRouteDetail(id: string) {
    return request({
      url: `/api/routes/${id}`,
      method: 'get'
    })
  },

  // 创建
  createRoute(data: RouteCreateRequest) {
    return request({
      url: '/api/routes',
      method: 'post',
      data
    })
  },

  // 更新
  updateRoute(id: string, data: RouteCreateRequest) {
    return request({
      url: `/api/routes/${id}`,
      method: 'put',
      data
    })
  },

  // 删除
  deleteRoute(id: string) {
    return request({
      url: `/api/routes/${id}`,
      method: 'delete'
    })
  }
}
```

### 2.4 Vue 组件规范

#### 2.4.1 组件结构

```vue
<!-- ✅ 正确：组件结构顺序 -->
<template>
  <div class="component-name">
    <!-- 模板内容 -->
  </div>
</template>

<script setup lang="ts">
// 1. 导入
import { ref, computed, onMounted } from 'vue'
import type { PropType } from 'vue'
import { useUserStore } from '@/stores/user'

// 2. Props 定义
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  routeData: {
    type: Object as PropType<RouteConfig>,
    default: null
  }
})

// 3. Emits
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

// 4. 组合式函数
const { hasPermission } = usePermission()

// 5. 响应式状态
const loading = ref(false)
const formData = ref<RouteConfig>({})

// 6. 计算属性
const isVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 7. 方法
const handleSubmit = async () => {
  loading.value = true
  try {
    // ...
  } finally {
    loading.value = false
  }
}

// 8. 生命周期
onMounted(() => {
  // ...
})
</script>

<style lang="scss" scoped>
.component-name {
  // 样式
}
</style>
```

#### 2.4.2 组合式函数 (Composables)

```typescript
// ✅ 正确：use 前缀，返回有意义的命名
export const usePermission = () => {
  const userStore = useUserStore()
  
  const hasPermission = (permission: string): boolean => {
    return userStore.permissions.includes(permission) || userStore.permissions.includes('*:*:*')
  }
  
  const hasRole = (role: string): boolean => {
    return userStore.roles.includes(role)
  }
  
  const hasAnyPermission = (permissions: string[]): boolean => {
    return permissions.some(p => hasPermission(p))
  }
  
  return {
    hasPermission,
    hasRole,
    hasAnyPermission
  }
}
```

### 2.5 Pinia 状态管理

```typescript
// ✅ 正确：使用 defineStore + composition-api 风格
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const userInfo = ref<Partial<UserInfo>>({})
  const permissions = ref<string[]>([])
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value.username || '')
  
  // 方法
  const loginAction = async (loginForm: LoginForm) => {
    const response = await loginApi(loginForm)
    token.value = response.data.token
    userInfo.value = response.data.userInfo
  }
  
  const logout = async () => {
    token.value = ''
    userInfo.value = {}
    permissions.value = []
    localStorage.clear()
  }
  
  return {
    token,
    userInfo,
    permissions,
    isLoggedIn,
    username,
    loginAction,
    logout
  }
})
```

### 2.6 HTTP 请求规范

```typescript
// ✅ 正确：统一封装 axios
import axios, { AxiosInstance, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  (error: AxiosError) => {
    // 统一错误处理
    if (error.response?.status === 401) {
      // 处理未授权
    }
    return Promise.reject(error)
  }
)

export default request

// ✅ 正确：封装常用方法
export const get = <T = any>(url: string, params?: any) => {
  return request.get<T, T>(url, { params })
}

export const post = <T = any>(url: string, data?: any) => {
  return request.post<T, T>(url, data)
}

export const put = <T = any>(url: string, data?: any) => {
  return request.put<T, T>(url, data)
}

export const del = <T = any>(url: string) => {
  return request.delete<T, T>(url)
}
```

### 2.7 样式规范

```vue
<!-- ✅ 正确：使用 scoped + lang="scss" -->
<style lang="scss" scoped>
.component-name {
  // 使用 BEM 命名
  &__header {
    display: flex;
    align-items: center;
  }
  
  &__title {
    font-size: 16px;
    font-weight: 500;
  }
  
  &--active {
    color: var(--el-color-primary);
  }
}

// ✅ 正确：使用 CSS 变量
.button {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}
</style>
```

---

## 三、API 设计规范

### 3.1 RESTful API

| 操作 | 方法 | URL | 说明 |
|------|------|-----|------|
| 查询列表 | GET | `/api/routes` | 分页查询 |
| 查询详情 | GET | `/api/routes/{id}` | 获取单个 |
| 创建 | POST | `/api/routes` | 创建资源 |
| 更新 | PUT | `/api/routes/{id}` | 完整更新 |
| 删除 | DELETE | `/api/routes/{id}` | 删除资源 |
| 批量操作 | POST | `/api/routes/batch` | 批量操作 |

### 3.2 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 业务数据
  }
}
```

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 四、Git 提交规范

### 4.1 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 4.2 Type 类型

| type | 说明 |
|------|------|
| feat | 新功能 |
| fix | Bug 修复 |
| docs | 文档更新 |
| style | 代码格式（不影响功能） |
| refactor | 重构 |
| perf | 性能优化 |
| test | 测试相关 |
| chore | 构建/工具相关 |

### 4.3 示例

```
feat(route): 添加路由匹配优先级支持

实现基于 order 字段的路由优先级排序，支持数字越小优先级越高

- 按 order 字段升序排列路由列表
- 匹配时按优先级顺序遍历
- 默认兜底路由使用 Integer.MAX_VALUE

Close #123
```

---

## 五、AI 编程特别提示

### 5.1 Java 开发

1. **优先使用 Builder 模式**：复杂对象使用 `@Data @Builder`
2. **保持核心模块纯净**：gateway-core 不引入 Spring 依赖
3. **线程安全意识**：使用 ConcurrentHashMap、volatile、AtomicReference
4. **资源释放**：finally 块确保锁释放、连接归还
5. **日志规范**：统一使用 `[类名]` 前缀格式

### 5.2 TypeScript/Vue 开发

1. **类型优先**：所有函数参数和返回值必须有类型定义
2. **组合式函数**：使用 `<script setup>` + Composition API
3. **Pinia 状态管理**：使用 defineStore + composition-api 风格
4. **API 封装**：统一使用 request 工具，模块化 API
5. **组件命名**：PascalCase 文件名，kebab-case 模板中使用

### 5.3 通用规范

1. **代码行数控制**：单个方法/函数不超过 50 行
2. **单一职责**：每个类/函数只做一件事
3. **可读性优先**：清晰命名胜过简短命名
4. **防御性编程**：参数校验，非空判断
5. **一致性**：遵循项目现有代码风格
