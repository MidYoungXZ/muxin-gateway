<template>
  <div class="step-target-service">
    <div class="section-title">
      服务选择
      <span class="required-mark">*</span>
    </div>
    <el-form :model="modelValue" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="目标服务" prop="serviceName">
        <el-select
          :model-value="modelValue.serviceName"
          @update:model-value="updateField('serviceName', $event)"
          placeholder="请选择目标服务"
          filterable
          style="width: 100%"
          :disabled="readonly"
        >
          <el-option
            v-for="name in serviceNames"
            :key="name"
            :label="name"
            :value="name"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <div class="section-title">
      负载均衡配置
      <span class="required-mark">*</span>
    </div>
    <el-form :model="modelValue" label-position="top">
      <el-form-item label="负载均衡策略">
        <el-select
          :model-value="modelValue.loadBalanceStrategy"
          @update:model-value="updateField('loadBalanceStrategy', $event)"
          style="width: 100%"
          :disabled="readonly"
        >
          <el-option
            v-for="item in LOAD_BALANCE_STRATEGIES"
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
    </el-form>

    <div class="section-title">服务节点</div>
    <div class="node-preview" v-if="modelValue.serviceName">
      <div class="node-header">
        <span>{{ modelValue.serviceName }} 节点列表</span>
        <el-tag size="small">共 {{ serviceNodes.length }} 个节点</el-tag>
      </div>
      <el-table :data="serviceNodes" size="small" v-if="serviceNodes.length > 0">
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="weight" label="权重" width="80" />
      </el-table>
      <el-empty v-else description="暂无可用节点" :image-size="60" />
      <div class="node-link">
        <el-button type="primary" link @click="goToNodes">
          在服务管理中配置节点 →
        </el-button>
      </div>
    </div>
    <el-empty v-else description="请先选择目标服务" :image-size="60" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import type { RouteFormState } from '@/api/routes'
import { routesApi, LOAD_BALANCE_STRATEGIES } from '@/api/routes'
import request from '@/utils/request'

const props = defineProps<{
  modelValue: RouteFormState
  readonly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: RouteFormState]
}>()

const router = useRouter()
const formRef = ref<FormInstance>()
const serviceNames = ref<string[]>([])
const allServiceNodes = ref<Record<string, any[]>>({})

const rules: FormRules = {
  serviceName: [
    { required: true, message: '请选择目标服务', trigger: 'change' }
  ]
}

const serviceNodes = computed(() => {
  if (!props.modelValue.serviceName) return []
  return allServiceNodes.value[props.modelValue.serviceName] || []
})

function updateField(field: keyof RouteFormState, value: any) {
  emit('update:modelValue', { ...props.modelValue, [field]: value })
}

async function loadServiceNames() {
  try {
    const res = await routesApi.getServiceNames()
    if (res?.data) {
      serviceNames.value = res.data
    }
  } catch (error) {
    console.error('加载服务名称失败', error)
  }
}

async function loadServiceNodes(serviceName: string) {
  if (!serviceName || allServiceNodes.value[serviceName]) return
  try {
    const res = await request({
      url: `/api/nodes/services/${serviceName}/nodes`,
      method: 'get',
      params: { pageNum: 1, pageSize: 100 }
    })
    if (res?.data?.data) {
      allServiceNodes.value[serviceName] = res.data.data.map((node: any) => ({
        address: node.address || `${node.host}:${node.port}`,
        weight: node.weight || 1
      }))
    }
  } catch (error) {
    console.error('加载服务节点失败', error)
  }
}

function goToNodes() {
  router.push('/routes/nodes')
}

watch(() => props.modelValue.serviceName, (name) => {
  if (name) loadServiceNodes(name)
})

onMounted(() => {
  loadServiceNames()
})

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
.step-target-service {
  padding: 0 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--border-primary);
}

.required-mark {
  color: var(--el-color-danger);
  margin-left: 4px;
}

.field-tip {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 2px;
  line-height: 1.4;
}

.node-preview {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
  border: 1px solid var(--border-primary);
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.node-link {
  margin-top: 8px;
}

:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  padding-bottom: 2px;
}
</style>
