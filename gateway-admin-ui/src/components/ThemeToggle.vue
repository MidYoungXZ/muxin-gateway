<template>
  <el-tooltip
    :content="isDark ? '切换到亮色模式' : '切换到暗色模式'"
    placement="bottom"
  >
    <div class="theme-toggle" @click="toggleTheme">
      <el-icon :size="20">
        <svg v-if="isDark" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
          <path fill="currentColor" d="M240.448 240.448a384 384 0 1 0 559.424 0 384 384 0 0 0-559.424 0zm181.056 362.048a384 384 0 0 0 559.424 0 384 384 0 1 1-559.424 0z"/>
        </svg>
        <svg v-else viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
          <path fill="currentColor" d="M512 192a32 32 0 0 1 32 32v64a32 32 0 0 1-64 0v-64a32 32 0 0 1 32-32zm0 640a32 32 0 0 1 32 32v64a32 32 0 1 1-64 0v-64a32 32 0 0 1 32-32zm448-192a32 32 0 0 1-32 32h-64a32 32 0 1 1 0-64h64a32 32 0 0 1 32 32zm-640 0a32 32 0 0 1-32 32H224a32 32 0 0 1 0-64h64a32 32 0 0 1 32 32zM195.2 195.2a32 32 0 0 1 45.248 0L285.696 240.448a32 32 0 1 1-45.248 45.248L195.2 240.448a32 32 0 0 1 0-45.248zm543.104 543.104a32 32 0 0 1 45.248 0L828.8 783.552a32 32 0 0 1-45.248 45.248L738.304 783.552a32 32 0 0 1 0-45.248zM828.8 195.2a32 32 0 0 1 0 45.248L783.552 285.696a32 32 0 1 1-45.248-45.248L783.552 195.2a32 32 0 0 1 45.248 0zM285.696 738.304a32 32 0 0 1 0 45.248L240.448 828.8a32 32 0 0 1-45.248-45.248l45.248-45.248a32 32 0 0 1 45.248 0zM512 320a192 192 0 1 1 0 384 192 192 0 0 1 0-384z"/>
        </svg>
      </el-icon>
    </div>
  </el-tooltip>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

const { theme, toggleTheme } = useTheme()

const isDark = computed(() => theme.value === 'dark')
</script>

<style lang="scss" scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base) ease;
  
  &:hover {
    background-color: var(--bg-secondary);
  }
  
  .el-icon {
    color: var(--text-primary);
    transition: transform var(--transition-base) ease;
  }
  
  &:hover .el-icon {
    transform: rotate(180deg);
  }
}
</style> 