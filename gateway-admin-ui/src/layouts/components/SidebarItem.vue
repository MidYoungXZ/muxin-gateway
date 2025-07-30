<template>
  <div v-if="!item.meta?.hidden">
    <!-- 无子菜单 -->
    <el-menu-item
      v-if="!hasChildren"
      :index="resolvePath(onlyOneChild?.path || item.path)"
    >
      <el-icon v-if="icon">
        <component :is="icon" />
      </el-icon>
      <template #title>{{ item.meta?.title || item.name }}</template>
    </el-menu-item>
    
    <!-- 有子菜单 -->
    <el-sub-menu v-else :index="resolvePath(item.path)">
      <template #title>
        <el-icon v-if="icon">
          <component :is="icon" />
        </el-icon>
        <span>{{ item.meta?.title || item.name }}</span>
      </template>
      <sidebar-item
        v-for="child in visibleChildren"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(item.path)"
      />
    </el-sub-menu>
  </div>
</template>

<script setup lang="ts">
// 使用自动导入的 computed, ref
import { RouteRecordRaw } from 'vue-router'
import path from 'path-browserify'

const props = defineProps<{
  item: RouteRecordRaw
  basePath: string
}>()

const onlyOneChild = ref<RouteRecordRaw>()

// 获取可见的子路由
const visibleChildren = computed(() => {
  if (!props.item.children) return []
  return props.item.children.filter((child: any) => !child.meta?.hidden)
})

const hasChildren = computed(() => {
  const children = visibleChildren.value
  
  if (children.length === 0) {
    return false
  }
  
  // 如果只有一个子路由，则直接显示该子路由
  if (children.length === 1 && !children[0].children?.length) {
    onlyOneChild.value = children[0]
    return false
  }
  
  return true
})

const icon = computed(() => {
  // 如果只有一个子路由，使用子路由的图标
  if (!hasChildren.value && onlyOneChild.value) {
    return onlyOneChild.value.meta?.icon || props.item.meta?.icon
  }
  return props.item.meta?.icon
})

const resolvePath = (routePath?: string) => {
  if (!routePath) return props.basePath
  
  if (isExternal(routePath)) {
    return routePath
  }
  
  if (isExternal(props.basePath)) {
    return props.basePath
  }
  
  // 如果路径以 / 开头，说明是绝对路径，直接返回
  if (routePath.startsWith('/')) {
    return routePath
  }
  
  return path.resolve(props.basePath, routePath)
}

const isExternal = (path: string) => {
  return /^(https?:|mailto:|tel:)/.test(path)
}
</script> 