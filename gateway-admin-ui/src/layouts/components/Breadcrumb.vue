<template>
  <el-breadcrumb separator="/" class="custom-breadcrumb">
    <el-breadcrumb-item :to="{ path: '/' }">
      <el-icon><HomeFilled /></el-icon>
      <span>首页</span>
    </el-breadcrumb-item>
    <el-breadcrumb-item
      v-for="(item, index) in breadcrumbs"
      :key="item.path"
    >
      <span v-if="index === breadcrumbs.length - 1" class="breadcrumb-current">{{ item.meta?.title }}</span>
      <router-link v-else :to="item.path" class="breadcrumb-link">{{ item.meta?.title }}</router-link>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { RouteLocationMatched } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title && item.path !== '/')
  
  const first = matched[0]
  if (isDashboard(first)) {
    return []
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

<style lang="scss" scoped>
.custom-breadcrumb {
  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      display: flex;
      align-items: center;
      gap: 4px;
      
      .el-icon {
        font-size: 14px;
      }
    }
    
    .el-breadcrumb__separator {
      color: var(--text-tertiary);
    }
  }
}

.breadcrumb-link {
  color: var(--text-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
  
  &:hover {
    color: var(--primary-color);
  }
}

.breadcrumb-current {
  color: var(--text-primary);
  font-weight: var(--font-medium);
}
</style> 