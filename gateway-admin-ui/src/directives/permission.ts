import { useMenuStore } from '@/stores/menu'

const permission = {
  mounted(el: any, binding: any) {
    const menuStore = useMenuStore()
    const { value } = binding
    
    if (value) {
      const permissions = Array.isArray(value) ? value : [value]
      const hasPermission = menuStore.hasAnyPermissions(permissions)
      if (!hasPermission) {
        el.parentNode?.removeChild(el)
      }
    }
  },
  
  updated(el: any, binding: any) {
    const menuStore = useMenuStore()
    const { value } = binding
    
    if (value) {
      const permissions = Array.isArray(value) ? value : [value]
      const hasPermission = menuStore.hasAnyPermissions(permissions)
      if (!hasPermission) {
        el.style.display = 'none'
      } else {
        el.style.display = ''
      }
    }
  }
}

export default permission