<template>
  <div class="route-management">
    <div class="page-header">
      <div class="header-left">
        <h1>路由列表</h1>
        <p>管理Gateway路由配置，包括断言、过滤器等</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增路由
        </el-button>
      </div>
    </div>

    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" label-width="80px">
        <el-form-item label="路由ID">
          <el-input 
            v-model="searchForm.routeId" 
            placeholder="请输入路由ID"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="路由名称">
          <el-input 
            v-model="searchForm.routeName" 
            placeholder="请输入路由名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
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
        <div class="table-actions">
          <el-button 
            type="danger" 
            :disabled="!selectedRoutes.length"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
        <div class="table-info">
          共 {{ total }} 条记录
        </div>
      </div>

      <el-table 
        :data="routeList" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="routeId" label="路由ID" min-width="140" />
        <el-table-column prop="routeName" label="路由名称" min-width="140" />
        <el-table-column prop="uri" label="目标URI" min-width="180" show-overflow-tooltip />
        <el-table-column prop="order" label="优先级" width="80" />
        <el-table-column label="断言" width="80">
          <template #default="{ row }">
            <el-tag type="info">{{ row.predicates?.length || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="过滤器" width="80">
          <template #default="{ row }">
            <el-tag type="info">{{ row.filters?.length || 0 }}</el-tag>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleCopy(row)"
            >
              复制
            </el-button>
            <el-popconfirm
              title="确定要删除这个路由吗？"
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
      :title="isEdit ? '编辑路由' : '新增路由'"
      width="800px"
      :close-on-click-modal="false"
      @close="handleCloseDialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="路由ID" prop="routeId">
              <el-input
                v-model="form.routeId"
                placeholder="请输入路由ID"
                :disabled="isEdit"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由名称" prop="routeName">
              <el-input
                v-model="form.routeName"
                placeholder="请输入路由名称"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="目标服务" prop="serviceName">
              <el-select 
                v-model="form.serviceName" 
                placeholder="请选择目标服务"
                filterable
                style="width: 100%"
                @change="handleServiceChange"
              >
                <el-option 
                  v-for="name in serviceNames" 
                  :key="name" 
                  :label="name" 
                  :value="name"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="order">
              <el-input-number
                v-model="form.order"
                :min="0"
                :max="9999"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入路由描述"
            :rows="2"
          />
        </el-form-item>

        <el-form-item label="断言配置" prop="predicateIds">
          <el-select
            v-model="form.predicateIds"
            multiple
            placeholder="请选择断言"
            style="width: 100%"
          >
            <el-option 
              v-for="p in availablePredicates" 
              :key="p.id" 
              :label="`${p.predicateName} (${p.predicateTypeDesc || p.predicateType})`" 
              :value="p.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="过滤器配置" prop="filterIds">
          <el-select
            v-model="form.filterIds"
            multiple
            placeholder="请选择过滤器"
            style="width: 100%"
          >
            <el-option 
              v-for="f in availableFilters" 
              :key="f.id" 
              :label="`${f.filterName} (${f.filterType})`" 
              :value="f.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-switch v-model="form.enabled" />
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
      v-model="detailDialogVisible"
      title="路由详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="路由ID">{{ currentRoute?.routeId }}</el-descriptions-item>
        <el-descriptions-item label="路由名称">{{ currentRoute?.routeName }}</el-descriptions-item>
        <el-descriptions-item label="目标URI">{{ currentRoute?.uri }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ currentRoute?.order }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRoute?.enabled ? 'success' : 'danger'">
            {{ currentRoute?.enabled ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentRoute?.version }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentRoute?.description || '无' }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section" v-if="currentRoute?.predicates?.length">
        <h4>断言配置</h4>
        <el-table :data="currentRoute.predicates" stripe size="small">
          <el-table-column prop="predicateName" label="名称" />
          <el-table-column prop="predicateTypeDesc" label="类型" />
          <el-table-column label="配置">
            <template #default="{ row }">
              <pre class="config-json">{{ JSON.stringify(row.config, null, 2) }}</pre>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="detail-section" v-if="currentRoute?.filters?.length">
        <h4>过滤器配置</h4>
        <el-table :data="currentRoute.filters" stripe size="small">
          <el-table-column prop="filterName" label="名称" />
          <el-table-column prop="filterType" label="类型" />
          <el-table-column label="配置">
            <template #default="{ row }">
              <pre class="config-json">{{ JSON.stringify(row.config, null, 2) }}</pre>
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
import { routesApi, type Route, type RouteQueryParams, type PredicateInfo, type FilterInfo } from '@/api/routes'
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

const formDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const currentRoute = ref<Route>()

const searchForm = reactive<RouteQueryParams>({
  routeId: '',
  routeName: '',
  enabled: undefined
})

const form = reactive({
  id: undefined as number | undefined,
  routeId: '',
  routeName: '',
  description: '',
  serviceName: '',
  order: 0,
  predicateIds: [] as number[],
  filterIds: [] as number[],
  enabled: true
})

const pagination = reactive({
  page: 1,
  size: 20
})

const isEdit = computed(() => !!form.id)

const rules: FormRules = {
  routeId: [
    { required: true, message: '请输入路由ID', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9-_]+$/, message: '只能包含字母、数字、下划线和中划线', trigger: 'blur' }
  ],
  routeName: [
    { required: true, message: '请输入路由名称', trigger: 'blur' }
  ],
  serviceName: [
    { required: true, message: '请选择目标服务', trigger: 'change' }
  ],
  predicateIds: [
    { required: true, message: '请至少选择一个断言', trigger: 'change', type: 'array' }
  ]
}

const loadRouteList = async () => {
  try {
    loading.value = true
    const queryParams = {
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    const response = await routesApi.list(queryParams)
    if (response && response.data) {
      const responseData = response.data
      if (responseData.data && Array.isArray(responseData.data.data)) {
        routeList.value = responseData.data.data
        total.value = responseData.data.total
      } else if (Array.isArray(responseData.data)) {
        routeList.value = responseData.data
        total.value = responseData.data.length
      } else {
        routeList.value = []
        total.value = 0
      }
    }
  } catch (error) {
    ElMessage.error('加载路由列表失败：' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

const loadServiceNames = async () => {
  try {
    const response = await routesApi.getServiceNames()
    if (response && response.data) {
      serviceNames.value = response.data
    }
  } catch (error) {
    console.error('加载服务名称失败', error)
  }
}

const loadAvailablePredicates = async () => {
  try {
    const response = await predicatesApi.getAvailable()
    if (response && response.data) {
      availablePredicates.value = response.data
    }
  } catch (error) {
    console.error('加载断言列表失败', error)
  }
}

const loadAvailableFilters = async () => {
  try {
    const response = await filtersApi.getAvailable()
    if (response && response.data) {
      availableFilters.value = response.data
    }
  } catch (error) {
    console.error('加载过滤器列表失败', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadRouteList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    routeId: '',
    routeName: '',
    enabled: undefined
  })
  handleSearch()
}

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    routeId: '',
    routeName: '',
    description: '',
    serviceName: '',
    order: 0,
    predicateIds: [],
    filterIds: [],
    enabled: true
  })
  formDialogVisible.value = true
}

const handleView = async (route: Route) => {
  try {
    const response = await routesApi.detail(route.id)
    if (response && response.data) {
      currentRoute.value = response.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取路由详情失败')
  }
}

const handleEdit = async (route: Route) => {
  try {
    const response = await routesApi.detail(route.id)
    if (response && response.data) {
      const detail = response.data
      const serviceName = detail.uri.replace('lb://', '')
      Object.assign(form, {
        id: detail.id,
        routeId: detail.routeId,
        routeName: detail.routeName,
        description: detail.description || '',
        serviceName: serviceName,
        order: detail.order || 0,
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
  const serviceName = route.uri.replace('lb://', '')
  Object.assign(form, {
    id: undefined,
    routeId: route.routeId + '_copy',
    routeName: route.routeName + '_copy',
    description: route.description || '',
    serviceName: serviceName,
    order: route.order || 0,
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
    await ElMessageBox.confirm('确定要删除选中的路由吗？', '批量删除', {
      type: 'warning'
    })
    const ids = selectedRoutes.value.map(r => r.id)
    await routesApi.batchDelete(ids)
    ElMessage.success('批量删除成功')
    loadRouteList()
    selectedRoutes.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleStatusChange = async (route: Route) => {
  try {
    if (route.enabled) {
      await routesApi.enable(route.id)
      ElMessage.success('启用成功')
    } else {
      await routesApi.disable(route.id)
      ElMessage.success('禁用成功')
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    route.enabled = !route.enabled
  }
}

const handleServiceChange = (name: string) => {
}

const handleSelectionChange = (selection: Route[]) => {
  selectedRoutes.value = selection
}

const handleSizeChange = () => {
  pagination.page = 1
  loadRouteList()
}

const handleCurrentChange = () => {
  loadRouteList()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData: any = {
      routeId: form.routeId,
      routeName: form.routeName,
      description: form.description,
      uri: `lb://${form.serviceName}`,
      predicateIds: form.predicateIds,
      filterIds: form.filterIds,
      order: form.order,
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
    ElMessage.error('操作失败：' + (error as Error).message)
  } finally {
    formLoading.value = false
  }
}

const handleCloseDialog = () => {
  formRef.value?.resetFields()
  formDialogVisible.value = false
}

onMounted(() => {
  loadServiceNames()
  loadAvailablePredicates()
  loadAvailableFilters()
  loadRouteList()
})
</script>

<style lang="scss" scoped>
.route-management {
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

  .detail-section {
    margin-top: 20px;
    
    h4 {
      margin: 0 0 12px 0;
      font-size: 14px;
      font-weight: 600;
    }
  }

  .config-json {
    background-color: var(--el-bg-color-page);
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    padding: 8px;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 12px;
    line-height: 1.5;
    max-height: 100px;
    overflow-y: auto;
    white-space: pre-wrap;
    word-break: break-all;
    margin: 0;
  }
}
</style>