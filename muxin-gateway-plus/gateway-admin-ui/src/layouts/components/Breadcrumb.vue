<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">
      <el-icon><HomeFilled /></el-icon>
      首页
    </el-breadcrumb-item>
    <el-breadcrumb-item
      v-for="(item, index) in breadcrumbs"
      :key="item.path"
    >
      <span v-if="index === breadcrumbs.length - 1">{{ item.meta?.title }}</span>
      <router-link v-else :to="item.path">{{ item.meta?.title }}</router-link>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
// 使用自动导入的 computed
import { useRoute } from 'vue-router'
import { RouteLocationMatched } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  const first = matched[0]
  
  if (!isDashboard(first)) {
    matched.unshift({ path: '/', meta: { title: '首页' } } as any)
  }
  
  return matched
})

const isDashboard = (route: RouteLocationMatched) => {
  const name = route?.name
  if (!name) {
    return false
  }
  return name === 'Dashboard'
}
</script> 