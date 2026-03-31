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
        <el-table-column label="插件" width="60">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.plugins?.length || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
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

    <RouteFormDialog
      v-model="formDialogVisible"
      :route="currentRoute"
      @success="loadRouteList"
    />

    <el-dialog v-model="detailDialogVisible" title="路由详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="路由ID">{{ currentRoute?.routeId }}</el-descriptions-item>
        <el-descriptions-item label="路由名称">{{ currentRoute?.routeName }}</el-descriptions-item>
        <el-descriptions-item label="目标URI">{{ currentRoute?.uri }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ currentRoute?.order }}</el-descriptions-item>
        <el-descriptions-item label="负载均衡">
          <el-tag>{{ getStrategyLabel(currentRoute?.loadBalanceStrategy) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRoute?.enabled ? 'success' : 'danger'">
            {{ currentRoute?.enabled ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentRoute?.version }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentRoute?.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="currentRoute?.predicates?.length" style="margin-top: 16px">
        <h4 style="margin: 0 0 8px; font-size: 14px">断言配置</h4>
        <el-table :data="currentRoute.predicates" stripe size="small">
          <el-table-column prop="predicateName" label="名称" />
          <el-table-column prop="predicateType" label="类型" />
          <el-table-column label="配置">
            <template #default="{ row }">
              <pre class="config-json">{{ JSON.stringify(row.config, null, 2) }}</pre>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="currentRoute?.plugins?.length" style="margin-top: 16px">
        <h4 style="margin: 0 0 8px; font-size: 14px">插件配置</h4>
        <el-table :data="currentRoute.plugins" stripe size="small">
          <el-table-column prop="pluginName" label="名称" />
          <el-table-column prop="pluginType" label="类型" width="80" />
          <el-table-column label="优先级" width="80">
            <template #default="{ row }">
              {{ row.effectivePriority }}
            </template>
          </el-table-column>
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
const detailDialogVisible = ref(false)
const currentRoute = ref<Route | null>(null)

const searchForm = reactive<RouteQueryParams>({
  routeId: '',
  routeName: '',
  enabled: undefined
})

const pagination = reactive({
  page: 1,
  size: 20
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
    ElMessage.error('加载路由列表失败')
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
  formDialogVisible.value = true
}

const handleView = async (route: Route) => {
  try {
    const response = await routesApi.detail(route.id)
    if (response?.data) {
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
    if (response?.data) {
      currentRoute.value = response.data
      formDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取路由详情失败')
  }
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
.page-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.search-actions {
  display: flex;
  gap: 8px;
}

.table-wrapper {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-right {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.config-json {
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 6px;
  font-family: Monaco, Menlo, monospace;
  font-size: 11px;
  max-height: 80px;
  overflow: auto;
  margin: 0;
}
</style>