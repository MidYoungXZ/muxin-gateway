import type { RouteRecordRaw } from 'vue-router'
import type { MenuItem } from '@/stores/menu'

const modules = import.meta.glob('@/views/**/*.vue')

export interface RouteMeta {
  title?: string
  icon?: string
  hidden?: boolean
  affix?: boolean
  breadcrumb?: boolean
  cache?: boolean
}

export function generateRoutes(menus: MenuItem[]): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []
  
  for (const menu of menus) {
    if (menu.menuType === 'F') continue
    
    if (menu.menuType === 'M') {
      const route = generateDirectoryRoute(menu)
      if (route) routes.push(route)
    } else if (menu.menuType === 'C') {
      const route = generateMenuRoute(menu)
      if (route) routes.push(route)
    }
  }
  
  return routes
}

function generateDirectoryRoute(menu: MenuItem): RouteRecordRaw | null {
  if (!menu.path) return null
  
  const children: RouteRecordRaw[] = []
  
  if (menu.children && menu.children.length > 0) {
    for (const child of menu.children) {
      if (child.menuType === 'F') continue
      
      if (child.menuType === 'C') {
        const childRoute = generateMenuRoute(child)
        if (childRoute) children.push(childRoute)
      } else if (child.menuType === 'M') {
        const nestedRoute = generateDirectoryRoute(child)
        if (nestedRoute) children.push(nestedRoute)
      }
    }
  }
  
  const route: RouteRecordRaw = {
    path: menu.path,
    name: routeNameFromPath(menu.path),
    meta: {
      title: menu.menuName,
      icon: menu.icon,
      hidden: menu.visible !== 1
    } as RouteMeta,
    children: children.length > 0 ? children : undefined
  }
  
  if (!route.children || route.children.length === 0) {
    route.redirect = menu.path
  }
  
  return route
}

function generateMenuRoute(menu: MenuItem): RouteRecordRaw | null {
  if (!menu.path) return null
  
  const route: RouteRecordRaw = {
    path: menu.path,
    name: routeNameFromPath(menu.path),
    meta: {
      title: menu.menuName,
      icon: menu.icon,
      hidden: menu.visible !== 1
    } as RouteMeta,
    component: loadComponent(menu.component)
  }
  
  return route
}

function loadComponent(component?: string) {
  if (!component) {
    return () => import('@/layouts/blank.vue')
  }
  
  const componentPath = component.startsWith('/') ? component : `/${component}`
  const fullPath = `/src/views${componentPath}.vue`
  
  if (modules[fullPath]) {
    return modules[fullPath]
  }
  
  console.warn(`Component not found: ${fullPath}, using blank component`)
  return () => import('@/layouts/blank.vue')
}

function routeNameFromPath(path: string): string {
  if (!path) return ''
  return path
    .split('/')
    .filter(Boolean)
    .map(segment => segment.charAt(0).toUpperCase() + segment.slice(1))
    .join('')
}

export function flattenRoutes(routes: RouteRecordRaw[]): RouteRecordRaw[] {
  const result: RouteRecordRaw[] = []
  
  for (const route of routes) {
    if (route.children && route.children.length > 0) {
      result.push(...route.children)
    } else {
      result.push(route)
    }
  }
  
  return result
}