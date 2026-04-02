<template>
  <div class="schema-field">
    <el-form-item
      :label="title"
      :required="isRequired"
      :prop="propPath"
    >
      <template v-if="schema.type === 'string'">
        <template v-if="schema.enum">
          <el-select :model-value="modelValue" style="width: 100%" @update:model-value="handleInput">
            <el-option
              v-for="opt in schema.enum"
              :key="opt"
              :label="schema.enumTitles?.[opt] || opt"
              :value="opt"
            />
          </el-select>
        </template>
        <template v-else-if="schema.format === 'password'">
          <el-input
            :model-value="modelValue"
            @update:model-value="handleInput"
            type="password"
            show-password
            :placeholder="placeholder"
          />
        </template>
        <template v-else>
          <el-input
            :model-value="modelValue"
            @update:model-value="handleInput"
            :placeholder="placeholder"
          />
        </template>
      </template>

      <template v-else-if="schema.type === 'number' || schema.type === 'integer'">
        <el-input-number
          :model-value="modelValue"
          @update:model-value="handleInput"
          :min="schema.minimum"
          :max="schema.maximum"
          :step="schema.type === 'integer' ? 1 : 0.1"
          style="width: 100%"
        />
      </template>

      <template v-else-if="schema.type === 'boolean'">
        <el-switch
          :model-value="modelValue"
          @update:model-value="handleInput"
        />
      </template>

      <template v-else-if="schema.type === 'array'">
        <div class="array-field">
          <template v-if="schema.items?.type === 'object'">
            <div v-for="(_, index) in (modelValue || [])" :key="index" class="array-item">
              <div class="array-item-header">
                <span class="array-item-index">#{{ index + 1 }}</span>
                <el-button type="danger" size="small" link @click="removeArrayItem(index)">
                  删除
                </el-button>
              </div>
              <div class="array-item-body">
                <SchemaField
                  v-for="(subProp, subKey) in schema.items.properties"
                  :key="subKey"
                  :schema="subProp"
                  :model-value="(modelValue[index] || {})[subKey]"
                  @update:model-value="updateArrayObjectField(index, subKey, $event)"
                  :title="subProp.title || subKey"
                  :is-required="schema.items.required?.includes(subKey)"
                  :prop-path="`${propPath}.${index}.${subKey}`"
                />
              </div>
            </div>
            <el-button type="primary" plain size="small" @click="addArrayObjectItem">
              <el-icon><Plus /></el-icon> 添加
            </el-button>
          </template>
          <template v-else>
            <div class="tag-list">
              <el-tag
                v-for="(item, index) in (modelValue || [])"
                :key="index"
                closable
                @close="removeArrayItem(index)"
                style="margin-right: 8px; margin-bottom: 8px"
              >
                {{ item }}
              </el-tag>
            </div>
            <div class="tag-input-row">
              <el-input
                v-model="arrayInput"
                :placeholder="`添加${title}`"
                size="small"
                style="width: 200px"
                @keyup.enter="addArrayItem"
              >
                <template #append>
                  <el-button @click="addArrayItem">添加</el-button>
                </template>
              </el-input>
            </div>
          </template>
        </div>
      </template>

      <template v-else-if="schema.type === 'object'">
        <div class="object-field">
          <template v-if="schema.properties && Object.keys(schema.properties).length > 0">
            <SchemaField
              v-for="(subProp, subKey) in schema.properties"
              :key="subKey"
              :schema="subProp"
              :model-value="(modelValue || {})[subKey]"
              @update:model-value="updateObjectField(subKey, $event)"
              :title="subProp.title || subKey"
              :is-required="schema.required?.includes(subKey)"
              :prop-path="`${propPath}.${subKey}`"
            />
          </template>
          <template v-else>
            <el-input
              :model-value="objectJsonStr"
              @update:model-value="handleObjectJsonInput"
              type="textarea"
              :rows="3"
              placeholder='{"key": "value"}'
            />
            <div v-if="jsonError" class="json-error">{{ jsonError }}</div>
          </template>
        </div>
      </template>

      <div class="field-desc" v-if="schema.description">{{ schema.description }}</div>
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'

interface JsonSchema {
  type?: string
  title?: string
  description?: string
  default?: any
  enum?: string[]
  enumTitles?: Record<string, string>
  format?: string
  minimum?: number
  maximum?: number
  properties?: Record<string, JsonSchema>
  required?: string[]
  items?: JsonSchema
  [key: string]: any
}

const props = defineProps<{
  schema: JsonSchema
  modelValue: any
  title: string
  isRequired?: boolean
  propPath?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: any]
}>()

const arrayInput = ref('')
const objectJsonStr = ref('')
const jsonError = ref('')

const placeholder = computed(() => `请输入${props.title}`)

function handleInput(val: any) {
  emit('update:modelValue', val)
}

function addArrayItem() {
  const val = arrayInput.value.trim()
  if (!val) return
  const arr = [...(props.modelValue || [])]
  arr.push(val)
  emit('update:modelValue', arr)
  arrayInput.value = ''
}

function removeArrayItem(index: number) {
  const arr = [...(props.modelValue || [])]
  arr.splice(index, 1)
  emit('update:modelValue', arr)
}

function addArrayObjectItem() {
  const arr = [...(props.modelValue || [])]
  const newObj: Record<string, any> = {}
  if (props.schema.items?.properties) {
    for (const [key, prop] of Object.entries(props.schema.items.properties)) {
      newObj[key] = prop.default ?? getDefaultForType(prop.type)
    }
  }
  arr.push(newObj)
  emit('update:modelValue', arr)
}

function updateArrayObjectField(index: number, field: string, value: any) {
  const arr = [...(props.modelValue || [])]
  arr[index] = { ...(arr[index] || {}), [field]: value }
  emit('update:modelValue', arr)
}

function updateObjectField(key: string, value: any) {
  const obj = { ...(props.modelValue || {}) }
  obj[key] = value
  emit('update:modelValue', obj)
}

function handleObjectJsonInput(val: string) {
  objectJsonStr.value = val
  jsonError.value = ''
  try {
    const parsed = JSON.parse(val)
    emit('update:modelValue', parsed)
  } catch {
    jsonError.value = 'JSON 格式不正确'
  }
}

function getDefaultForType(type?: string): any {
  switch (type) {
    case 'string': return ''
    case 'number':
    case 'integer': return 0
    case 'boolean': return false
    case 'array': return []
    case 'object': return {}
    default: return null
  }
}
</script>

<style lang="scss" scoped>
.field-desc {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.array-field {
  width: 100%;
}

.array-item {
  background: var(--bg-tertiary);
  border: 1px solid var(--border-primary);
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 8px;

  .array-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .array-item-index {
      font-size: 12px;
      font-weight: 600;
      color: var(--text-tertiary);
    }
  }

  .array-item-body {
    :deep(.el-form-item) {
      margin-bottom: 12px;
    }

    :deep(.el-form-item:last-child) {
      margin-bottom: 0;
    }
  }
}

.object-field {
  width: 100%;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-primary);
  border-radius: 6px;
  padding: 12px;

  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  :deep(.el-form-item:last-child) {
    margin-bottom: 0;
  }
}

.tag-list {
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
}

.tag-input-row {
  display: flex;
  align-items: center;
}

.json-error {
  font-size: 12px;
  color: var(--el-color-danger);
  margin-top: 4px;
}
</style>
