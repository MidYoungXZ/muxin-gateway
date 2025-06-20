import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/index.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'House' }
      },
      {
        path: '/monitor',
        name: 'Monitor',
        redirect: '/monitor/realtime',
        meta: { title: '监控大屏', icon: 'Monitor' },
        children: [
          {
            path: '/monitor/realtime',
            name: 'RealtimeStats',
            component: () => import('@/views/monitor/realtime/index.vue'),
            meta: { title: '实时请求统计' }
          },
          {
            path: '/monitor/routes',
            name: 'RouteMonitor',
            component: () => import('@/views/monitor/routes/index.vue'),
            meta: { title: '路由调用监控' }
          },
          {
            path: '/monitor/filters',
            name: 'FilterMonitor',
            component: () => import('@/views/monitor/filters/index.vue'),
            meta: { title: '过滤器命中监控' }
          }
        ]
      },
      {
        path: '/routes',
        name: 'Routes',
        redirect: '/routes/list',
        meta: { title: '路由管理', icon: 'Connection' },
        children: [
          {
            path: '/routes/list',
            name: 'RouteList',
            component: () => import('@/views/routes/list/index.vue'),
            meta: { title: '路由列表' }
          },
          {
            path: '/routes/test',
            name: 'RouteTest',
            component: () => import('@/views/routes/test/index.vue'),
            meta: { title: '路由测试工具' }
          },
          {
            path: '/routes/nodes',
            name: 'NodeManage',
            component: () => import('@/views/routes/nodes/index.vue'),
            meta: { title: '节点管理' }
          },
          {
            path: '/routes/loadbalance',
            name: 'LoadBalance',
            component: () => import('@/views/routes/loadbalance/index.vue'),
            meta: { title: '负载均衡配置' }
          },
          {
            path: '/routes/filters',
            name: 'FilterManage',
            component: () => import('@/views/routes/filters/index.vue'),
            meta: { title: '过滤器管理' }
          },
          {
            path: '/routes/predicates',
            name: 'PredicateManage',
            component: () => import('@/views/routes/predicates/index.vue'),
            meta: { title: '断言管理' }
          }
        ]
      },
      {
        path: '/auth',
        name: 'Auth',
        redirect: '/auth/api-permission',
        meta: { title: '鉴权规则', icon: 'Lock' },
        children: [
          {
            path: '/auth/api-permission',
            name: 'ApiPermission',
            component: () => import('@/views/auth/api-permission/index.vue'),
            meta: { title: 'API权限控制' }
          },
          {
            path: '/auth/whitelist',
            name: 'Whitelist',
            component: () => import('@/views/auth/whitelist/index.vue'),
            meta: { title: '白名单配置' }
          }
        ]
      },
      {
        path: '/system',
        name: 'System',
        redirect: '/system/users',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          {
            path: '/system/users',
            name: 'UserManage',
            component: () => import('@/views/system/users/index.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: '/system/roles',
            name: 'RoleManage',
            component: () => import('@/views/system/roles/index.vue'),
            meta: { title: '角色管理' }
          },
          {
            path: '/system/departments',
            name: 'DepartmentManage',
            component: () => import('@/views/system/departments/index.vue'),
            meta: { title: '部门管理' }
          },
          {
            path: '/system/permissions',
            name: 'PermissionManage',
            component: () => import('@/views/system/permissions/index.vue'),
            meta: { title: '权限管理' }
          },
          {
            path: '/system/operation-logs',
            name: 'OperationLog',
            component: () => import('@/views/system/operation-logs/index.vue'),
            meta: { title: '操作日志' }
          },
          {
            path: '/system/config',
            name: 'SystemConfig',
            component: () => import('@/views/system/config/index.vue'),
            meta: { title: '系统配置' }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const { useUserStore } = await import('@/stores/user')
  const userStore = useUserStore()
  
  // 初始化用户信息
  if (!userStore.isLoggedIn) {
    await userStore.init()
  }
  
  // 白名单路由（无需登录）
  const whiteList = ['/login']
  
  if (userStore.isLoggedIn) {
    if (to.path === '/login') {
      // 已登录用户访问登录页，重定向到首页
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      // 在白名单中，直接进入
      next()
    } else {
      // 未登录且不在白名单中，重定向到登录页
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
  }
})

export default router 