<template>
  <div class="page-list-container">
    <div class="page-title-bar">
      <span class="title">操作日志</span>
    </div>

    <div class="search-bar">
      <el-input
        v-model="queryForm.module"
        placeholder="模块"
        clearable
      />
      <el-input
        v-model="queryForm.operation"
        placeholder="操作"
        clearable
      />
      <el-input
        v-model="queryForm.operator"
        placeholder="操作人"
        clearable
      />
      <el-select
        v-model="queryForm.status"
        placeholder="状态"
        clearable
      >
        <el-option label="成功" :value="1" />
        <el-option label="失败" :value="0" />
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
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
          <el-button
            type="warning"
            @click="handleClearAll"
          >
            清空日志
          </el-button>
        </div>
        <span class="toolbar-right">共 {{ pagination.total }} 条</span>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        row-key="id"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="operation" label="操作" width="120" />
        <el-table-column prop="method" label="请求方法" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getMethodTagType(row.method)"
              size="small"
            >
              {{ row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="operatorIp" label="操作IP" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="执行时长" width="100">
          <template #default="{ row }">
            <span>{{ row.duration }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="operateTime" label="操作时间" min-width="180">
          <template #default="{ row }">
            <span class="time-cell">{{ formatDateTime(row.operateTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="handleViewDetail(row)">详情</el-button>
            <el-button link type="danger" size="small" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageNumChange"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="操作日志详情"
      width="800px"
      destroy-on-close
    >
      <div v-if="currentLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
          <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
          <el-descriptions-item label="操作">{{ currentLog.operation }}</el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag :type="getMethodTagType(currentLog.method)" size="small">
              {{ currentLog.method }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求URL" :span="2">{{ currentLog.requestUrl }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentLog.operator }}</el-descriptions-item>
          <el-descriptions-item label="操作IP">{{ currentLog.operatorIp }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentLog.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行时长">{{ currentLog.duration }}ms</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ formatDateTime(currentLog.operateTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <div v-if="currentLog.params" class="detail-section">
          <h4>请求参数</h4>
          <el-input
            v-model="currentLog.params"
            type="textarea"
            :rows="4"
            readonly
          />
        </div>
        
        <div v-if="currentLog.result" class="detail-section">
          <h4>返回结果</h4>
          <el-input
            v-model="currentLog.result"
            type="textarea"
            :rows="4"
            readonly
          />
        </div>
        
        <div v-if="currentLog.error" class="detail-section">
          <h4>异常信息</h4>
          <el-input
            v-model="currentLog.error"
            type="textarea"
            :rows="4"
            readonly
            class="error-text"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { operationLogApi } from '@/api/operationLog'
import type { OperationLog, OperationLogQueryParams } from '@/types/system'

// 响应式数据
const loading = ref(false)
const tableData = ref<OperationLog[]>([])
const selectedIds = ref<number[]>([])
const detailDialogVisible = ref(false)
const currentLog = ref<OperationLog | null>(null)

// 查询表单
const queryForm = reactive<OperationLogQueryParams>({
  pageNum: 1,
  pageSize: 20,
  module: '',
  operation: '',
  operator: '',
  status: undefined,
  method: '',
  keyword: '',
  startTime: '',
  endTime: ''
})

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 方法
const getMethodTagType = (method: string) => {
  const typeMap: Record<string, string> = {
    'GET': 'info',
    'POST': 'success', 
    'PUT': 'warning',
    'DELETE': 'danger'
  }
  return typeMap[method] || 'info'
}

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      ...queryForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    
    const response = await operationLogApi.getOperationLogs(params)
    
    if (response && response.data) {
      const pageData = response.data
      tableData.value = pageData.data || []
      pagination.total = pageData.total || 0
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('数据加载失败:', error)
    ElMessage.error('数据加载失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  Object.assign(queryForm, {
    module: '',
    operation: '',
    operator: '',
    status: undefined,
    method: '',
    keyword: '',
    startTime: '',
    endTime: ''
  })
  pagination.pageNum = 1
  loadData()
}

const handlePageSizeChange = (newSize: number) => {
  pagination.pageSize = newSize
  pagination.pageNum = 1
  loadData()
}

const handlePageNumChange = (newPage: number) => {
  pagination.pageNum = newPage
  loadData()
}

const handleSelectionChange = (selection: OperationLog[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleViewDetail = async (row: OperationLog) => {
  try {
    const response = await operationLogApi.getOperationLogDetail(row.id)
    if (response && response.data) {
      currentLog.value = response.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const handleDelete = async (row: OperationLog) => {
  try {
    await ElMessageBox.confirm(`确定要删除日志ID为 ${row.id} 的记录吗？`, '删除确认', {
      type: 'warning'
    })
    
    await operationLogApi.batchDeleteOperationLogs([row.id])
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？`, '批量删除确认', {
      type: 'warning'
    })
    
    await operationLogApi.batchDeleteOperationLogs(selectedIds.value)
    ElMessage.success(`成功删除 ${selectedIds.value.length} 条记录`)
    selectedIds.value = []
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复！', '清空确认', {
      type: 'warning',
      confirmButtonText: '确定清空',
      cancelButtonText: '取消'
    })
    
    await operationLogApi.clearAllOperationLogs()
    ElMessage.success('日志清空成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败:', error)
      ElMessage.error('清空失败')
    }
  }
}

const handleExport = async () => {
  try {
    ElMessage.info('正在导出数据...')
    
    const params = { ...queryForm, pageNum: 1, pageSize: 10000 }
    await operationLogApi.exportOperationLogs(params)
    
    ElMessage.success('数据导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 时间格式化 - 标准格式不带T
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.log-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.error-text :deep(.el-textarea__inner) {
  color: #f56c6c;
}

.time-cell {
  white-space: nowrap;
}
</style> 