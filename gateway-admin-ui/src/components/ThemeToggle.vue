<template>
  <el-tooltip
    :content="isDark ? '切换到亮色模式' : '切换到暗色模式'"
    placement="bottom"
  >
    <div class="theme-toggle" @click="toggleTheme">
      <el-icon :size="18">
        <Moon v-if="!isDark" />
        <Sunny v-else />
      </el-icon>
    </div>
  </el-tooltip>
</template>

<script setup lang="ts">
import { Moon, Sunny } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

const { theme, toggleTheme } = useTheme()

const isDark = computed(() => theme.value === 'dark')
</script>

<style lang="scss" scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-secondary);
  
  &:hover {
    color: var(--primary-color);
    background: var(--primary-50);
  }
}
</style> 