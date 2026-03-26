import router from '@/router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'

const whiteList = ['/login', '/404', '/403']

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const menuStore = useMenuStore()
  
  document.title = to.meta?.title ? `${to.meta.title} - Muxin Gateway` : 'Muxin Gateway'
  
  if (userStore.isLoggedIn) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      if (!menuStore.routesLoaded) {
        try {
          await menuStore.initRoutes()
          next({ ...to, replace: true })
        } catch (error) {
          console.error('初始化路由失败:', error)
          await userStore.logout()
          next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
        }
      } else {
        next()
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

router.afterEach(() => {
})