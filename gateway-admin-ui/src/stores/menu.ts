import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export interface MenuItem {
  id: number
  parentId: number
  menuName: string
  menuType: 'M' | 'C' | 'F'
  path: string
  icon: string
  visible: number
  children?: MenuItem[]
}

export const useMenuStore = defineStore('menu', () => {
  const menus = ref<MenuItem[]>([])
  const permissions = ref<string[]>([])
  const isLoaded = ref(false)

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

  function hasPermission(permission: string) {
    return permissions.value.includes(permission)
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
    fetchUserMenus,
    fetchUserPermissions,
    hasPermission,
    clearMenus
  }
})