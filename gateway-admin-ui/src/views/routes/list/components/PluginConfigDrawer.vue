<template>
  <el-drawer
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="plugin ? `配置插件: ${plugin.pluginName}` : '配置插件'"
    direction="rtl"
    size="480px"
    :close-on-click-modal="false"
  >
    <template v-if="plugin">
      <div class="plugin-info">
        <div class="plugin-desc">{{ plugin.description }}</div>
        <el-tag size="small">{{ plugin.pluginType }}</el-tag>
      </div>

      <el-divider />

      <el-form :model="formData" ref="formRef" label-position="top" v-if="schema">
        <div class="form-section">
          <div class="section-title">基础配置</div>
          <template v-for="(prop, key) in schema.properties" :key="key">
            <el-form-item
              :label="prop.title || key"
              :required="schema.required?.includes(key)"
            >
              <template v-if="prop.type === 'string'">
                <template v-if="prop.enum">
                  <el-select v-model="formData[key]" style="width: 100%">
                    <el-option
                      v-for="opt in prop.enum"
                      :key="opt"
                      :label="prop.enumTitles?.[opt] || opt"
                      :value="opt"
                    />
                  </el-select>
                </template>
                <template v-else-if="prop.format === 'password'">
                  <el-input
                    v-model="formData[key]"
                    type="password"
                    show-password
                    :placeholder="`请输入${prop.title || key}`"
                  />
                </template>
                <template v-else>
                  <el-input
                    v-model="formData[key]"
                    :placeholder="`请输入${prop.title || key}`"
                  />
                </template>
              </template>
              <template v-else-if="prop.type === 'number' || prop.type === 'integer'">
                <el-input-number
                  v-model="formData[key]"
                  :min="prop.minimum"
                  :max="prop.maximum"
                  :step="prop.type === 'integer' ? 1 : 0.1"
                  style="width: 100%"
                />
              </template>
              <template v-else-if="prop.type === 'boolean'">
                <el-switch v-model="formData[key]" />
              </template>
              <template v-else-if="prop.type === 'array' && prop.items?.type === 'string'">
                <div class="dynamic-tags">
                  <el-tag
                    v-for="(tag, index) in (formData[key] || [])"
                    :key="index"
                    closable
                    @close="removeArrayItem(key, index)"
                    style="margin-right: 8px; margin-bottom: 8px"
                  >
                    {{ tag }}
                  </el-tag>
                  <el-input
                    v-model="arrayInput[key]"
                    :placeholder="`添加${prop.title || key}`"
                    size="small"
                    style="width: 150px"
                    @keyup.enter="addArrayItem(key)"
                  >
                    <template #append>
                      <el-button @click="addArrayItem(key)">添加</el-button>
                    </template>
                  </el-input>
                </div>
              </template>
              <div class="field-desc" v-if="prop.description">{{ prop.description }}</div>
            </el-form-item>
          </template>
        </div>

        <el-divider />

        <div class="form-section">
          <div class="section-title">高级配置</div>
          <el-form-item label="自定义优先级">
            <div class="priority-config">
              <el-checkbox
                v-model="priorityEnabled"
                label="覆盖默认优先级"
              />
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
import type { PluginInfo } from '@/api/routes'

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
const arrayInput = ref<Record<string, string>>({})
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

function addArrayItem(key: string) {
  const value = arrayInput.value[key]?.trim()
  if (!value) return
  if (!formData.value[key]) formData.value[key] = []
  formData.value[key].push(value)
  arrayInput.value[key] = ''
}

function removeArrayItem(key: string, index: number) {
  formData.value[key].splice(index, 1)
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

.field-desc {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-top: 4px;
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

.dynamic-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
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