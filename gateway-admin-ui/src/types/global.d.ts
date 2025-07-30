// 全局类型定义文件
declare global {
  // 常用的实用类型
  type Nullable<T> = T | null
  type Optional<T> = T | undefined
  type RequiredKeys<T, K extends keyof T> = T & Required<Pick<T, K>>
  type PartialKeys<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>

  // API 响应基础类型
  interface ApiResponse<T = any> {
    code: number
    message: string
    data: T
    timestamp?: number
  }

  // 分页响应类型
  interface PageResponse<T = any> {
    total: number
    list: T[]
    page?: number
    size?: number
  }

  // 分页请求参数
  interface PageParams {
    page?: number
    size?: number
    keyword?: string
    [key: string]: any
  }

  // 表单验证规则类型
  interface FormRule {
    required?: boolean
    message?: string
    trigger?: string | string[]
    min?: number
    max?: number
    len?: number
    pattern?: RegExp
    validator?: (rule: any, value: any, callback: any) => void
  }

  type FormRules = Record<string, FormRule[]>

  // 路由元信息类型
  interface RouteMeta {
    title?: string
    icon?: string
    hidden?: boolean
    requireAuth?: boolean
    permissions?: string[]
    roles?: string[]
    keepAlive?: boolean
    breadcrumb?: boolean
  }

  // 菜单项类型
  interface MenuItem {
    id: string | number
    name: string
    path: string
    icon?: string
    component?: string
    redirect?: string
    meta?: RouteMeta
    children?: MenuItem[]
  }

  // 用户信息类型
  interface UserInfo {
    id: number
    username: string
    nickname: string
    email?: string
    phone?: string
    avatar?: string
    roles?: string[]
    permissions?: string[]
    status?: number
    createTime?: string
    lastLoginTime?: string
  }

  // 表格列定义类型
  interface TableColumn {
    prop: string
    label: string
    width?: string | number
    minWidth?: string | number
    align?: 'left' | 'center' | 'right'
    sortable?: boolean
    formatter?: (row: any, column: any, cellValue: any, index: number) => string
    type?: 'selection' | 'index' | 'expand'
    fixed?: boolean | 'left' | 'right'
    showOverflowTooltip?: boolean
  }

  // WebSocket 消息类型
  interface WebSocketMessage<T = any> {
    type: string
    data: T
    timestamp?: number
  }

  // 文件上传类型
  interface UploadFile {
    name: string
    size: number
    type: string
    url?: string
    status?: 'ready' | 'uploading' | 'success' | 'fail'
    percentage?: number
  }

  // 主题类型
  type ThemeMode = 'light' | 'dark' | 'auto'

  // 语言类型
  type Language = 'zh-CN' | 'en-US'

  // 环境类型
  type Environment = 'development' | 'production' | 'test'

  // 窗口对象扩展
  interface Window {
    __VUE_DEVTOOLS_GLOBAL_HOOK__?: any
    webkitRequestFileSystem?: any
    mozRequestFileSystem?: any
  }
}

// Element Plus 组件类型扩展
declare module 'element-plus' {
  // 增强 ElTable 类型
  interface ElTable {
    clearSelection(): void
    toggleRowSelection(row: any, selected?: boolean): void
    toggleAllSelection(): void
    toggleRowExpansion(row: any, expanded?: boolean): void
    setCurrentRow(row: any): void
    clearSort(): void
    clearFilter(columnKey?: string): void
    doLayout(): void
    sort(prop: string, order: string): void
  }

  // 增强 ElForm 类型
  interface ElForm {
    validate(callback?: (valid: boolean, invalidFields?: any) => void): Promise<boolean>
    validateField(props: string | string[], callback?: (errorMessage: string) => void): void
    resetFields(): void
    clearValidate(props?: string | string[]): void
  }

  // 增强 ElUpload 类型
  interface ElUpload {
    abort(file: UploadFile): void
    submit(): void
    clearFiles(): void
    handleStart(rawFile: any): void
    handleRemove(file: UploadFile | any, rawFile?: any): void
  }
}

// Vue Router 类型扩展
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    hidden?: boolean
    requireAuth?: boolean
    permissions?: string[]
    roles?: string[]
    keepAlive?: boolean
    breadcrumb?: boolean
    affix?: boolean
  }
}

export {} 