<template>
  <div class="page-list-container">
    <div class="page-title-bar">
      <span class="title">过滤器管理</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增过滤器
      </el-button>
    </div>

    <div class="search-bar">
      <el-input 
        v-model="searchForm.filterName" 
        placeholder="过滤器名称"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-select v-model="searchForm.filterType" placeholder="过滤器类型" clearable>
        <el-option 
          v-for="type in filterTypes" 
          :key="type.value" 
          :label="type.label" 
          :value="type.value" 
        />
      </el-select>
      <el-select v-model="searchForm.enabled" placeholder="状态" clearable>
        <el-option label="启用" :value="true" />
        <el-option label="禁用" :value="false" />
      </el-select>
      <el-select v-model="searchForm.isSystem" placeholder="来源" clearable>
        <el-option label="系统内置" :value="true" />
        <el-option label="自定义" :value="false" />
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
            :disabled="!selectedFilters.length || hasSystemFilters"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
        <span class="toolbar-right">共 {{ total }} 条</span>
      </div>

      <el-table 
        :data="filterList" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="filterName" label="过滤器名称" min-width="160" />
        <el-table-column prop="filterType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getFilterTypeTagType(row.filterType)">
              {{ getFilterTypeLabel(row.filterType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
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
        <el-table-column prop="order" label="排序" width="80" />
        <el-table-column label="来源" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isSystem ? 'info' : 'success'">
              {{ row.isSystem ? '系统内置' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="使用路由" width="100">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleViewRoutes(row)"
            >
              {{ row.usageCount || 0 }} 个
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180">
          <template #default="{ row }">
            <span class="time-cell">{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
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
    </div>

    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑过滤器' : '新增过滤器'"
      width="700px"
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
                  <span style="color: var(--el-text-color-secondary); font-size: 12px; margin-left: 8px">
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

        <el-form-item label="配置参数" prop="args">
          <div class="args-container">
            <div v-if="!form.filterType" class="args-placeholder">
              请先选择过滤器类型
            </div>
            <div v-else class="args-form">
              <template v-for="field in currentConfigFields" :key="field.field">
                <div class="args-item">
                  <label>
                    {{ field.label }}
                    <span v-if="field.required" class="required-mark">*</span>
                  </label>
                  <div class="args-input">
                    <template v-if="field.type === 'array'">
                      <el-select
                        v-model="form.args[field.field]"
                        multiple
                        filterable
                        allow-create
                        default-first-option
                        :placeholder="field.placeholder || `请输入${field.label}`"
                        style="width: 100%"
                      />
                    </template>
                    <template v-else-if="field.type === 'number'">
                      <el-input-number
                        v-model="form.args[field.field]"
                        :placeholder="field.placeholder || `请输入${field.label}`"
                        style="width: 100%"
                      />
                    </template>
                    <template v-else>
                      <el-input
                        v-model="form.args[field.field]"
                        :placeholder="field.placeholder || `请输入${field.label}`"
                      />
                    </template>
                  </div>
                  <div v-if="field.description" class="args-desc">
                    {{ field.description }}
                  </div>
                </div>
              </template>
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
      v-model="argsDialogVisible"
      title="过滤器配置"
      width="500px"
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
        <el-descriptions-item label="排序">
          {{ currentFilter?.order }}
        </el-descriptions-item>
        <el-descriptions-item label="配置参数">
          <pre class="args-json-display">{{ formatConfig(currentFilter?.args) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="routesDialogVisible"
      title="使用路由"
      width="600px"
    >
      <el-table :data="usedRoutes" v-loading="routesLoading" stripe>
        <el-table-column prop="routeId" label="路由ID" width="150" />
        <el-table-column prop="routeName" label="路由名称" min-width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleGoToRoute(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!usedRoutes.length && !routesLoading" class="empty-routes">
        暂无路由使用此过滤器
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { filtersApi, type Filter, type FilterQueryParams, type FilterType, type RouteSimple } from '@/api/filters'

const router = useRouter()

const loading = ref(false)
const formLoading = ref(false)
const routesLoading = ref(false)
const filterList = ref<Filter[]>([])
const total = ref(0)
const selectedFilters = ref<Filter[]>([])
const filterTypes = ref<FilterType[]>([])

const formDialogVisible = ref(false)
const argsDialogVisible = ref(false)
const routesDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const currentFilter = ref<Filter>()
const usedRoutes = ref<RouteSimple[]>([])

const searchForm = reactive<FilterQueryParams>({
  filterName: '',
  filterType: '',
  enabled: undefined,
  isSystem: undefined
})

const form = reactive({
  id: undefined as number | undefined,
  filterName: '',
  filterType: '',
  description: '',
  args: {} as Record<string, any>,
  order: 1
})

const pagination = reactive({
  page: 1,
  size: 20
})

const isEdit = computed(() => !!form.id)

const hasSystemFilters = computed(() => 
  selectedFilters.value.some(f => f.isSystem)
)

const currentConfigFields = computed(() => {
  if (!form.filterType) return []
  const type = filterTypes.value.find(t => t.value === form.filterType)
  return type?.configFields || []
})

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
  ],
  args: [
    { 
      validator: (_rule, _value, callback) => {
        if (!form.filterType) {
          callback(new Error('请先选择过滤器类型'))
          return
        }
        const requiredFields = currentConfigFields.value.filter(f => f.required)
        for (const field of requiredFields) {
          const val = form.args[field.field]
          if (val === undefined || val === null || val === '' || 
              (Array.isArray(val) && val.length === 0)) {
            callback(new Error(`${field.label}不能为空`))
            return
          }
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

const loadFilterList = async () => {
  try {
    loading.value = true
    const queryParams = {
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    const response = await filtersApi.list(queryParams)
    if (response && response.data) {
      const responseData = response.data
      if (responseData.data && Array.isArray(responseData.data.data)) {
        filterList.value = responseData.data.data
        total.value = responseData.data.total
      } else if (Array.isArray(responseData.data)) {
        filterList.value = responseData.data
        total.value = responseData.data.length
      } else {
        filterList.value = []
        total.value = 0
      }
    }
  } catch (error) {
    ElMessage.error('加载过滤器列表失败：' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

const loadFilterTypes = async () => {
  try {
    const response = await filtersApi.getTypes()
    if (response && response.data) {
      filterTypes.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载过滤器类型失败')
  }
}

const getFilterTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'AddRequestHeader': 'primary',
    'AddResponseHeader': 'success',
    'RemoveRequestHeader': 'warning',
    'RemoveResponseHeader': 'info',
    'RewritePath': 'danger',
    'RequestRateLimiter': 'warning',
    'CircuitBreaker': 'info',
    'Retry': ''
  }
  return typeMap[type] || ''
}

const getFilterTypeLabel = (type?: string) => {
  if (!type) return ''
  const typeObj = filterTypes.value.find(t => t.value === type)
  return typeObj?.label || type
}

const handleSearch = () => {
  pagination.page = 1
  loadFilterList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    filterName: '',
    filterType: '',
    enabled: undefined,
    isSystem: undefined
  })
  handleSearch()
}

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    filterName: '',
    filterType: '',
    description: '',
    args: {},
    order: 1
  })
  formDialogVisible.value = true
}

const handleEdit = (filter: Filter) => {
  Object.assign(form, {
    id: filter.id,
    filterName: filter.filterName,
    filterType: filter.filterType,
    description: filter.description || '',
    args: { ...filter.args } || {},
    order: filter.order
  })
  formDialogVisible.value = true
}

const handleCopy = (filter: Filter) => {
  Object.assign(form, {
    id: undefined,
    filterName: filter.filterName + '_copy',
    filterType: filter.filterType,
    description: filter.description || '',
    args: { ...filter.args } || {},
    order: filter.order + 1
  })
  formDialogVisible.value = true
}

const handleDelete = async (filter: Filter) => {
  try {
    await filtersApi.delete(filter.id)
    ElMessage.success('删除成功')
    loadFilterList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除选中的过滤器吗？', '批量删除', {
      type: 'warning'
    })
    const ids = selectedFilters.value.map(f => f.id)
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
    filter.enabled = !filter.enabled
  }
}

const handleViewConfig = (filter: Filter) => {
  currentFilter.value = filter
  argsDialogVisible.value = true
}

const handleViewRoutes = async (filter: Filter) => {
  currentFilter.value = filter
  routesDialogVisible.value = true
  routesLoading.value = true
  try {
    const response = await filtersApi.getUsedRoutes(filter.id)
    if (response && response.data) {
      usedRoutes.value = response.data
    }
  } catch (error) {
    usedRoutes.value = []
  } finally {
    routesLoading.value = false
  }
}

const handleGoToRoute = (route: RouteSimple) => {
  router.push(`/routes/list?id=${route.id}`)
}

const handleSelectionChange = (selection: Filter[]) => {
  selectedFilters.value = selection
}

const handleSizeChange = () => {
  pagination.page = 1
  loadFilterList()
}

const handleCurrentChange = () => {
  loadFilterList()
}

const handleTypeChange = () => {
  form.args = {}
  for (const field of currentConfigFields.value) {
    if (field.defaultValue !== undefined) {
      form.args[field.field] = field.defaultValue
    } else if (field.type === 'array') {
      form.args[field.field] = []
    }
  }
}

const formatConfig = (args?: Record<string, any>) => {
  if (!args) return '{}'
  return JSON.stringify(args, null, 2)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      filterName: form.filterName,
      description: form.description,
      args: form.args,
      order: form.order
    }

    if (isEdit.value && form.id) {
      await filtersApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await filtersApi.create({
        ...submitData,
        filterType: form.filterType
      })
      ElMessage.success('创建成功')
    }

    handleCloseDialog()
    loadFilterList()
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
  loadFilterTypes()
  loadFilterList()
})

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
</script>

<style lang="scss" scoped>
.args-container {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 16px;
  background-color: var(--el-bg-color-page);
  width: 100%;

  .args-placeholder {
    color: var(--el-text-color-secondary);
    text-align: center;
    padding: 20px;
  }

  .args-form {
    .args-item {
      margin-bottom: 16px;

      label {
        display: block;
        margin-bottom: 8px;
        font-weight: 500;
        font-size: 14px;

        .required-mark {
          color: var(--el-color-danger);
          margin-left: 4px;
        }
      }

      .args-input {
        width: 100%;
      }

      .args-desc {
        margin-top: 4px;
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

.args-json-display {
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

.empty-routes {
  text-align: center;
  padding: 40px;
  color: var(--el-text-color-secondary);
}

.time-cell {
  white-space: nowrap;
}
</style>