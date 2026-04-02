import router from '@/router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'

const whiteList = ['/login', '/403']

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const menuStore = useMenuStore()
  
  document.title = to.meta?.title ? `${to.meta.title} - Muxin Gateway` : 'Muxin Gateway'
  
  const hasToken = !!(userStore.token || localStorage.getItem('user-token'))
  
  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      if (!menuStore.routesLoaded) {
        try {
          await userStore.init()
          await menuStore.initRoutes()
          next({ ...to, replace: true })
        } catch (error) {
          console.error('初始化路由失败:', error)
          userStore.clearAuth()
          menuStore.clearMenus()
          next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
        }
      } else {
        if (to.name === 'NotFound' && to.matched.length === 0) {
          next({ path: '/dashboard' })
        } else {
          next()
        }
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
  }
})

router.afterEach(() => {})