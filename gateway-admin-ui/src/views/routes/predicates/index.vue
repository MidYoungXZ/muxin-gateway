<template>
  <div class="page-list-container">
    <div class="page-title-bar">
      <span class="title">断言管理</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增断言
      </el-button>
    </div>

    <div class="search-bar">
      <el-input 
        v-model="searchForm.predicateName" 
        placeholder="断言名称"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-select v-model="searchForm.predicateType" placeholder="断言类型" clearable>
        <el-option 
          v-for="type in predicateTypes" 
          :key="type.type" 
          :label="type.name" 
          :value="type.type" 
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
            :disabled="!selectedPredicates.length || hasSystemPredicates"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
        <span class="toolbar-right">共 {{ total }} 条</span>
      </div>

      <el-table 
        :data="predicateList" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
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
    </div>

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

        <el-form-item label="配置参数" prop="args">
          <div class="args-container">
            <div v-if="!form.predicateType" class="args-placeholder">
              请先选择断言类型
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
                    <template v-else-if="field.type === 'datetime'">
                      <el-date-picker
                        v-model="form.args[field.field]"
                        type="datetime"
                        :placeholder="field.placeholder || `请选择${field.label}`"
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
          <pre class="args-json-display">{{ formatConfig(currentPredicate?.args) }}</pre>
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
const argsDialogVisible = ref(false)
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
  args: {} as Record<string, any>
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
  args: [
    { 
      validator: (_rule, _value, callback) => {
        if (!form.predicateType) {
          callback(new Error('请先选择断言类型'))
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
    args: {}
  })
  formDialogVisible.value = true
}

const handleEdit = (predicate: Predicate) => {
  Object.assign(form, {
    id: predicate.id,
    predicateName: predicate.predicateName,
    predicateType: predicate.predicateType,
    description: predicate.description || '',
    args: { ...predicate.args } || {}
  })
  formDialogVisible.value = true
}

const handleCopy = (predicate: Predicate) => {
  Object.assign(form, {
    id: undefined,
    predicateName: predicate.predicateName + '_copy',
    predicateType: predicate.predicateType,
    description: predicate.description || '',
    args: { ...predicate.args } || {}
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
  argsDialogVisible.value = true
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
      predicateName: form.predicateName,
      description: form.description,
      args: form.args
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
</style>