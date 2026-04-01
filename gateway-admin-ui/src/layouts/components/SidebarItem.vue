<template>
  <template v-if="visible">
    <el-sub-menu v-if="hasChildren" :index="menuIndex">
      <template #title>
        <el-icon v-if="icon"><component :is="icon" /></el-icon>
        <span>{{ title }}</span>
      </template>
      <sidebar-item
        v-for="child in children"
        :key="childKey(child)"
        :item="child"
        :base-path="menuPath"
        :level="level + 1"
      />
    </el-sub-menu>
    
    <el-menu-item v-else :index="menuPath">
      <el-icon v-if="icon"><component :is="icon" /></el-icon>
      <template #title><span>{{ title }}</span></template>
    </el-menu-item>
  </template>
</template>

<script setup lang="ts">
import type { MenuItem } from '@/stores/menu'

const props = withDefaults(defineProps<{
  item: any
  basePath?: string
  level?: number
}>(), {
  basePath: '',
  level: 1
})

const isMenuItem = computed(() => 'menuType' in props.item)
const menuPath = computed(() => resolvePath(props.item.path))
const menuIndex = computed(() => props.item.path || String(props.item.id))
const title = computed(() => props.item.menuName || props.item.meta?.title || props.item.name)
const icon = computed(() => props.item.icon || props.item.meta?.icon)

const visible = computed(() => {
  if (isMenuItem.value) {
    return props.item.menuType !== 'F' && props.item.visible === 1
  }
  return !props.item.meta?.hidden
})

const children = computed(() => {
  const list = props.item.children || []
  if (isMenuItem.value) {
    return list.filter((c: MenuItem) => c.menuType !== 'F' && c.visible === 1)
  }
  return list.filter((c: any) => !c.meta?.hidden)
})

const hasChildren = computed(() => {
  if (isMenuItem.value) {
    if (props.item.menuType === 'M') {
      return children.value.length > 0
    }
    return false
  }
  return children.value.length > 0
})

function childKey(child: any) {
  return child.id || child.path
}

function resolvePath(path?: string) {
  if (!path || path.startsWith('/') || /^(https?:|mailto:|tel:)/.test(path)) {
    return path || props.basePath
  }
  return `${props.basePath}/${path}`.replace(/\/+/g, '/')
}
</script>

<style lang="scss" scoped>
:deep(.el-menu-item) {
  padding-left: calc(20px + var(--level, 1) * 16px) !important;
}

:deep(.el-sub-menu > .el-sub-menu__title) {
  padding-left: calc(20px + var(--level, 1) * 16px) !important;
}
</style>