<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="mode === 'view' ? '查看路由' : (isEdit ? '编辑路由' : '新增路由')"
    width="900px"
    :close-on-click-modal="false"
    class="route-form-dialog"
    @close="handleClose"
  >
    <div class="dialog-content">
      <StepNavigation
        :current-step="currentStep"
        :completed-steps="isViewMode ? [0, 1, 2, 3] : Array.from(completedSteps)"
        :view-mode="isViewMode"
        @update:current-step="handleStepChange"
      />
      <div class="form-content">
        <StepBasicInfo
          v-show="currentStep === 0"
          ref="stepBasicInfoRef"
          v-model="formData"
          :is-edit="isEdit"
          :readonly="isViewMode"
        />
        <StepRouteMatching
          v-show="currentStep === 1"
          ref="stepRouteMatchingRef"
          v-model="formData"
          :readonly="isViewMode"
        />
        <StepTargetService
          v-show="currentStep === 2"
          ref="stepTargetServiceRef"
          v-model="formData"
          :readonly="isViewMode"
        />
        <StepPlugins
          v-show="currentStep === 3"
          ref="stepPluginsRef"
          v-model="formData"
          :readonly="isViewMode"
        />
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
        <template v-if="!isViewMode">
          <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
          <el-button v-if="currentStep < 3" type="primary" @click="nextStep">下一步</el-button>
          <el-button v-if="currentStep === 3" type="primary" :loading="loading" @click="handleSave">
            保存
          </el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { Route, RouteFormState, RouteCreateRequest, RouteUpdateRequest } from '@/api/routes'
import { routesApi, getDefaultFormState } from '@/api/routes'
import StepNavigation from './StepNavigation.vue'
import StepBasicInfo from './StepBasicInfo.vue'
import StepRouteMatching from './StepRouteMatching.vue'
import StepTargetService from './StepTargetService.vue'
import StepPlugins from './StepPlugins.vue'

