<template>
  <div class="predicate-management">
    <div class="page-header">
      <div class="header-left">
        <h1>断言管理</h1>
        <p>管理Gateway断言，用于路由匹配条件配置</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增断言
        </el-button>
      </div>
    </div>

    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" label-width="80px">
        <el-form-item label="断言名称">
          <el-input 
            v-model="searchForm.predicateName" 
            placeholder="请输入断言名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="断言类型">
          <el-select v-model="searchForm.predicateType" placeholder="请选择类型" clearable style="width: 150px">
            <el-option 
              v-for="type in predicateTypes" 
              :key="type.type" 
              :label="type.name" 
              :value="type.type" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.enabled" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="searchForm.isSystem" placeholder="请选择来源" clearable style="width: 120px">
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

    <el-card class="table-card">
      <div class="table-header">
        <div class="table-actions">
          <el-button 
            type="danger" 
            :disabled="!selectedPredicates.length || hasSystemPredicates"
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
        :data="predicateList" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="predicateName" label="断言名称" min-width="160" />
        <el-table-column prop="predicateType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getPredicateTypeTagType(row.predicateType)">
              {{ row.predicateTypeDesc || row.predicateType }}
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
        <el-table-column prop="createTime" label="创建时间" width="160" />
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
              title="确定要删除这个断言吗？"
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

    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑断言' : '新增断言'"
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
            <el-form-item label="断言名称" prop="predicateName">
              <el-input
                v-model="form.predicateName"
                placeholder="请输入断言名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="断言类型" prop="predicateType">
              <el-select 
                v-model="form.predicateType" 
                placeholder="请选择断言类型"
                :disabled="isEdit"
                @change="handleTypeChange"
                style="width: 100%"
              >
                <el-option 
                  v-for="type in predicateTypes" 
                  :key="type.type" 
                  :label="type.name" 
                  :value="type.type"
                >
                  <span>{{ type.name }}</span>
                  <span style="color: var(--el-text-color-secondary); font-size: 12px; margin-left: 8px">
                    {{ type.description }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入断言描述"
            :rows="2"
          />
        </el-form-item>

        <el-form-item label="配置参数" prop="config">
          <div class="config-container">
            <div v-if="!form.predicateType" class="config-placeholder">
              请先选择断言类型
            </div>
            <div v-else class="config-form">
              <template v-for="field in currentConfigFields" :key="field.field">
                <div class="config-item">
                  <label>
                    {{ field.label }}
                    <span v-if="field.required" class="required-mark">*</span>
                  </label>
                  <div class="config-input">
                    <template v-if="field.type === 'array'">
                      <el-select
                        v-model="form.config[field.field]"
                        multiple
                        filterable
                        allow-create
                        default-first-option
                        :placeholder="field.placeholder || `请输入${field.label}`"
                        style="width: 100%"
                      />
                    </template>
                    <template v-else-if="field.type === 'datetime'">
                      <el-date-picker
                        v-model="form.config[field.field]"
                        type="datetime"
                        :placeholder="field.placeholder || `请选择${field.label}`"
                        style="width: 100%"
                      />
                    </template>
                    <template v-else-if="field.type === 'number'">
                      <el-input-number
                        v-model="form.config[field.field]"
                        :placeholder="field.placeholder || `请输入${field.label}`"
                        style="width: 100%"
                      />
                    </template>
                    <template v-else>
                      <el-input
                        v-model="form.config[field.field]"
                        :placeholder="field.placeholder || `请输入${field.label}`"
                      />
                    </template>
                  </div>
                  <div v-if="field.description" class="config-desc">
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
      v-model="configDialogVisible"
      title="断言配置"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="断言名称">
          {{ currentPredicate?.predicateName }}
        </el-descriptions-item>
        <el-descriptions-item label="断言类型">
          <el-tag>{{ currentPredicate?.predicateTypeDesc || currentPredicate?.predicateType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">
          {{ currentPredicate?.description || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="配置参数">
          <pre class="config-json-display">{{ formatConfig(currentPredicate?.config) }}</pre>
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
        暂无路由使用此断言
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { predicatesApi, type Predicate, type PredicateQueryParams, type PredicateType, type RouteSimple } from '@/api/predicates'

const router = useRouter()

const loading = ref(false)
const formLoading = ref(false)
const routesLoading = ref(false)
const predicateList = ref<Predicate[]>([])
const total = ref(0)
const selectedPredicates = ref<Predicate[]>([])
const predicateTypes = ref<PredicateType[]>([])

const formDialogVisible = ref(false)
const configDialogVisible = ref(false)
const routesDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const currentPredicate = ref<Predicate>()
const usedRoutes = ref<RouteSimple[]>([])

const searchForm = reactive<PredicateQueryParams>({
  predicateName: '',
  predicateType: '',
  enabled: undefined,
  isSystem: undefined
})

const form = reactive({
  id: undefined as number | undefined,
  predicateName: '',
  predicateType: '',
  description: '',
  config: {} as Record<string, any>
})

const pagination = reactive({
  page: 1,
  size: 20
})

const isEdit = computed(() => !!form.id)

const hasSystemPredicates = computed(() => 
  selectedPredicates.value.some(p => p.isSystem)
)

const currentConfigFields = computed(() => {
  if (!form.predicateType) return []
  const type = predicateTypes.value.find(t => t.type === form.predicateType)
  return type?.configFields || []
})

const rules: FormRules = {
  predicateName: [
    { required: true, message: '请输入断言名称', trigger: 'blur' },
    { min: 2, max: 50, message: '断言名称长度在2-50个字符', trigger: 'blur' }
  ],
  predicateType: [
    { required: true, message: '请选择断言类型', trigger: 'change' }
  ],
  config: [
    { 
      validator: (_rule, _value, callback) => {
        if (!form.predicateType) {
          callback(new Error('请先选择断言类型'))
          return
        }
        const requiredFields = currentConfigFields.value.filter(f => f.required)
        for (const field of requiredFields) {
          const val = form.config[field.field]
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

const loadPredicateList = async () => {
  try {
    loading.value = true
    const queryParams = {
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    const response = await predicatesApi.list(queryParams)
    if (response && response.data) {
      const responseData = response.data
      if (responseData.data && Array.isArray(responseData.data.data)) {
        predicateList.value = responseData.data.data
        total.value = responseData.data.total
      } else if (Array.isArray(responseData.data)) {
        predicateList.value = responseData.data
        total.value = responseData.data.length
      } else {
        predicateList.value = []
        total.value = 0
      }
    }
  } catch (error) {
    ElMessage.error('加载断言列表失败：' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

const loadPredicateTypes = async () => {
  try {
    const response = await predicatesApi.getTypes()
    if (response && response.data) {
      predicateTypes.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载断言类型失败')
  }
}

const getPredicateTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'Path': 'primary',
    'Method': 'success',
    'Header': 'warning',
    'Query': 'info',
    'Cookie': 'danger',
    'Host': '',
    'RemoteAddr': 'success',
    'Between': 'warning'
  }
  return typeMap[type] || ''
}

const handleSearch = () => {
  pagination.page = 1
  loadPredicateList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    predicateName: '',
    predicateType: '',
    enabled: undefined,
    isSystem: undefined
  })
  handleSearch()
}

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    predicateName: '',
    predicateType: '',
    description: '',
    config: {}
  })
  formDialogVisible.value = true
}

const handleEdit = (predicate: Predicate) => {
  Object.assign(form, {
    id: predicate.id,
    predicateName: predicate.predicateName,
    predicateType: predicate.predicateType,
    description: predicate.description || '',
    config: { ...predicate.config } || {}
  })
  formDialogVisible.value = true
}

const handleCopy = (predicate: Predicate) => {
  Object.assign(form, {
    id: undefined,
    predicateName: predicate.predicateName + '_copy',
    predicateType: predicate.predicateType,
    description: predicate.description || '',
    config: { ...predicate.config } || {}
  })
  formDialogVisible.value = true
}

const handleDelete = async (predicate: Predicate) => {
  try {
    await predicatesApi.delete(predicate.id)
    ElMessage.success('删除成功')
    loadPredicateList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除选中的断言吗？', '批量删除', {
      type: 'warning'
    })
    const ids = selectedPredicates.value.map(p => p.id)
    await predicatesApi.batchDelete(ids)
    ElMessage.success('批量删除成功')
    loadPredicateList()
    selectedPredicates.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleStatusChange = async (predicate: Predicate) => {
  try {
    if (predicate.enabled) {
      await predicatesApi.enable(predicate.id)
      ElMessage.success('启用成功')
    } else {
      await predicatesApi.disable(predicate.id)
      ElMessage.success('禁用成功')
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    predicate.enabled = !predicate.enabled
  }
}

const handleViewConfig = (predicate: Predicate) => {
  currentPredicate.value = predicate
  configDialogVisible.value = true
}

const handleViewRoutes = async (predicate: Predicate) => {
  currentPredicate.value = predicate
  routesDialogVisible.value = true
  routesLoading.value = true
  try {
    const response = await predicatesApi.getUsedRoutes(predicate.id)
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

const handleSelectionChange = (selection: Predicate[]) => {
  selectedPredicates.value = selection
}

const handleSizeChange = () => {
  pagination.page = 1
  loadPredicateList()
}

const handleCurrentChange = () => {
  loadPredicateList()
}

const handleTypeChange = () => {
  form.config = {}
  for (const field of currentConfigFields.value) {
    if (field.defaultValue !== undefined) {
      form.config[field.field] = field.defaultValue
    } else if (field.type === 'array') {
      form.config[field.field] = []
    }
  }
}

const formatConfig = (config?: Record<string, any>) => {
  if (!config) return '{}'
  return JSON.stringify(config, null, 2)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      predicateName: form.predicateName,
      description: form.description,
      config: form.config
    }

    if (isEdit.value && form.id) {
      await predicatesApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await predicatesApi.create({
        ...submitData,
        predicateType: form.predicateType
      })
      ElMessage.success('创建成功')
    }

    handleCloseDialog()
    loadPredicateList()
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
  loadPredicateTypes()
  loadPredicateList()
})
</script>

<style lang="scss" scoped>
.predicate-management {
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

    .config-placeholder {
      color: var(--el-text-color-secondary);
      text-align: center;
      padding: 20px;
    }

    .config-form {
      .config-item {
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

  .empty-routes {
    text-align: center;
    padding: 40px;
    color: var(--el-text-color-secondary);
  }
}
</style>