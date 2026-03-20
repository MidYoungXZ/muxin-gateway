import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export interface MenuItem {
  id: number
  parentId: number
  menuName: string
  i18nCode: string
  menuType: 'M' | 'C' | 'F'
  path: string
  component: string
  perms: string
  icon: string
  sortOrder: number
  visible: number
  status: number
  children?: MenuItem[]
}

export interface RouteMeta {
  title: string
  icon?: string
  hidden?: boolean
}

export interface AppRoute {
  path: string
  name: string
  component?: any
  meta: RouteMeta
  children?: AppRoute[]
}

export const useMenuStore = defineStore('menu', () => {
  const menus = ref<MenuItem[]>([])
  const permissions = ref<string[]>([])
  const isLoaded = ref(false)

  const menuTree = computed(() => menus.value)

  async function fetchUserMenus() {
    if (isLoaded.value) return menus.value

    try {
      const res = await request({
        url: '/api/menus/user-tree',
        method: 'get'
      })

      if (res.data) {
        menus.value = res.data
        isLoaded.value = true
      }
      return menus.value
    } catch (error) {
      console.error('获取用户菜单失败:', error)
      return []
    }
  }

  async function fetchUserPermissions() {
    try {
      const res = await request({
        url: '/api/menus/user-permissions',
        method: 'get'
      })

      if (res.data) {
        permissions.value = res.data
      }
      return permissions.value
    } catch (error) {
      console.error('获取用户权限失败:', error)
      return []
    }
  }

  function hasPermission(permission: string): boolean {
    return permissions.value.includes(permission)
  }

  function hasAnyPermissions(permissionList: string[]): boolean {
    return permissionList.some(p => permissions.value.includes(p))
  }

  function hasAllPermissions(permissionList: string[]): boolean {
    return permissionList.every(p => permissions.value.includes(p))
  }

  function buildRoutes(menuList: MenuItem[]): AppRoute[] {
    const routes: AppRoute[] = []

    for (const menu of menuList) {
      if (menu.menuType === 'M') {
        const route: AppRoute = {
          path: menu.path,
          name: `menu_${menu.id}`,
          meta: {
            title: menu.menuName,
            icon: menu.icon || undefined,
            hidden: menu.visible !== 1
          },
          children: menu.children ? buildRoutes(menu.children) : []
        }
        routes.push(route)
      } else if (menu.menuType === 'C') {
        const route: AppRoute = {
          path: menu.path,
          name: `page_${menu.id}`,
          meta: {
            title: menu.menuName,
            icon: menu.icon || undefined,
            hidden: menu.visible !== 1
          },
          children: []
        }

        if (menu.component) {
          route.component = () => import(`@/views/${menu.component}.vue`)
        }

        routes.push(route)
      }
    }

    return routes
  }

  function clearMenus() {
    menus.value = []
    permissions.value = []
    isLoaded.value = false
  }

  return {
    menus,
    permissions,
    isLoaded,
    menuTree,
    fetchUserMenus,
    fetchUserPermissions,
    hasPermission,
    hasAnyPermissions,
    hasAllPermissions,
    buildRoutes,
    clearMenus
  }
})