<template>
  <div class="step-navigation">
    <div class="step-title">配置步骤</div>
    <div class="step-list">
      <div
        v-for="(step, index) in steps"
        :key="index"
        class="step-item"
        :class="{ 
          active: currentStep === index, 
          completed: index < currentStep,
          disabled: index > maxAccessibleStep
        }"
        @click="handleStepClick(index)"
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
import { computed } from 'vue'
import { Check } from '@element-plus/icons-vue'

const props = defineProps<{
  currentStep: number
  completedSteps?: number[]
}>()

const emit = defineEmits<{
  'update:currentStep': [value: number]
}>()

const steps = [
  { label: '基本信息', key: 'basic' },
  { label: '路由匹配', key: 'matching' },
  { label: '目标服务', key: 'service' },
  { label: '插件配置', key: 'plugins' }
]

const maxAccessibleStep = computed(() => {
  if (!props.completedSteps || props.completedSteps.length === 0) {
    return props.currentStep
  }
  const maxCompleted = Math.max(...props.completedSteps, -1)
  return Math.min(maxCompleted + 1, steps.length - 1)
})

function handleStepClick(index: number) {
  if (index <= maxAccessibleStep.value) {
    emit('update:currentStep', index)
  }
}

function getStepStatus(index: number): string {
  if (index < props.currentStep) return '已完成'
  if (index === props.currentStep) return '进行中'
  return '待完成'
}
</script>

<style lang="scss" scoped>
.step-navigation {
  width: 200px;
  height: 100%;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-primary);
  padding: 20px 0;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
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

  &:hover:not(.disabled) {
    background: var(--bg-tertiary);
  }

  &.active {
    background: var(--primary-100);
    border-left-color: var(--primary-color);

    .step-indicator {
      background: var(--primary-color);
      color: #fff;
    }

    .step-label {
      color: var(--primary-color);
      font-weight: 600;
    }
    
    .step-status {
      color: var(--primary-color);
    }
  }

  &.completed {
    .step-indicator {
      background: var(--success-color);
      color: #fff;
    }

    .step-status {
      color: var(--success-color);
    }
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.6;

    .step-indicator {
      background: var(--bg-tertiary);
      color: var(--text-disabled);
    }

    .step-label {
      color: var(--text-disabled);
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
  background: var(--bg-tertiary);
  color: var(--text-secondary);
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
  color: var(--text-primary);
  margin-bottom: 2px;
}

.step-status {
  font-size: 12px;
  color: var(--text-tertiary);
}
</style>