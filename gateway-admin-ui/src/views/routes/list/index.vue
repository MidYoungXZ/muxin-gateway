<template>
  <div class="page-list-container">
    <div class="page-title-bar">
      <span class="title">路由列表</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增路由
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchForm.routeId"
        placeholder="路由ID"
        clearable
        @keyup.enter="handleSearch"
        style="width: 180px"
      />
      <el-input
        v-model="searchForm.routeName"
        placeholder="路由名称"
        clearable
        @keyup.enter="handleSearch"
        style="width: 180px"
      />
      <el-select
        v-model="searchForm.enabled"
        placeholder="状态"
        clearable
        style="width: 120px"
      >
        <el-option label="启用" :value="true" />
        <el-option label="禁用" :value="false" />
      </el-select>
      <div class="search-actions">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="table-wrapper">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button
            type="danger"
            plain
            :disabled="!selectedRoutes.length"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
        <span class="toolbar-right">共 {{ total }} 条</span>
      </div>

      <el-table
        :data="routeList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="routeId" label="路由ID" min-width="140" />
        <el-table-column prop="routeName" label="路由名称" min-width="140" />
        <el-table-column prop="uri" label="目标URI" min-width="180" show-overflow-tooltip />
        <el-table-column label="负载均衡" min-width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getStrategyLabel(row.loadBalanceStrategy) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="order" label="优先级" min-width="70" />
        <el-table-column label="断言" min-width="60">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.predicates?.length || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="插件" min-width="60">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.plugins?.length || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="70">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
              <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
            </div>
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

    <RouteFormDialog
      v-model="formDialogVisible"
      :route="currentRoute"
      :mode="dialogMode"
      @success="loadRouteList"
    />

    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { routesApi, LOAD_BALANCE_STRATEGIES, type Route, type RouteQueryParams } from '@/api/routes'
import RouteFormDialog from './components/RouteFormDialog.vue'

const loading = ref(false)
const routeList = ref<Route[]>([])
const total = ref(0)
const selectedRoutes = ref<Route[]>([])

const formDialogVisible = ref(false)
const currentRoute = ref<Route | null>(null)
const dialogMode = ref<'create' | 'edit' | 'view'>('create')

const searchForm = reactive<RouteQueryParams>({
  routeId: '',
  routeName: '',
  enabled: undefined
})

const pagination = reactive({
  page: 1,
  size: 10
})

const loadRouteList = async () => {
  try {
    loading.value = true
    const response = await routesApi.list({
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.size
    })
    if (response?.data) {
      const data = response.data
      routeList.value = data.data || []
      total.value = data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载路由失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadRouteList()
}

const handleReset = () => {
  Object.assign(searchForm, { routeId: '', routeName: '', enabled: undefined })
  handleSearch()
}

const handleAdd = () => {
  currentRoute.value = null
  dialogMode.value = 'create'
  formDialogVisible.value = true
}

const handleView = async (route: Route) => {
  try {
    const response = await routesApi.detail(route.id)
    if (response?.data) {
      currentRoute.value = response.data
      dialogMode.value = 'view'
      formDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取路由详情失败')
  }
}

const handleEdit = async (route: Route) => {
  try {
    const response = await routesApi.detail(route.id)
    if (response?.data) {
      currentRoute.value = response.data
      dialogMode.value = 'edit'
      formDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取路由详情失败')
  }
}

const handleDelete = async (route: Route) => {
  try {
    await ElMessageBox.confirm(`确定要删除路由"${route.routeName}"吗？`, '删除确认', {
      type: 'warning'
    })
    
    await routesApi.delete(route.id)
    ElMessage.success('删除成功')
    loadRouteList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
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

const getStrategyLabel = (strategy: string | undefined) => {
  if (!strategy) return '轮询'
  const found = LOAD_BALANCE_STRATEGIES.find(s => s.value === strategy)
  return found ? found.label : strategy
}

onMounted(() => {
  loadRouteList()
})
</script>

<style lang="scss" scoped>
.title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.search-actions {
  display: flex;
  gap: 8px;
}

.toolbar-right {
  font-size: 13px;
  color: var(--text-secondary);
}

.action-buttons {
  display: flex;
  flex-wrap: nowrap;
  gap: 4px;
  align-items: center;
}
</style>