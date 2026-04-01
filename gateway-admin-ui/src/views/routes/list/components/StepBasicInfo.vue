<template>
  <div class="step-basic-info">
    <div class="section-title">路由标识</div>
    <el-form :model="modelValue" :rules="rules" ref="formRef" label-position="top">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="路由ID" prop="routeId">
            <el-input
              :model-value="modelValue.routeId"
              @update:model-value="updateField('routeId', $event)"
              placeholder="请输入路由ID"
              :disabled="isEdit"
            />
            <div class="field-tip">只能包含字母、数字、下划线和中划线，全局唯一标识</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="路由名称" prop="routeName">
            <el-input
              :model-value="modelValue.routeName"
              @update:model-value="updateField('routeName', $event)"
              placeholder="请输入路由名称"
            />
            <div class="field-tip">路由的显示名称，便于识别和管理</div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div class="section-title">路由设置</div>
    <el-form :model="modelValue" label-position="top">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="路由优先级">
            <el-input-number
              :model-value="modelValue.order"
              @update:model-value="updateField('order', $event)"
              :min="0"
              :max="9999"
              style="width: 100%"
            />
            <div class="field-tip">数值越大优先级越高，当多个路由匹配时优先执行</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="启用状态">
            <el-switch
              :model-value="modelValue.enabled"
              @update:model-value="updateField('enabled', $event)"
              active-text="启用此路由"
            />
            <div class="field-tip">启用后路由立即生效，禁用则不会匹配任何请求</div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div class="section-title">路由描述</div>
    <el-form :model="modelValue" label-position="top">
      <el-form-item>
        <el-input
          :model-value="modelValue.description"
          @update:model-value="updateField('description', $event)"
          type="textarea"
          :rows="3"
          placeholder="请输入路由描述（可选）"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { RouteFormState } from '@/api/routes'

const props = defineProps<{
  modelValue: RouteFormState
  isEdit: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: RouteFormState]
}>()

const formRef = ref<FormInstance>()

const rules: FormRules = {
  routeId: [
    { required: true, message: '请输入路由ID', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9-_]+$/, message: '只能包含字母、数字、下划线和中划线', trigger: 'blur' }
  ],
  routeName: [
    { required: true, message: '请输入路由名称', trigger: 'blur' }
  ]
}

function updateField(field: keyof RouteFormState, value: any) {
  emit('update:modelValue', { ...props.modelValue, [field]: value })
}

async function validate(): Promise<boolean> {
  if (!formRef.value) return false
  try {
    await formRef.value.validate()
    return true
  } catch {
    return false
  }
}

defineExpose({ validate })
</script>

<style lang="scss" scoped>
.step-basic-info {
  padding: 0 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.field-tip {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-top: 2px;
  line-height: 1.4;
}

:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  padding-bottom: 2px;
}
</style>