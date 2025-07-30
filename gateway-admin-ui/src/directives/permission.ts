import { useUserStore } from '@/stores/user'

const permission = {
  mounted(el: any, binding: any) {
    const userStore = useUserStore()
    const { value } = binding
    
    if (value) {
      const hasPermission = userStore.hasPermission(value)
      if (!hasPermission) {
        el.style.display = 'none'
      }
    }
  },
  
  updated(el: any, binding: any) {
    const userStore = useUserStore()
    const { value } = binding
    
    if (value) {
      const hasPermission = userStore.hasPermission(value)
      if (!hasPermission) {
        el.style.display = 'none'
      } else {
        el.style.display = ''
      }
    }
  }
}

export default permission 