<template>
  <div class="step-route-matching">
    <div class="section-title">
      路径匹配
      <span class="required-mark">*</span>
    </div>
    <el-form :model="modelValue" :rules="rules" ref="formRef" label-position="top">
      <el-row :gutter="24">
        <el-col :span="14">
          <el-form-item label="路径模式" prop="pathPattern">
            <el-input
              :model-value="modelValue.pathPattern"
              @update:model-value="updateField('pathPattern', $event)"
              placeholder="请输入路径模式，如 /api/v1/**"
              :disabled="readonly"
            />
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="匹配类型">
            <el-select
              :model-value="modelValue.matchType"
              @update:model-value="updateField('matchType', $event)"
              style="width: 100%"
              :disabled="readonly"
            >
              <el-option
                v-for="item in MATCH_TYPES"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
                <div>
                  <div>{{ item.label }}</div>
                  <div style="font-size: 12px; color: var(--text-tertiary)">
                    {{ item.description }}
                  </div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-checkbox
          :model-value="modelValue.ignoreCase"
          @update:model-value="updateField('ignoreCase', $event)"
          :disabled="readonly"
        >
          忽略大小写
        </el-checkbox>
      </el-form-item>
    </el-form>

    <div class="section-title">方法匹配（可选）</div>
    <div class="field-tip" style="margin-bottom: 12px">限制请求方法，不选择则匹配所有方法</div>
    <el-form :model="modelValue" label-position="top">
      <el-form-item>
        <div class="method-checkboxes">
          <el-checkbox-group
            :model-value="modelValue.methods"
            @update:model-value="updateField('methods', $event)"
            :disabled="readonly"
          >
            <el-checkbox-button
              v-for="method in HTTP_METHODS"
              :key="method"
              :label="method"
            >
              {{ method }}
            </el-checkbox-button>
          </el-checkbox-group>
        </div>
      </el-form-item>
    </el-form>

    <div class="section-header">
      <div class="section-title">Header匹配（可选）</div>
      <el-button v-if="!readonly" type="primary" link @click="addHeader">
        <el-icon><Plus /></el-icon> 添加
      </el-button>
    </div>
    <el-form :model="modelValue" label-position="top">
      <div class="match-list">
        <div
          v-for="(header, index) in modelValue.headers"
          :key="index"
          class="match-row"
        >
          <el-input
            v-model="header.name"
            placeholder="Header名称"
            style="width: 160px"
            :disabled="readonly"
          />
          <el-input
            v-model="header.value"
            placeholder="匹配值"
            style="width: 160px"
            :disabled="readonly"
          />
          <el-select v-model="header.matchType" style="width: 120px" :disabled="readonly">
            <el-option
              v-for="item in HEADER_MATCH_TYPES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-button v-if="!readonly" type="danger" link @click="removeHeader(index)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </el-form>

    <div class="section-header">
      <div class="section-title">Host匹配（可选）</div>
      <el-button v-if="!readonly" type="primary" link @click="addHost">
        <el-icon><Plus /></el-icon> 添加
      </el-button>
    </div>
    <el-form :model="modelValue" label-position="top">
      <div class="host-list">
        <div
          v-for="(host, index) in modelValue.hosts"
          :key="index"
          class="host-row"
        >
          <el-input
            :model-value="host"
            @update:model-value="updateHost(index, $event)"
            placeholder="如 api.example.com"
            style="flex: 1"
            :disabled="readonly"
          />
          <el-button v-if="!readonly" type="danger" link @click="removeHost(index)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
      <div class="field-tip">支持通配符 *，如 *.example.com 匹配所有子域名</div>
    </el-form>

    <div class="section-header">
      <div class="section-title">Query参数匹配（可选）</div>
      <el-button v-if="!readonly" type="primary" link @click="addQuery">
        <el-icon><Plus /></el-icon> 添加
      </el-button>
    </div>
    <el-form :model="modelValue" label-position="top">
      <div class="match-list">
        <div
          v-for="(query, index) in modelValue.queries"
          :key="index"
          class="match-row"
        >
          <el-input
            v-model="query.name"
            placeholder="参数名称"
            style="width: 160px"
            :disabled="readonly"
          />
          <el-input
            v-model="query.value"
            placeholder="匹配值"
            style="width: 160px"
            :disabled="readonly"
          />
          <el-select v-model="query.matchType" style="width: 120px" :disabled="readonly">
            <el-option
              v-for="item in HEADER_MATCH_TYPES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-button v-if="!readonly" type="danger" link @click="removeQuery(index)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </el-form>

    <div class="match-preview" v-if="hasMatchingRules">
      <div class="preview-title">匹配规则预览</div>
      <div class="preview-content">
        <div class="preview-tip">当请求满足以下所有条件时，匹配此路由：</div>
        <div class="preview-rules">
          <div class="rule-item">
            <el-icon class="rule-icon"><Check /></el-icon>
            <span>路径: {{ modelValue.pathPattern }} ({{ getMatchTypeLabel(modelValue.matchType) }})</span>
          </div>
          <div class="rule-item" v-if="modelValue.methods.length > 0">
            <el-icon class="rule-icon"><Check /></el-icon>
            <span>方法: {{ modelValue.methods.join(' 或 ') }}</span>
          </div>
          <div class="rule-item" v-for="(header, index) in modelValue.headers" :key="'h' + index">
            <el-icon class="rule-icon"><Check /></el-icon>
            <span>Header: {{ header.name }} {{ getMatchTypeDesc(header.matchType, header.value) }}</span>
          </div>
          <div class="rule-item" v-for="(host, index) in modelValue.hosts" :key="'host' + index">
            <el-icon class="rule-icon"><Check /></el-icon>
            <span>Host: {{ host }}</span>
          </div>
          <div class="rule-item" v-for="(query, index) in modelValue.queries" :key="'q' + index">
            <el-icon class="rule-icon"><Check /></el-icon>
            <span>Query: {{ query.name }} {{ getMatchTypeDesc(query.matchType, query.value) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Delete, Plus, Check } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { RouteFormState, HeaderMatch, QueryMatch } from '@/api/routes'
import { MATCH_TYPES, HTTP_METHODS, HEADER_MATCH_TYPES } from '@/api/routes'

const props = defineProps<{
  modelValue: RouteFormState
  readonly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: RouteFormState]
}>()

