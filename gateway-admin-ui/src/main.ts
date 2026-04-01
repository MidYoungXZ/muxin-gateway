import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './router/permission'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import {
  User, Lock, Menu, Setting, Monitor, Connection, House, HomeFilled,
  Expand, Fold, SwitchButton, Bell, Search, Plus, Edit, Delete, View,
  RefreshRight, ArrowRight, ArrowLeft, ArrowUp, ArrowDown, Close, Check,
  Warning, InfoFilled, SuccessFilled, CircleClose, QuestionFilled, FolderRemove,
  Back, Right, Guide, List, UserFilled, OfficeBuilding, Document, Key,
  SetUp, CircleCheck, Folder
} from '@element-plus/icons-vue'

import './styles/index.scss'

import permission from './directives/permission'

const app = createApp(App)
const pinia = createPinia()

const icons = {
  User, Lock, Menu, Setting, Monitor, Connection, House, HomeFilled,
  Expand, Fold, SwitchButton, Bell, Search, Plus, Edit, Delete, View,
  RefreshRight, ArrowRight, ArrowLeft, ArrowUp, ArrowDown, Close, Check,
  Warning, InfoFilled, SuccessFilled, CircleClose, QuestionFilled, FolderRemove,
  Back, Right, Guide, List, UserFilled, OfficeBuilding, Document, Key,
  SetUp, CircleCheck, Folder
}

Object.entries(icons).forEach(([key, component]) => {
  app.component(key, component)
})

app.use(ElementPlus, { locale: zhCn, size: 'default' })
app.use(pinia)
app.use(router)
app.directive('permission', permission)

import { initTheme } from '@/composables/useTheme'
initTheme()

async function bootstrap() {
  const storedToken = localStorage.getItem('user-token')
  if (!storedToken) {
    localStorage.removeItem('user-token')
    localStorage.removeItem('user-token-type')
    localStorage.removeItem('user-info')
  }
  
  if (storedToken) {
    const { useUserStore } = await import('@/stores/user')
    const userStore = useUserStore()
    await userStore.init()
  }
  
  app.mount('#app')
}

bootstrap()