const props = defineProps<{
  modelValue: boolean
  route?: Route | null
  mode?: 'create' | 'edit' | 'view'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const currentStep = ref(0)
const loading = ref(false)
const formData = ref<RouteFormState>(getDefaultFormState())
const completedSteps = ref<Set<number>>(new Set())

const stepBasicInfoRef = ref<InstanceType<typeof StepBasicInfo>>()
const stepRouteMatchingRef = ref<InstanceType<typeof StepRouteMatching>>()
const stepTargetServiceRef = ref<InstanceType<typeof StepTargetService>>()
const stepPluginsRef = ref<InstanceType<typeof StepPlugins>>()

const isEdit = computed(() => !!props.route?.id)
const isViewMode = computed(() => props.mode === 'view')

watch(() => props.modelValue, (val) => {
  if (val) {
    if (props.route) {
      loadRouteData(props.route)
    } else {
      formData.value = getDefaultFormState()
    }
    currentStep.value = 0
    completedSteps.value = new Set()
  }
})

function loadRouteData(route: Route) {
  formData.value = {
    routeId: route.routeId,
    routeName: route.routeName,
    description: route.description || '',
    order: route.order,
    enabled: route.enabled,
    pathPattern: '',
    matchType: 'ANT',
    ignoreCase: false,
    methods: [],
    headers: [],
    hosts: [],
    queries: [],
    serviceName: route.uri?.replace('lb://', '') || '',
    loadBalanceStrategy: (route.loadBalanceStrategy as any) || 'ROUND_ROBIN',
    pathRewriteEnabled: false,
    pathRewriteFrom: '',
    pathRewriteTo: '',
    connectTimeout: 5000,
    responseTimeout: 30000,
    plugins: []
  }
  
  if (route.plugins && route.plugins.length > 0) {
    for (const plugin of route.plugins) {
      if (plugin.pluginName === 'timeout') {
        formData.value.connectTimeout = plugin.config?.connectTimeout || 5000
        formData.value.responseTimeout = plugin.config?.responseTimeout || 30000
      } else if (plugin.pluginName === 'request-rewrite') {
        if (plugin.config?.pathRegex) {
          formData.value.pathRewriteEnabled = true
          formData.value.pathRewriteFrom = plugin.config.pathRegex
          formData.value.pathRewriteTo = plugin.config.pathReplacement || ''
        }
      } else {
        formData.value.plugins.push({
          pluginId: plugin.pluginId,
          pluginName: plugin.pluginName,
          pluginType: plugin.pluginType,
          config: plugin.config,
          priorityOverride: plugin.priorityOverride,
          enabled: plugin.enabled
        })
      }
    }
  }
  
  if (route.predicates && route.predicates.length > 0) {
    for (const pred of route.predicates) {
      const config = pred.args || pred.config || {}
      if (pred.predicateType === 'PATH' || pred.predicateName === 'Path') {
        formData.value.pathPattern = config.pattern || ''
        formData.value.matchType = config.matchType || 'ANT'
        formData.value.ignoreCase = config.ignoreCase || false
      } else if (pred.predicateType === 'METHOD' || pred.predicateName === 'Method') {
        formData.value.methods = config.methods || []
      } else if (pred.predicateType === 'HEADER' || pred.predicateName === 'Header') {
        formData.value.headers = config.headers || []
      } else if (pred.predicateType === 'HOST' || pred.predicateName === 'Host') {
        formData.value.hosts = config.hosts || []
      } else if (pred.predicateType === 'QUERY' || pred.predicateName === 'Query') {
        formData.value.queries = config.queries || []
      }
    }
  }
}

async function validateCurrentStep(): Promise<boolean> {
  switch (currentStep.value) {
    case 0:
      return await stepBasicInfoRef.value?.validate() || false
    case 1:
      return await stepRouteMatchingRef.value?.validate() || false
    case 2:
      return await stepTargetServiceRef.value?.validate() || false
    case 3:
      return await stepPluginsRef.value?.validate() || false
    default:
      return true
  }
}

async function nextStep() {
  const valid = await validateCurrentStep()
  if (valid) {
    completedSteps.value.add(currentStep.value)
    currentStep.value++
  } else {
    ElMessage.warning('请完成必填项')
  }
}

function prevStep() {
  currentStep.value--
}

function handleStepChange(step: number) {
  if (isViewMode.value) {
    currentStep.value = step
  } else if (step <= currentStep.value || completedSteps.value.has(step - 1)) {
    currentStep.value = step
  }
}

function buildSubmitData(): RouteCreateRequest | RouteUpdateRequest {
  const data: any = {
    routeName: formData.value.routeName,
    description: formData.value.description,
    uri: `lb://${formData.value.serviceName}`,
    order: formData.value.order,
    enabled: formData.value.enabled,
    matching: {
      path: {
        pattern: formData.value.pathPattern,
        matchType: formData.value.matchType,
        ignoreCase: formData.value.ignoreCase
      }
    },
    loadBalanceStrategy: formData.value.loadBalanceStrategy,
    plugins: formData.value.plugins.map(p => ({
      pluginId: p.pluginId,
      config: p.config,
      priorityOverride: p.priorityOverride,
      enabled: p.enabled
    }))
  }

  if (formData.value.methods.length > 0) {
    data.matching.methods = formData.value.methods
  }
  if (formData.value.headers.length > 0) {
    data.matching.headers = formData.value.headers
  }
  if (formData.value.hosts.length > 0) {
    data.matching.hosts = formData.value.hosts.filter(h => h)
  }
  if (formData.value.queries.length > 0) {
    data.matching.queries = formData.value.queries
  }

  if (formData.value.pathRewriteEnabled && formData.value.pathRewriteFrom) {
    data.pathRewrite = {
      from: formData.value.pathRewriteFrom,
      to: formData.value.pathRewriteTo
    }
  }

  if (formData.value.connectTimeout || formData.value.responseTimeout) {
    data.timeouts = {
      connect: formData.value.connectTimeout,
      response: formData.value.responseTimeout
    }
  }

  if (!isEdit.value) {
    data.routeId = formData.value.routeId
  }

  return data
}

async function handleSave() {
  const step1Valid = await stepBasicInfoRef.value?.validate()
  const step2Valid = await stepRouteMatchingRef.value?.validate()
  const step3Valid = await stepTargetServiceRef.value?.validate()
  
  if (!step1Valid || !step2Valid || !step3Valid) {
    ElMessage.warning('请完成必填项')
    return
  }

  loading.value = true
  try {
    const data = buildSubmitData()
    
    console.log('=== 提交的路由数据 ===')
    console.log('plugins:', data.plugins)
    console.log('timeouts:', data.timeouts)
    console.log('pathRewrite:', data.pathRewrite)
    console.log('===================')
    
    if (isEdit.value && props.route?.id) {
      await routesApi.update(props.route.id, data as RouteUpdateRequest)
      ElMessage.success('更新成功')
    } else {
      await routesApi.create(data as RouteCreateRequest)
      ElMessage.success('创建成功')
    }
    
    emit('update:modelValue', false)
    emit('success')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

function handleClose() {
  currentStep.value = 0
  completedSteps.value = new Set()
}
</script>

<style lang="scss" scoped>
.route-form-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.dialog-content {
  display: flex;
  min-height: 420px;
  max-height: 70vh;
}

.form-content {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>