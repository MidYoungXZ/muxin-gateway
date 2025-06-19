<template>
  <div class="filter-management">
    <div class="page-header">
      <div class="header-left">
        <h1>过滤器管理</h1>
        <p>管理Gateway过滤器，包括系统内置和自定义过滤器</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增过滤器
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" label-width="80px">
        <el-form-item label="过滤器名">
          <el-input 
            v-model="searchForm.filterName" 
            placeholder="请输入过滤器名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="过滤器类型">
          <el-select v-model="searchForm.filterType" placeholder="请选择类型" clearable>
            <el-option 
              v-for="type in filterTypes" 
              :key="type.value" 
              :label="type.label" 
              :value="type.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.enabled" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="过滤器来源">
          <el-select v-model="searchForm.isSystem" placeholder="请选择来源" clearable>
            <el-option label="系统内置" :value="true" />
            <el-option label="自定义" :value="false" />
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

    <!-- 过滤器列表 -->
    <el-card class="table-card">
      <div class="table-header">
        <div class="table-actions">
          <el-button 
            type="danger" 
            :disabled="!selectedFilters.length || hasSystemFilters"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button @click="loadFilterTypes">
            <el-icon><RefreshRight /></el-icon>
            刷新类型
          </el-button>
        </div>
        <div class="table-info">
          共 {{ total }} 条记录
        </div>
      </div>

      <el-table 
        :data="filterList" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="filterName" label="过滤器名称" min-width="160" />
        <el-table-column prop="filterType" label="类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getFilterTypeTagType(row.filterType)">
              {{ getFilterTypeLabel(row.filterType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="order" label="排序" width="80" />
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
        <el-table-column label="来源" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isSystem ? 'info' : 'success'">
              {{ row.isSystem ? '系统内置' : '自定义' }}
            </el-tag>
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
        <el-table-column prop="usageCount" label="使用次数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleEdit(row)"
              :disabled="row.isSystem"
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
              title="确定要删除这个过滤器吗？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button 
                  type="danger" 
                  size="small" 
                  link
                  :disabled="row.isSystem"
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

    <!-- 过滤器表单对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑过滤器' : '新增过滤器'"
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
            <el-form-item label="过滤器名称" prop="filterName">
              <el-input
                v-model="form.filterName"
                placeholder="请输入过滤器名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="过滤器类型" prop="filterType">
              <el-select 
                v-model="form.filterType" 
                placeholder="请选择过滤器类型"
                :disabled="isEdit"
                @change="handleTypeChange"
                style="width: 100%"
              >
                <el-option 
                  v-for="type in filterTypes" 
                  :key="type.value" 
                  :label="type.label" 
                  :value="type.value"
                >
                  <span>{{ type.label }}</span>
                  <span style="color: var(--el-text-color-secondary); font-size: 12px">
                    {{ type.description }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                placeholder="请输入过滤器描述"
                :rows="2"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="order">
              <el-input-number
                v-model="form.order"
                :min="1"
                :max="999"
                placeholder="请输入排序"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 动态配置表单 -->
        <el-form-item label="配置参数">
          <div class="config-container">
            <div v-if="!form.filterType" class="config-placeholder">
              请先选择过滤器类型
            </div>
            <div v-else-if="configTemplate" class="config-form">
              <div v-for="(value, key) in configTemplate" :key="key" class="config-item">
                <label>{{ key }}:</label>
                <el-input 
                  v-if="typeof value === 'string'"
                  v-model="form.config[key]"
                  :placeholder="`请输入${key}`"
                />
                <el-input-number 
                  v-else-if="typeof value === 'number'"
                  v-model="form.config[key]"
                  :placeholder="`请输入${key}`"
                  style="width: 100%"
                />
                <el-select 
                  v-else-if="Array.isArray(value)"
                  v-model="form.config[key]"
                  multiple
                  :placeholder="`请选择${key}`"
                  style="width: 100%"
                >
                  <el-option 
                    v-for="item in value" 
                    :key="item" 
                    :label="item" 
                    :value="item" 
                  />
                </el-select>
                <el-input 
                  v-else
                  v-model="form.config[key]"
                  :placeholder="`请输入${key}`"
                />
              </div>
            </div>
            <div v-else class="config-json">
              <el-input
                v-model="configJson"
                type="textarea"
                placeholder="请输入JSON格式的配置"
                :rows="8"
                @blur="handleConfigJsonChange"
              />
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

    <!-- 配置查看对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      title="过滤器配置"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="过滤器名称">
          {{ currentFilter?.filterName }}
        </el-descriptions-item>
        <el-descriptions-item label="过滤器类型">
          <el-tag>{{ getFilterTypeLabel(currentFilter?.filterType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">
          {{ currentFilter?.description || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="配置参数">
          <pre class="config-json-display">{{ formatConfig(currentFilter?.config) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { filtersApi, type Filter, type FilterQueryParams, type FilterType } from '@/api/filters'

// 数据定义
const loading = ref(false)
const formLoading = ref(false)
const filterList = ref<Filter[]>([])
const total = ref(0)
const selectedFilters = ref<Filter[]>([])
const filterTypes = ref<FilterType[]>([])

// 表单和对话框
const formDialogVisible = ref(false)
const configDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const currentFilter = ref<Filter>()

// 搜索表单
const searchForm = reactive<FilterQueryParams>({
  filterName: '',
  filterType: '',
  enabled: undefined,
  isSystem: undefined
})

// 过滤器表单
const form = reactive({
  id: undefined as number | undefined,
  filterName: '',
  filterType: '',
  description: '',
  config: {} as Record<string, any>,
  order: 1
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20
})

// 配置JSON字符串
const configJson = ref('')

// 计算属性
const isEdit = computed(() => !!form.id)

const hasSystemFilters = computed(() => 
  selectedFilters.value.some(filter => filter.isSystem)
)

const configTemplate = computed(() => {
  if (!form.filterType) return null
  const type = filterTypes.value.find(t => t.value === form.filterType)
  return type?.configTemplate
})

// 表单验证规则
const rules: FormRules = {
  filterName: [
    { required: true, message: '请输入过滤器名称', trigger: 'blur' },
    { min: 2, max: 50, message: '过滤器名称长度在2-50个字符', trigger: 'blur' }
  ],
  filterType: [
    { required: true, message: '请选择过滤器类型', trigger: 'change' }
  ],
  order: [
    { required: true, message: '请输入排序', trigger: 'blur' },
    { type: 'number', min: 1, max: 999, message: '排序范围1-999', trigger: 'blur' }
  ]
}

// 加载过滤器列表
const loadFilterList = async () => {
  try {
    loading.value = true
    console.log('🔄 开始加载过滤器列表...')
    
    const queryParams = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    }
    
    console.log('📤 发送请求参数:', queryParams)
    const response = await filtersApi.list(queryParams)
    console.log('📨 API响应:', response)
    
    if (response && response.data) {
      const responseData = response.data
      
      if (Array.isArray(responseData.data)) {
        filterList.value = responseData.data
        total.value = responseData.total || responseData.data.length
        console.log('✅ 过滤器列表加载成功:', filterList.value.length, '条记录，总计:', total.value)
      } else if (Array.isArray(responseData)) {
        // 如果直接返回数组
        filterList.value = responseData
        total.value = responseData.length
        console.log('✅ 过滤器列表加载成功（直接数组）:', filterList.value.length, '条记录')
      } else {
        console.warn('⚠️ 后端返回的数据格式不正确:', responseData)
        filterList.value = []
        total.value = 0
      }
    } else {
      console.error('❌ API响应数据为空或格式错误:', response)
      filterList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('❌ 加载过滤器列表失败:', error)
    ElMessage.error('加载过滤器列表失败：' + (error as Error).message)
    
    // 如果API失败，显示模拟数据
    const mockFilters: Filter[] = [
      {
        id: 1,
        filterName: '添加请求头',
        filterType: 'AddRequestHeader',
        description: '向请求添加自定义头部',
        config: { name: 'X-Request-From', value: 'gateway' },
        order: 1,
        isSystem: true,
        enabled: true,
        usageCount: 5,
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 2,
        filterName: '路径重写',
        filterType: 'RewritePath',
        description: '重写请求路径',
        config: { regexp: '/api/v1/(?<segment>.*)', replacement: '/${segment}' },
        order: 2,
        isSystem: false,
        enabled: true,
        usageCount: 3,
        createTime: '2024-01-02 10:00:00'
      },
      {
        id: 3,
        filterName: '请求限流',
        filterType: 'RequestRateLimiter',
        description: '限制请求速率',
        config: { replenishRate: 10, burstCapacity: 20 },
        order: 3,
        isSystem: false,
        enabled: false,
        usageCount: 1,
        createTime: '2024-01-03 10:00:00'
      }
    ]
    
    filterList.value = mockFilters
    total.value = mockFilters.length
    console.log('🔄 使用模拟数据:', mockFilters.length, '条记录')
  } finally {
    loading.value = false
  }
}

// 加载过滤器类型
const loadFilterTypes = async () => {
  try {
    console.log('🔄 开始加载过滤器类型...')
    const response = await filtersApi.getTypes()
    
    if (response && response.data) {
      filterTypes.value = response.data
      console.log('✅ 过滤器类型加载成功:', filterTypes.value.length, '种类型')
    }
  } catch (error) {
    console.error('❌ 加载过滤器类型失败:', error)
    ElMessage.error('加载过滤器类型失败：' + (error as Error).message)
    
    // 使用模拟数据
    filterTypes.value = [
      {
        value: 'AddRequestHeader',
        label: '添加请求头',
        description: '向请求中添加Header',
        configTemplate: { name: 'X-Request-From', value: 'gateway' }
      },
      {
        value: 'AddResponseHeader',
        label: '添加响应头',
        description: '向响应中添加Header',
        configTemplate: { name: 'X-Response-From', value: 'gateway' }
      },
      {
        value: 'RewritePath',
        label: '路径重写',
        description: '重写请求路径',
        configTemplate: { regexp: '/api/v1/(?<segment>.*)', replacement: '/${segment}' }
      },
      {
        value: 'RequestRateLimiter',
        label: '请求限流',
        description: '限制请求速率',
        configTemplate: { replenishRate: 10, burstCapacity: 20 }
      }
    ]
  }
}

// 获取过滤器类型标签样式
const getFilterTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'AddRequestHeader': 'primary',
    'AddResponseHeader': 'success',
    'RewritePath': 'warning',
    'RequestRateLimiter': 'danger',
    'CircuitBreaker': 'info'
  }
  return typeMap[type] || 'default'
}

// 获取过滤器类型标签文本
const getFilterTypeLabel = (type?: string) => {
  if (!type) return ''
  const typeObj = filterTypes.value.find(t => t.value === type)
  return typeObj?.label || type
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadFilterList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    filterName: '',
    filterType: '',
    enabled: undefined,
    isSystem: undefined
  })
  handleSearch()
}

// 新增过滤器
const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    filterName: '',
    filterType: '',
    description: '',
    config: {},
    order: 1
  })
  configJson.value = ''
  formDialogVisible.value = true
}

// 编辑过滤器
const handleEdit = (filter: Filter) => {
  Object.assign(form, {
    id: filter.id,
    filterName: filter.filterName,
    filterType: filter.filterType,
    description: filter.description || '',
    config: { ...filter.config } || {},
    order: filter.order
  })
  configJson.value = JSON.stringify(filter.config || {}, null, 2)
  formDialogVisible.value = true
}

// 复制过滤器
const handleCopy = (filter: Filter) => {
  Object.assign(form, {
    id: undefined,
    filterName: filter.filterName + '_copy',
    filterType: filter.filterType,
    description: filter.description || '',
    config: { ...filter.config } || {},
    order: filter.order + 1
  })
  configJson.value = JSON.stringify(filter.config || {}, null, 2)
  formDialogVisible.value = true
}

// 删除过滤器
const handleDelete = async (filter: Filter) => {
  try {
    await filtersApi.delete(filter.id)
    ElMessage.success('删除成功')
    loadFilterList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除选中的过滤器吗？', '批量删除', {
      type: 'warning'
    })
    
    const ids = selectedFilters.value.map(filter => filter.id)
    await filtersApi.batchDelete(ids)
    ElMessage.success('批量删除成功')
    loadFilterList()
    selectedFilters.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (filter: Filter) => {
  try {
    if (filter.enabled) {
      await filtersApi.enable(filter.id)
      ElMessage.success('启用成功')
    } else {
      await filtersApi.disable(filter.id)
      ElMessage.success('禁用成功')
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    filter.enabled = !filter.enabled
  }
}

// 查看配置
const handleViewConfig = (filter: Filter) => {
  currentFilter.value = filter
  configDialogVisible.value = true
}

// 选择变更
const handleSelectionChange = (selection: Filter[]) => {
  selectedFilters.value = selection
}

// 分页变更
const handleSizeChange = () => {
  pagination.page = 1
  loadFilterList()
}

const handleCurrentChange = () => {
  loadFilterList()
}

// 过滤器类型变更
const handleTypeChange = () => {
  form.config = {}
  if (configTemplate.value) {
    form.config = { ...configTemplate.value }
    configJson.value = JSON.stringify(configTemplate.value, null, 2)
  } else {
    configJson.value = '{}'
  }
}

// 配置JSON变更
const handleConfigJsonChange = () => {
  try {
    form.config = JSON.parse(configJson.value || '{}')
  } catch (error) {
    ElMessage.error('JSON格式不正确')
  }
}

// 格式化配置显示
const formatConfig = (config?: Record<string, any>) => {
  if (!config) return '{}'
  return JSON.stringify(config, null, 2)
}

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      filterName: form.filterName,
      filterType: form.filterType,
      description: form.description,
      config: form.config,
      order: form.order
    }

    if (isEdit.value && form.id) {
      console.log('📤 更新过滤器:', form.id, submitData)
      await filtersApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      console.log('📤 创建过滤器:', submitData)
      await filtersApi.create(submitData)
      ElMessage.success('创建成功')
    }

    handleCloseDialog()
    loadFilterList()
  } catch (error) {
    console.error('❌ 提交失败:', error)
    ElMessage.error('操作失败：' + (error as Error).message)
  } finally {
    formLoading.value = false
  }
}

// 关闭对话框
const handleCloseDialog = () => {
  formRef.value?.resetFields()
  formDialogVisible.value = false
}

// 初始化
onMounted(() => {
  loadFilterTypes()
  loadFilterList()
})
</script>

<style lang="scss" scoped>
.filter-management {
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
    
    .header-right {
      .el-button + .el-button {
        margin-left: 12px;
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
      
      .table-actions {
        .el-button + .el-button {
          margin-left: 8px;
        }
      }
      
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

    .config-placeholder {
      color: var(--el-text-color-secondary);
      text-align: center;
      padding: 20px;
    }

    .config-form {
      .config-item {
        display: flex;
        align-items: center;
        margin-bottom: 12px;

        label {
          min-width: 120px;
          margin-right: 12px;
          font-weight: 500;
        }

        .el-input,
        .el-input-number,
        .el-select {
          flex: 1;
        }
      }
    }

    .config-json {
      .el-textarea {
        font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
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