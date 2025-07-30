/// <reference types="vite/client" />

// Vue 单文件组件类型声明
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// Element Plus 全局组件类型声明
declare module '@vue/runtime-core' {
  export interface GlobalComponents {
    ElButton: typeof import('element-plus')['ElButton']
    ElInput: typeof import('element-plus')['ElInput']
    ElForm: typeof import('element-plus')['ElForm']
    ElFormItem: typeof import('element-plus')['ElFormItem']
    ElTable: typeof import('element-plus')['ElTable']
    ElTableColumn: typeof import('element-plus')['ElTableColumn']
    ElCard: typeof import('element-plus')['ElCard']
    ElMenu: typeof import('element-plus')['ElMenu']
    ElMenuItem: typeof import('element-plus')['ElMenuItem']
    ElSubMenu: typeof import('element-plus')['ElSubMenu']
    ElDropdown: typeof import('element-plus')['ElDropdown']
    ElDropdownMenu: typeof import('element-plus')['ElDropdownMenu']
    ElDropdownItem: typeof import('element-plus')['ElDropdownItem']
    ElPagination: typeof import('element-plus')['ElPagination']
    ElDialog: typeof import('element-plus')['ElDialog']
    ElDrawer: typeof import('element-plus')['ElDrawer']
    ElSelect: typeof import('element-plus')['ElSelect']
    ElOption: typeof import('element-plus')['ElOption']
    ElCheckbox: typeof import('element-plus')['ElCheckbox']
    ElRadio: typeof import('element-plus')['ElRadio']
    ElSwitch: typeof import('element-plus')['ElSwitch']
    ElDatePicker: typeof import('element-plus')['ElDatePicker']
    ElTimePicker: typeof import('element-plus')['ElTimePicker']
    ElUpload: typeof import('element-plus')['ElUpload']
    ElTree: typeof import('element-plus')['ElTree']
    ElTabs: typeof import('element-plus')['ElTabs']
    ElTabPane: typeof import('element-plus')['ElTabPane']
    ElBreadcrumb: typeof import('element-plus')['ElBreadcrumb']
    ElBreadcrumbItem: typeof import('element-plus')['ElBreadcrumbItem']
    ElSteps: typeof import('element-plus')['ElSteps']
    ElStep: typeof import('element-plus')['ElStep']
    ElTooltip: typeof import('element-plus')['ElTooltip']
    ElPopover: typeof import('element-plus')['ElPopover']
    ElPopconfirm: typeof import('element-plus')['ElPopconfirm']
    ElTag: typeof import('element-plus')['ElTag']
    ElBadge: typeof import('element-plus')['ElBadge']
    ElAlert: typeof import('element-plus')['ElAlert']
    ElProgress: typeof import('element-plus')['ElProgress']
    ElSkeleton: typeof import('element-plus')['ElSkeleton']
    ElEmpty: typeof import('element-plus')['ElEmpty']
    ElResult: typeof import('element-plus')['ElResult']
    ElDescriptions: typeof import('element-plus')['ElDescriptions']
    ElDescriptionsItem: typeof import('element-plus')['ElDescriptionsItem']
    ElStatistic: typeof import('element-plus')['ElStatistic']
    ElRate: typeof import('element-plus')['ElRate']
    ElSlider: typeof import('element-plus')['ElSlider']
    ElTransfer: typeof import('element-plus')['ElTransfer']
  }
}

