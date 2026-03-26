import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import request from '@/utils/request'
import router, { asyncRoutes, resetRouter } from '@/router'
import { generateRoutes } from '@/router/utils'

export interface MenuItem {
  id: number
  parentId: number
  menuName: string
  menuType: 'M' | 'C' | 'F'
  path: string
  component?: string
  icon: string
  visible: number
  children?: MenuItem[]
}

export const useMenuStore = defineStore('menu', () => {
  const menus = ref<MenuItem[]>([])
  const permissions = ref<string[]>([])
  const isLoaded = ref(false)
  const routesLoaded = ref(false)

  const menuRoutes = computed(() => menus.value)

  async function fetchUserMenus() {
    if (isLoaded.value) return menus.value
    try {
      const res = await request({ url: '/api/menus/user-tree', method: 'get' })
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
      const res = await request({ url: '/api/menus/user-permissions', method: 'get' })
      if (res.data) permissions.value = res.data
      return permissions.value
    } catch (error) {
      console.error('获取用户权限失败:', error)
      return []
    }
  }

  function generateAndAddRoutes(): RouteRecordRaw[] {
    if (routesLoaded.value) return []
    
    const dynamicRoutes = generateRoutes(menus.value)
    
    router.addRoute(asyncRoutes)
    
    for (const route of dynamicRoutes) {
      router.addRoute(asyncRoutes.name as string, route)
    }
    
    router.addRoute({
      path: '/:pathMatch(.*)*',
      redirect: '/404'
    })
    
    routesLoaded.value = true
    return dynamicRoutes
  }

  async function initRoutes() {
    if (routesLoaded.value) return
    
    await fetchUserMenus()
    await fetchUserPermissions()
    generateAndAddRoutes()
  }

  function hasPermission(permission: string) {
    return permissions.value.includes(permission)
  }

  function clearMenus() {
    menus.value = []
    permissions.value = []
    isLoaded.value = false
    routesLoaded.value = false
    resetRouter()
  }

  return {
    menus,
    permissions,
    isLoaded,
    routesLoaded,
    menuRoutes,
    fetchUserMenus,
    fetchUserPermissions,
    generateAndAddRoutes,
    initRoutes,
    hasPermission,
    clearMenus
  }
})