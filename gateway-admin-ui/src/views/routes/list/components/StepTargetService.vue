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
        >
          <el-option
            v-for="item in LOAD_BALANCE_STRATEGIES"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
            <div>
              <div>{{ item.label }}</div>
              <div style="font-size: 12px; color: var(--el-text-color-secondary)">
                {{ item.description }}
              </div>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <div class="section-title">服务节点详情</div>
    <div class="node-preview" v-if="modelValue.serviceName">
      <div class="node-header">
        <span>{{ modelValue.serviceName }} 节点列表</span>
        <el-tag size="small">共 {{ serviceNodes.length }} 个节点</el-tag>
      </div>
      <el-table :data="serviceNodes" size="small" v-if="serviceNodes.length > 0">
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="weight" label="权重" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.healthy ? 'success' : 'danger'" size="small">
              {{ row.healthy ? '健康' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseTime" label="响应时间" width="100">
          <template #default="{ row }">
            {{ row.responseTime }}ms
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无可用节点" :image-size="60" />
      <div class="node-link">
        <el-button type="primary" link @click="goToNodes">
          在服务节点管理中配置节点 →
        </el-button>
      </div>
    </div>
    <el-empty v-else description="请先选择目标服务" :image-size="60" />

    <div class="section-title">转发设置（可选）</div>
    <el-form :model="modelValue" label-position="top">
      <el-form-item>
        <el-checkbox
          :model-value="modelValue.pathRewriteEnabled"
          @update:model-value="updateField('pathRewriteEnabled', $event)"
        >
          启用路径重写
        </el-checkbox>
      </el-form-item>
      <el-row :gutter="24" v-if="modelValue.pathRewriteEnabled">
        <el-col :span="12">
          <el-form-item label="原路径模式">
            <el-input
              :model-value="modelValue.pathRewriteFrom"
              @update:model-value="updateField('pathRewriteFrom', $event)"
              placeholder="/api/v1/**"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标路径">
            <el-input
              :model-value="modelValue.pathRewriteTo"
              @update:model-value="updateField('pathRewriteTo', $event)"
              placeholder="/v1/**"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="field-tip" v-if="modelValue.pathRewriteEnabled">
        示例: /api/v1/user/123 → /v1/user/123
      </div>
    </el-form>

    <el-form :model="modelValue" label-position="top">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="连接超时 (ms)">
            <el-input-number
              :model-value="modelValue.connectTimeout"
              @update:model-value="updateField('connectTimeout', $event)"
              :min="100"
              :max="60000"
              :step="1000"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="响应超时 (ms)">
            <el-input-number
              :model-value="modelValue.responseTimeout"
              @update:model-value="updateField('responseTimeout', $event)"
              :min="1000"
              :max="300000"
              :step="1000"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
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
      url: '/api/service-nodes',
      method: 'get',
      params: { serviceName }
    })
    if (res?.data?.data) {
      allServiceNodes.value[serviceName] = res.data.data.map((node: any) => ({
        address: `${node.host}:${node.port}`,
        weight: node.weight || 1,
        healthy: node.healthy !== false,
        responseTime: node.responseTime || 0
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
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.required-mark {
  color: var(--el-color-danger);
  margin-left: 4px;
}

.field-tip {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-top: 4px;
  line-height: 1.4;
}

.node-preview {
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
}

.node-link {
  margin-top: 12px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  padding-bottom: 4px;
}
</style>