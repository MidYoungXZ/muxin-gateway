<template>
  <div class="page-list-container">
    <!-- 标题栏 -->
    <div class="page-title-bar">
      <span class="title">路由列表</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增路由
      </el-button>
    </div>

    <!-- 搜索区 -->
    <div class="search-bar">
      <el-input v-model="searchForm.routeId" placeholder="路由ID" clearable @keyup.enter="handleSearch" />
      <el-input v-model="searchForm.routeName" placeholder="路由名称" clearable @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.enabled" placeholder="状态" clearable>
        <el-option label="启用" :value="true" />
        <el-option label="禁用" :value="false" />
      </el-select>
      <div class="search-actions">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 表格区 -->
    <div class="table-wrapper">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="danger" :disabled="!selectedRoutes.length" @click="handleBatchDelete">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
        <span class="toolbar-right">共 {{ total }} 条</span>
      </div>

      <el-table :data="routeList" v-loading="loading" @selection-change="handleSelectionChange" stripe>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="routeId" label="路由ID" min-width="120" />
        <el-table-column prop="routeName" label="路由名称" min-width="120" />
        <el-table-column prop="uri" label="目标URI" min-width="160" show-overflow-tooltip />
        <el-table-column label="负载均衡" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getStrategyLabel(row.loadBalanceStrategy) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="order" label="优先级" width="70" />
        <el-table-column label="断言" width="60">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.predicates?.length || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="过滤器" width="60">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.filters?.length || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" size="small" link @click="handleCopy(row)">复制</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" size="small" link>删除</el-button>
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
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑路由' : '新增路由'" width="700px" :close-on-click-modal="false" @close="handleCloseDialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="路由ID" prop="routeId">
              <el-input v-model="form.routeId" placeholder="请输入路由ID" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由名称" prop="routeName">
              <el-input v-model="form.routeName" placeholder="请输入路由名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="目标服务" prop="serviceName">
              <el-select v-model="form.serviceName" placeholder="请选择目标服务" filterable style="width: 100%" @change="handleServiceChange">
                <el-option v-for="name in serviceNames" :key="name" :label="name" :value="name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="order">
              <el-input-number v-model="form.order" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="负载均衡" prop="loadBalanceStrategy">
              <el-select v-model="form.loadBalanceStrategy" placeholder="请选择负载均衡策略" style="width: 100%">
                <el-option v-for="s in loadBalanceStrategies" :key="s.value" :label="s.label" :value="s.value">
                  <span>{{ s.label }}</span>
                  <span style="color: var(--el-text-color-secondary); font-size: 12px; margin-left: 8px;">{{ s.description }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用状态">
              <el-switch v-model="form.enabled" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入路由描述" :rows="2" />
        </el-form-item>
        <el-form-item label="断言配置" prop="predicateIds">
          <el-select v-model="form.predicateIds" multiple placeholder="请选择断言" style="width: 100%">
            <el-option v-for="p in availablePredicates" :key="p.id" :label="`${p.predicateName} (${p.predicateTypeDesc || p.predicateType})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="过滤器配置" prop="filterIds">
          <el-select v-model="form.filterIds" multiple placeholder="请选择过滤器" style="width: 100%">
            <el-option v-for="f in availableFilters" :key="f.id" :label="`${f.filterName} (${f.filterType})`" :value="f.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="路由详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="路由ID">{{ currentRoute?.routeId }}</el-descriptions-item>
        <el-descriptions-item label="路由名称">{{ currentRoute?.routeName }}</el-descriptions-item>
        <el-descriptions-item label="目标URI">{{ currentRoute?.uri }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ currentRoute?.order }}</el-descriptions-item>
        <el-descriptions-item label="负载均衡">
          <el-tag>{{ getStrategyLabel(currentRoute?.loadBalanceStrategy) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRoute?.enabled ? 'success' : 'danger'">{{ currentRoute?.enabled ? '启用' : '禁用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentRoute?.version }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentRoute?.description || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="currentRoute?.predicates?.length" style="margin-top: 16px">
        <h4 style="margin: 0 0 8px; font-size: 14px">断言配置</h4>
        <el-table :data="currentRoute.predicates" stripe size="small">
          <el-table-column prop="predicateName" label="名称" />
          <el-table-column prop="predicateTypeDesc" label="类型" />
          <el-table-column label="配置">
            <template #default="{ row }">
              <pre class="config-json">{{ JSON.stringify(row.args, null, 2) }}</pre>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-if="currentRoute?.filters?.length" style="margin-top: 16px">
        <h4 style="margin: 0 0 8px; font-size: 14px">过滤器配置</h4>
        <el-table :data="currentRoute.filters" stripe size="small">
          <el-table-column prop="filterName" label="名称" />
          <el-table-column prop="filterType" label="类型" />
          <el-table-column label="配置">
            <template #default="{ row }">
              <pre class="config-json">{{ JSON.stringify(row.args, null, 2) }}</pre>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { routesApi, type Route, type RouteQueryParams, LOAD_BALANCE_STRATEGIES } from '@/api/routes'
import { predicatesApi, type Predicate } from '@/api/predicates'
import { filtersApi, type Filter } from '@/api/filters'

const loading = ref(false)
const formLoading = ref(false)
const routeList = ref<Route[]>([])
const total = ref(0)
const selectedRoutes = ref<Route[]>([])
const serviceNames = ref<string[]>([])
const availablePredicates = ref<Predicate[]>([])
const availableFilters = ref<Filter[]>([])
const loadBalanceStrategies = LOAD_BALANCE_STRATEGIES

const formDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const currentRoute = ref<Route>()

const searchForm = reactive<RouteQueryParams>({ routeId: '', routeName: '', enabled: undefined })
const form = reactive({
  id: undefined as number | undefined,
  routeId: '',
  routeName: '',
  description: '',
  serviceName: '',
  order: 0,
  loadBalanceStrategy: 'ROUND_ROBIN',
  predicateIds: [] as number[],
  filterIds: [] as number[],
  enabled: true
})
const pagination = reactive({ page: 1, size: 20 })

const isEdit = computed(() => !!form.id)

const rules: FormRules = {
  routeId: [
    { required: true, message: '请输入路由ID', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9-_]+$/, message: '只能包含字母、数字、下划线和中划线', trigger: 'blur' }
  ],
  routeName: [{ required: true, message: '请输入路由名称', trigger: 'blur' }],
  serviceName: [{ required: true, message: '请选择目标服务', trigger: 'change' }],
  predicateIds: [{ required: true, message: '请至少选择一个断言', trigger: 'change', type: 'array' }]
}

const loadRouteList = async () => {
  try {
    loading.value = true
    const response = await routesApi.list({ ...searchForm, pageNum: pagination.page, pageSize: pagination.size })
    if (response?.data) {
      const data = response.data
      routeList.value = data.data?.data || (Array.isArray(data.data) ? data.data : [])
      total.value = data.data?.total || routeList.value.length
    }
  } catch (error) {
    ElMessage.error('加载路由列表失败')
  } finally {
    loading.value = false
  }
}

const loadServiceNames = async () => {
  try {
    const response = await routesApi.getServiceNames()
    if (response?.data) serviceNames.value = response.data
  } catch (error) {
    console.error('加载服务名称失败', error)
  }
}

const loadAvailablePredicates = async () => {
  try {
    const response = await predicatesApi.getAvailable()
    if (response?.data) availablePredicates.value = response.data
  } catch (error) {
    console.error('加载断言列表失败', error)
  }
}

const loadAvailableFilters = async () => {
  try {
    const response = await filtersApi.getAvailable()
    if (response?.data) availableFilters.value = response.data
  } catch (error) {
    console.error('加载过滤器列表失败', error)
  }
}

const handleSearch = () => { pagination.page = 1; loadRouteList() }
const handleReset = () => { Object.assign(searchForm, { routeId: '', routeName: '', enabled: undefined }); handleSearch() }

const handleAdd = () => {
  Object.assign(form, { id: undefined, routeId: '', routeName: '', description: '', serviceName: '', order: 0, loadBalanceStrategy: 'ROUND_ROBIN', predicateIds: [], filterIds: [], enabled: true })
  formDialogVisible.value = true
}

const handleView = async (route: Route) => {
  try {
    const response = await routesApi.detail(route.id)
    if (response?.data) { currentRoute.value = response.data; detailDialogVisible.value = true }
  } catch (error) {
    ElMessage.error('获取路由详情失败')
  }
}

const handleEdit = async (route: Route) => {
  try {
    const response = await routesApi.detail(route.id)
    if (response?.data) {
      const detail = response.data
      Object.assign(form, {
        id: detail.id,
        routeId: detail.routeId,
        routeName: detail.routeName,
        description: detail.description || '',
        serviceName: detail.uri.replace('lb://', ''),
        order: detail.order || 0,
        loadBalanceStrategy: detail.loadBalanceStrategy || 'ROUND_ROBIN',
        predicateIds: detail.predicates?.map(p => p.id) || [],
        filterIds: detail.filters?.map(f => f.id) || [],
        enabled: detail.enabled
      })
      formDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取路由详情失败')
  }
}

const handleCopy = (route: Route) => {
  Object.assign(form, {
    id: undefined,
    routeId: route.routeId + '_copy',
    routeName: route.routeName + '_copy',
    description: route.description || '',
    serviceName: route.uri.replace('lb://', ''),
    order: route.order || 0,
    loadBalanceStrategy: route.loadBalanceStrategy || 'ROUND_ROBIN',
    predicateIds: route.predicates?.map(p => p.id) || [],
    filterIds: route.filters?.map(f => f.id) || [],
    enabled: true
  })
  formDialogVisible.value = true
}

const handleDelete = async (route: Route) => {
  try {
    await routesApi.delete(route.id)
    ElMessage.success('删除成功')
    loadRouteList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除选中的路由吗？', '批量删除', { type: 'warning' })
    await routesApi.batchDelete(selectedRoutes.value.map(r => r.id))
    ElMessage.success('批量删除成功')
    loadRouteList()
    selectedRoutes.value = []
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('批量删除失败')
  }
}

const handleStatusChange = async (route: Route) => {
  try {
    await (route.enabled ? routesApi.enable(route.id) : routesApi.disable(route.id))
    ElMessage.success(route.enabled ? '启用成功' : '禁用成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    route.enabled = !route.enabled
  }
}

const handleServiceChange = () => {}
const handleSelectionChange = (selection: Route[]) => { selectedRoutes.value = selection }
const handleSizeChange = () => { pagination.page = 1; loadRouteList() }
const handleCurrentChange = () => loadRouteList()

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    formLoading.value = true
    const submitData = {
      routeId: form.routeId,
      routeName: form.routeName,
      description: form.description,
      uri: `lb://${form.serviceName}`,
      predicateIds: form.predicateIds,
      filterIds: form.filterIds,
      order: form.order,
      loadBalanceStrategy: form.loadBalanceStrategy,
      enabled: form.enabled
    }
    if (isEdit.value && form.id) {
      await routesApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await routesApi.create(submitData)
      ElMessage.success('创建成功')
    }
    handleCloseDialog()
    loadRouteList()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleCloseDialog = () => { formRef.value?.resetFields(); formDialogVisible.value = false }

const getStrategyLabel = (strategy: string | undefined) => {
  if (!strategy) return '轮询'
  const found = loadBalanceStrategies.find(s => s.value === strategy)
  return found ? found.label : strategy
}

onMounted(() => {
  loadServiceNames()
  loadAvailablePredicates()
  loadAvailableFilters()
  loadRouteList()
})
</script>

<style lang="scss" scoped>
.config-json {
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 4px;
  padding: 6px;
  font-family: Monaco, Menlo, monospace;
  font-size: 11px;
  max-height: 80px;
  overflow: auto;
  margin: 0;
}
</style>