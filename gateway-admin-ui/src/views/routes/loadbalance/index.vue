<template>
  <div class="load-balance-management">
    <div class="page-header">
      <div class="header-left">
        <h1>负载均衡配置</h1>
        <p>管理各路由的负载均衡策略配置</p>
      </div>
    </div>

    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" label-width="80px">
        <el-form-item label="路由名称">
          <el-input 
            v-model="searchForm.routeName" 
            placeholder="请输入路由名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="策略">
          <el-select v-model="searchForm.strategy" placeholder="请选择策略" clearable style="width: 150px">
            <el-option 
              v-for="s in strategies" 
              :key="s.code" 
              :label="s.name" 
              :value="s.code" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.enabled" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <div class="table-header">
        <div class="table-info">
          共 {{ total }} 条记录
        </div>
      </div>

      <el-table 
        :data="loadBalanceList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="routeName" label="路由名称" min-width="160">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleGoToRoute(row)">
              {{ row.routeName || '-' }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="strategy" label="策略" width="140">
          <template #default="{ row }">
            <el-tag :type="getStrategyTagType(row.strategy)">
              {{ getStrategyName(row.strategy) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="strategyDesc" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="配置" width="100">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleViewConfig(row)"
            >
              查看配置
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-popconfirm
              title="确定要删除该配置吗？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button 
                  type="danger" 
                  size="small" 
                  link
                >
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑负载均衡配置' : '新增负载均衡配置'"
      width="600px"
      :close-on-click-modal="false"
      @close="handleCloseDialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="路由" prop="routeId" v-if="!isEdit">
          <el-select 
            v-model="form.routeId" 
            placeholder="请选择路由"
            filterable
            style="width: 100%"
          >
            <el-option 
              v-for="route in availableRoutes" 
              :key="route.id" 
              :label="route.routeName" 
              :value="route.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="路由" v-else>
          <el-input :value="currentRouteName" disabled />
        </el-form-item>

        <el-form-item label="策略" prop="strategy">
          <el-select 
            v-model="form.strategy" 
            placeholder="请选择策略"
            @change="handleStrategyChange"
            style="width: 100%"
          >
            <el-option 
              v-for="s in strategies" 
              :key="s.code" 
              :label="s.name" 
              :value="s.code"
            >
              <span>{{ s.name }}</span>
              <span style="color: var(--el-text-color-secondary); font-size: 12px; margin-left: 8px">
                {{ s.description }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="配置参数" v-if="currentConfigFields.length > 0">
          <div class="config-container">
            <div v-for="field in currentConfigFields" :key="field.field" class="config-item">
              <label>{{ field.label }}</label>
              <div class="config-input">
                <el-input-number
                  v-if="field.type === 'number'"
                  v-model="form.config[field.field]"
                  :placeholder="field.placeholder"
                  style="width: 100%"
                />
                <el-input
                  v-else
                  v-model="form.config[field.field]"
                  :placeholder="field.placeholder"
                />
              </div>
              <div v-if="field.description" class="config-desc">
                {{ field.description }}
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="configDialogVisible"
      title="负载均衡配置"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="路由名称">
          {{ currentLoadBalance?.routeName }}
        </el-descriptions-item>
        <el-descriptions-item label="策略">
          <el-tag>{{ getStrategyName(currentLoadBalance?.strategy) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="配置参数">
          <pre class="config-json-display">{{ formatConfig(currentLoadBalance?.config) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { loadBalanceApi, type LoadBalance, type LoadBalanceQueryParams, type LoadBalanceStrategy } from '@/api/loadbalance'
import { routesApi } from '@/api/routes'

const router = useRouter()

const loading = ref(false)
const formLoading = ref(false)
const loadBalanceList = ref<LoadBalance[]>([])
const total = ref(0)
const strategies = ref<LoadBalanceStrategy[]>([])
const availableRoutes = ref<any[]>([])

const formDialogVisible = ref(false)
const configDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const currentLoadBalance = ref<LoadBalance>()

const searchForm = reactive({
  routeName: '',
  strategy: '',
  enabled: undefined as boolean | undefined
})

const form = reactive({
  id: undefined as number | undefined,
  routeId: undefined as number | undefined,
  strategy: '',
  config: {} as Record<string, any>
})

const pagination = reactive({
  page: 1,
  size: 20
})

const isEdit = computed(() => !!form.id)

const currentRouteName = computed(() => {
  if (form.routeId) {
    const route = availableRoutes.value.find(r => r.id === form.routeId)
    return route?.routeName || ''
  }
  return ''
})

const currentConfigFields = computed(() => {
  if (!form.strategy) return []
  const strategy = strategies.value.find(s => s.code === form.strategy)
  return strategy?.configFields || []
})

const rules: FormRules = {
  routeId: [
    { required: true, message: '请选择路由', trigger: 'change' }
  ],
  strategy: [
    { required: true, message: '请选择策略', trigger: 'change' }
  ]
}

const loadLoadBalanceList = async () => {
  try {
    loading.value = true
    const queryParams: LoadBalanceQueryParams = {
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    delete (queryParams as any).routeName
    
    const response = await loadBalanceApi.list(queryParams)
    if (response && response.data) {
      const responseData = response.data
      if (responseData.data && Array.isArray(responseData.data.data)) {
        loadBalanceList.value = responseData.data.data
        total.value = responseData.data.total
      } else if (Array.isArray(responseData.data)) {
        loadBalanceList.value = responseData.data
        total.value = responseData.data.length
      } else {
        loadBalanceList.value = []
        total.value = 0
      }
    }
  } catch (error) {
    ElMessage.error('加载负载均衡列表失败：' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

const loadStrategies = async () => {
  try {
    const response = await loadBalanceApi.getStrategies()
    if (response && response.data) {
      strategies.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载策略列表失败')
  }
}

const loadAvailableRoutes = async () => {
  try {
    const response = await routesApi.list({ pageNum: 1, pageSize: 1000 })
    if (response && response.data) {
      const responseData = response.data
      if (responseData.data && Array.isArray(responseData.data.data)) {
        availableRoutes.value = responseData.data.data.filter((r: any) => r.enabled)
      } else if (Array.isArray(responseData.data)) {
        availableRoutes.value = responseData.data.filter((r: any) => r.enabled)
      }
    }
  } catch (error) {
    console.error('加载路由列表失败', error)
  }
}

const getStrategyTagType = (strategy: string) => {
  const typeMap: Record<string, string> = {
    'ROUND_ROBIN': 'primary',
    'RANDOM': 'success',
    'WEIGHTED_ROUND_ROBIN': 'warning',
    'LEAST_CONNECTIONS': 'info'
  }
  return typeMap[strategy] || ''
}

const getStrategyName = (strategy?: string) => {
  if (!strategy) return ''
  const s = strategies.value.find(item => item.code === strategy)
  return s?.name || strategy
}

const handleSearch = () => {
  pagination.page = 1
  loadLoadBalanceList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    routeName: '',
    strategy: '',
    enabled: undefined
  })
  handleSearch()
}

const handleGoToRoute = (row: LoadBalance) => {
  router.push(`/routes/list?id=${row.routeId}`)
}

const handleEdit = (lb: LoadBalance) => {
  Object.assign(form, {
    id: lb.id,
    routeId: lb.routeId,
    strategy: lb.strategy,
    config: { ...lb.config } || {}
  })
  formDialogVisible.value = true
}

const handleDelete = async (lb: LoadBalance) => {
  try {
    await loadBalanceApi.delete(lb.id)
    ElMessage.success('删除成功')
    loadLoadBalanceList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleStatusChange = async (lb: LoadBalance) => {
  try {
    if (lb.enabled) {
      await loadBalanceApi.enable(lb.id)
      ElMessage.success('启用成功')
    } else {
      await loadBalanceApi.disable(lb.id)
      ElMessage.success('禁用成功')
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    lb.enabled = !lb.enabled
  }
}

const handleViewConfig = (lb: LoadBalance) => {
  currentLoadBalance.value = lb
  configDialogVisible.value = true
}

const handleStrategyChange = () => {
  form.config = {}
  for (const field of currentConfigFields.value) {
    if (field.defaultValue !== undefined) {
      form.config[field.field] = field.defaultValue
    }
  }
}

const handleSizeChange = () => {
  pagination.page = 1
  loadLoadBalanceList()
}

const handleCurrentChange = () => {
  loadLoadBalanceList()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      strategy: form.strategy,
      config: form.config
    }

    if (isEdit.value && form.id) {
      await loadBalanceApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await loadBalanceApi.create({
        ...submitData,
        routeId: form.routeId!
      })
      ElMessage.success('创建成功')
    }

    handleCloseDialog()
    loadLoadBalanceList()
  } catch (error) {
    ElMessage.error('操作失败：' + (error as Error).message)
  } finally {
    formLoading.value = false
  }
}

const handleCloseDialog = () => {
  formRef.value?.resetFields()
  formDialogVisible.value = false
}

const formatConfig = (config?: Record<string, any>) => {
  if (!config) return '{}'
  return JSON.stringify(config, null, 2)
}

onMounted(() => {
  loadStrategies()
  loadAvailableRoutes()
  loadLoadBalanceList()
})
</script>

<style lang="scss" scoped>
.load-balance-management {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
    
    .header-left {
      h1 {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 600;
      }
      
      p {
        margin: 0;
        color: var(--text-secondary);
        font-size: 14px;
      }
    }
  }
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .table-card {
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .table-info {
        color: var(--text-secondary);
        font-size: 14px;
      }
    }
    
    .pagination-wrapper {
      margin-top: 20px;
      text-align: right;
    }
  }

  .config-container {
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    padding: 16px;
    background-color: var(--el-bg-color-page);
    width: 100%;

    .config-item {
      margin-bottom: 16px;

      label {
        display: block;
        margin-bottom: 8px;
        font-weight: 500;
        font-size: 14px;
      }

      .config-input {
        width: 100%;
      }

      .config-desc {
        margin-top: 4px;
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .config-json-display {
    background-color: var(--el-bg-color-page);
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    padding: 12px;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 12px;
    line-height: 1.5;
    max-height: 300px;
    overflow-y: auto;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>