// Vue Composition API 全局导入
declare global {
  const ref: typeof import('vue')['ref']
  const reactive: typeof import('vue')['reactive']
  const computed: typeof import('vue')['computed']
  const watch: typeof import('vue')['watch']
  const watchEffect: typeof import('vue')['watchEffect']
  const onMounted: typeof import('vue')['onMounted']
  const onUpdated: typeof import('vue')['onUpdated']
  const onUnmounted: typeof import('vue')['onUnmounted']
  const onBeforeMount: typeof import('vue')['onBeforeMount']
  const onBeforeUpdate: typeof import('vue')['onBeforeUpdate']
  const onBeforeUnmount: typeof import('vue')['onBeforeUnmount']
  const nextTick: typeof import('vue')['nextTick']
  const defineComponent: typeof import('vue')['defineComponent']
  const defineAsyncComponent: typeof import('vue')['defineAsyncComponent']
  const toRef: typeof import('vue')['toRef']
  const toRefs: typeof import('vue')['toRefs']
  const unref: typeof import('vue')['unref']
  const provide: typeof import('vue')['provide']
  const inject: typeof import('vue')['inject']
  const getCurrentInstance: typeof import('vue')['getCurrentInstance']
  const shallowRef: typeof import('vue')['shallowRef']
  const shallowReactive: typeof import('vue')['shallowReactive']
  const markRaw: typeof import('vue')['markRaw']
  const toRaw: typeof import('vue')['toRaw']
  const isRef: typeof import('vue')['isRef']
  const isReactive: typeof import('vue')['isReactive']
  const isReadonly: typeof import('vue')['isReadonly']
  const isProxy: typeof import('vue')['isProxy']

  // Vue Router
  const useRoute: typeof import('vue-router')['useRoute']
  const useRouter: typeof import('vue-router')['useRouter']

  // Pinia
  const defineStore: typeof import('pinia')['defineStore']
  const storeToRefs: typeof import('pinia')['storeToRefs']

  // Element Plus 消息和通知
  const ElMessage: typeof import('element-plus')['ElMessage']
  const ElMessageBox: typeof import('element-plus')['ElMessageBox']
  const ElNotification: typeof import('element-plus')['ElNotification']
  const ElLoading: typeof import('element-plus')['ElLoading']
}

// 环境变量类型声明
interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_API_BASE_URL: string
  readonly VITE_WS_BASE_URL: string
  readonly VITE_APP_ENV: 'development' | 'production' | 'test'
  readonly VITE_PUBLIC_PATH: string
  readonly VITE_DROP_CONSOLE: string
  readonly VITE_DROP_DEBUGGER: string
  readonly VITE_SOURCEMAP: string
  readonly VITE_OUT_DIR: string
  readonly VITE_ASSETS_DIR: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// CSS 模块类型声明
declare module '*.css' {
  const classes: { readonly [key: string]: string }
  export default classes
}

declare module '*.scss' {
  const classes: { readonly [key: string]: string }
  export default classes
}

declare module '*.sass' {
  const classes: { readonly [key: string]: string }
  export default classes
}

declare module '*.less' {
  const classes: { readonly [key: string]: string }
  export default classes
}

declare module '*.stylus' {
  const classes: { readonly [key: string]: string }
  export default classes
}

declare module '*.styl' {
  const classes: { readonly [key: string]: string }
  export default classes
}

// 图片资源类型声明
declare module '*.png' {
  const src: string
  export default src
}

declare module '*.jpg' {
  const src: string
  export default src
}

declare module '*.jpeg' {
  const src: string
  export default src
}

declare module '*.gif' {
  const src: string
  export default src
}

declare module '*.svg' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent
  export default component
}

declare module '*.webp' {
  const src: string
  export default src
}

declare module '*.ico' {
  const src: string
  export default src
}

declare module '*.bmp' {
  const src: string
  export default src
}

// 字体资源类型声明
declare module '*.woff' {
  const src: string
  export default src
}

declare module '*.woff2' {
  const src: string
  export default src
}

declare module '*.eot' {
  const src: string
  export default src
}

declare module '*.ttf' {
  const src: string
  export default src
}

declare module '*.otf' {
  const src: string
  export default src
}

// 媒体资源类型声明
declare module '*.mp4' {
  const src: string
  export default src
}

declare module '*.webm' {
  const src: string
  export default src
}

declare module '*.ogg' {
  const src: string
  export default src
}

declare module '*.mp3' {
  const src: string
  export default src
}

declare module '*.wav' {
  const src: string
  export default src
}

declare module '*.flac' {
  const src: string
  export default src
}

declare module '*.aac' {
  const src: string
  export default src
}

// JSON 模块类型声明
declare module '*.json' {
  const value: any
  export default value
}

// Worker 类型声明
declare module '*?worker' {
  const WorkerConstructor: {
    new (): Worker
  }
  export default WorkerConstructor
}

declare module '*?worker&inline' {
  const WorkerConstructor: {
    new (): Worker
  }
  export default WorkerConstructor
}

// URL 导入类型声明
declare module '*?url' {
  const url: string
  export default url
}

declare module '*?raw' {
  const content: string
  export default content
}

declare module '*?inline' {
  const content: string
  export default content
}

export {} 