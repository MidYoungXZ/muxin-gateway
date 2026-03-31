<template>
  <div class="step-navigation">
    <div class="step-title">配置步骤</div>
    <div class="step-list">
      <div
        v-for="(step, index) in steps"
        :key="index"
        class="step-item"
        :class="{ active: currentStep === index, completed: index < currentStep }"
        @click="$emit('update:currentStep', index)"
      >
        <div class="step-indicator">
          <span v-if="index < currentStep" class="step-check">
            <el-icon><Check /></el-icon>
          </span>
          <span v-else class="step-number">{{ index + 1 }}</span>
        </div>
        <div class="step-content">
          <div class="step-label">{{ step.label }}</div>
          <div class="step-status">{{ getStepStatus(index) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Check } from '@element-plus/icons-vue'

defineProps<{
  currentStep: number
}>()

defineEmits<{
  'update:currentStep': [value: number]
}>()

const steps = [
  { label: '基本信息', key: 'basic' },
  { label: '路由匹配', key: 'matching' },
  { label: '目标服务', key: 'service' },
  { label: '插件配置', key: 'plugins' }
]

function getStepStatus(index: number): string {
  return index < 2 ? '已完成' : '待完成'
}
</script>

<style lang="scss" scoped>
.step-navigation {
  width: 200px;
  height: 100%;
  background: var(--el-fill-color-light);
  border-right: 1px solid var(--el-border-color-light);
  padding: 20px 0;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  padding: 0 20px;
  margin-bottom: 20px;
}

.step-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.step-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;

  &:hover {
    background: var(--el-fill-color);
  }

  &.active {
    background: var(--el-color-primary-light-9);
    border-left-color: var(--el-color-primary);

    .step-indicator {
      background: var(--el-color-primary);
      color: #fff;
    }

    .step-label {
      color: var(--el-color-primary);
      font-weight: 600;
    }
  }

  &.completed {
    .step-indicator {
      background: var(--el-color-success);
      color: #fff;
    }

    .step-status {
      color: var(--el-color-success);
    }
  }
}

.step-indicator {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color);
  color: var(--el-text-color-secondary);
  margin-right: 12px;
  flex-shrink: 0;
  transition: all 0.2s;
}

.step-number {
  font-size: 14px;
  font-weight: 500;
}

.step-check {
  font-size: 14px;
}

.step-content {
  flex: 1;
  min-width: 0;
}

.step-label {
  font-size: 14px;
  color: var(--el-text-color-primary);
  margin-bottom: 2px;
}

.step-status {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
</style>