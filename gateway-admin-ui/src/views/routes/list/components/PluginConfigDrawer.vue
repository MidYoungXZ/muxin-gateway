<template>
  <el-drawer
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="plugin ? `配置插件: ${plugin.pluginName}` : '配置插件'"
    direction="rtl"
    size="500px"
    :close-on-click-modal="false"
  >
    <template v-if="plugin">
      <div class="plugin-info">
        <div class="plugin-desc">{{ plugin.description }}</div>
        <el-tag size="small">{{ plugin.pluginType }}</el-tag>
      </div>

      <el-divider />

      <el-form :model="formData" ref="formRef" label-position="top" v-if="schema && schema.properties">
        <div class="form-section">
          <div class="section-title">插件配置</div>
          <SchemaField
            v-for="(prop, key) in schema.properties"
            :key="key"
            :schema="prop"
            :model-value="formData[key]"
            @update:model-value="updateField(String(key), $event)"
            :title="prop.title || String(key)"
            :is-required="schema.required?.includes(String(key))"
            :prop-path="String(key)"
          />
        </div>

        <el-divider />

        <div class="form-section">
          <div class="section-title">高级配置</div>
          <el-form-item label="自定义优先级">
            <div class="priority-config">
              <el-checkbox v-model="priorityEnabled" label="覆盖默认优先级" />
              <el-input-number
                v-if="priorityEnabled"
                v-model="priorityValue"
                :min="1"
                :max="99999"
                style="width: 150px"
              />
              <span class="default-priority" v-if="!priorityEnabled">
                默认: {{ plugin.defaultPriority }}
              </span>
            </div>
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="pluginEnabled" active-text="启用此插件" />
          </el-form-item>
        </div>
      </el-form>

      <div v-if="!schema || !schema.properties" class="no-schema-tip">
        <el-divider />
        <el-form label-position="top">
          <el-form-item label="自定义优先级">
            <div class="priority-config">
              <el-checkbox v-model="priorityEnabled" label="覆盖默认优先级" />
              <el-input-number
                v-if="priorityEnabled"
                v-model="priorityValue"
                :min="1"
                :max="99999"
                style="width: 150px"
              />
              <span class="default-priority" v-if="!priorityEnabled">
                默认: {{ plugin.defaultPriority }}
              </span>
            </div>
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="pluginEnabled" active-text="启用此插件" />
          </el-form-item>
        </el-form>
      </div>
    </template>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="handleSave">应用配置</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { FormInstance } from 'element-plus'
import type { PluginInfo } from '@/api/plugins'
import SchemaField from './SchemaField.vue'

const props = defineProps<{
  modelValue: boolean
  plugin: PluginInfo | null
  config: Record<string, any>
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'save': [config: Record<string, any>, priorityOverride?: number]
}>()

const formRef = ref<FormInstance>()
const formData = ref<Record<string, any>>({})
const priorityEnabled = ref(false)
const priorityValue = ref(0)
const pluginEnabled = ref(true)

const schema = computed(() => props.plugin?.schema as any)

watch(() => props.config, (val) => {
  formData.value = { ...val }
}, { immediate: true, deep: true })

watch(() => props.plugin, (plugin) => {
  if (plugin) {
    priorityEnabled.value = false
    priorityValue.value = plugin.defaultPriority
    pluginEnabled.value = true
  }
})

function updateField(key: string, value: any) {
  formData.value = { ...formData.value, [key]: value }
}

function handleSave() {
  emit('save', formData.value, priorityEnabled.value ? priorityValue.value : undefined)
}
</script>

<style lang="scss" scoped>
.plugin-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.plugin-desc {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  flex: 1;
  margin-right: 12px;
}

.form-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}

.priority-config {
  display: flex;
  align-items: center;
  gap: 12px;
}

.default-priority {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.no-schema-tip {
  padding-top: 8px;
}

:deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

:deep(.el-drawer__body) {
  padding: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