const formRef = ref<FormInstance>()

const rules: FormRules = {
  pathPattern: [
    { required: true, message: '请输入路径模式', trigger: 'blur' }
  ]
}

const hasMatchingRules = computed(() => {
  return props.modelValue.pathPattern ||
    props.modelValue.methods.length > 0 ||
    props.modelValue.headers.length > 0 ||
    props.modelValue.hosts.length > 0 ||
    props.modelValue.queries.length > 0
})

function updateField(field: keyof RouteFormState, value: any) {
  emit('update:modelValue', { ...props.modelValue, [field]: value })
}

function addHeader() {
  const headers = [...props.modelValue.headers, { name: '', value: '', matchType: 'EXIST' as const }]
  updateField('headers', headers)
}

function removeHeader(index: number) {
  const headers = props.modelValue.headers.filter((_, i) => i !== index)
  updateField('headers', headers)
}

function addHost() {
  const hosts = [...props.modelValue.hosts, '']
  updateField('hosts', hosts)
}

function removeHost(index: number) {
  const hosts = props.modelValue.hosts.filter((_, i) => i !== index)
  updateField('hosts', hosts)
}

function updateHost(index: number, value: string) {
  const hosts = [...props.modelValue.hosts]
  hosts[index] = value
  updateField('hosts', hosts)
}

function addQuery() {
  const queries = [...props.modelValue.queries, { name: '', value: '', matchType: 'EXIST' as const }]
  updateField('queries', queries)
}

function removeQuery(index: number) {
  const queries = props.modelValue.queries.filter((_, i) => i !== index)
  updateField('queries', queries)
}

function getMatchTypeLabel(type: string): string {
  const item = MATCH_TYPES.find(t => t.value === type)
  return item ? item.label : type
}

function getMatchTypeDesc(matchType: string, value: string): string {
  switch (matchType) {
    case 'EXIST': return '存在'
    case 'NOT_EXIST': return '不存在'
    case 'EQUAL': return `等于 ${value}`
    case 'REGEX': return `匹配 ${value}`
    default: return ''
  }
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
.step-route-matching {
  padding: 0 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  padding-bottom: 6px;
  border-bottom: 1px solid var(--border-primary);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--border-primary);

  .section-title {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
  }
}

.required-mark {
  color: var(--el-color-danger);
  margin-left: 4px;
}

.field-tip {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
  line-height: 1.4;
}

.method-checkboxes {
  margin-bottom: 6px;

  :deep(.el-checkbox-button) {
    .el-checkbox-button__inner {
      background: var(--input-bg);
      color: var(--text-primary);
      border-color: var(--border-primary);
      transition: all 0.2s;
    }
    &.is-checked .el-checkbox-button__inner {
      background: var(--el-color-primary);
      color: #fff;
      border-color: var(--el-color-primary);
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.3);
    }
    &:hover .el-checkbox-button__inner {
      border-color: var(--el-color-primary-light-5);
    }
  }
}

.match-list, .host-list {
  margin-bottom: 8px;
}

.match-row, .host-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.match-preview {
  margin-top: 16px;
  background: var(--bg-tertiary);
  border-radius: 8px;
  padding: 12px;
  overflow: hidden;
}

.preview-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.preview-tip {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-bottom: 6px;
}

.preview-rules {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rule-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--text-primary);
  min-width: 0;

  span {
    overflow-wrap: break-word;
    word-break: break-word;
    min-width: 0;
  }
}

.rule-icon {
  color: var(--el-color-success);
  margin-right: 8px;
  flex-shrink: 0;
}

:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  padding-bottom: 2px;
}
</style>