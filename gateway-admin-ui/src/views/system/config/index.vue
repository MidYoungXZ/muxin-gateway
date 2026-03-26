<template>
  <div class="page-list-container">
    <div class="page-title-bar">
      <span class="title">系统配置</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增配置
      </el-button>
    </div>

    <div class="search-bar">
      <el-input 
        v-model="searchForm.configKey" 
        placeholder="配置键"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-input 
        v-model="searchForm.configName" 
        placeholder="配置名称"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-select v-model="searchForm.status" placeholder="状态" clearable>
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
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
            :disabled="!selectedConfigs.length"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
        <span class="toolbar-right">共 {{ total }} 条</span>
      </div>

      <el-table 
        :data="configList" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="configKey" label="配置键" min-width="180" show-overflow-tooltip />
        <el-table-column prop="configName" label="配置名称" min-width="150" />
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip :content="row.configValue" placement="top" :disabled="!row.configValue || row.configValue.length < 30">
              <span>{{ row.configValue ? (row.configValue.length > 30 ? row.configValue.substring(0, 30) + '...' : row.configValue) : '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
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
              title="确定要删除这个配置吗？"
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
    </div>

    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑配置' : '新增配置'"
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
        <el-form-item label="配置键" prop="configKey">
          <el-input
            v-model="form.configKey"
            placeholder="请输入配置键"
            :disabled="isEdit"
            @blur="checkConfigKey"
          />
          <div class="form-tip">配置键只能包含字母、数字和下划线</div>
        </el-form-item>
        
        <el-form-item label="配置名称" prop="configName">
          <el-input
            v-model="form.configName"
            placeholder="请输入配置名称"
          />
        </el-form-item>
        
        <el-form-item label="配置值" prop="configValue">
          <el-input
            v-model="form.configValue"
            type="textarea"
            :rows="4"
            placeholder="请输入配置值"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入描述"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { configApi, type Config, type ConfigQueryParams } from '@/api/config'

const loading = ref(false)
const formLoading = ref(false)
const configList = ref<Config[]>([])
const total = ref(0)
const selectedConfigs = ref<Config[]>([])

const formDialogVisible = ref(false)
const formRef = ref<FormInstance>()

const searchForm = reactive<ConfigQueryParams>({
  configKey: '',
  configName: '',
  status: undefined
})

const form = reactive({
  id: undefined as number | undefined,
  configKey: '',
  configValue: '',
  configName: '',
  description: '',
  status: 1 as 0 | 1
})

const pagination = reactive({
  page: 1,
  size: 20
})

const isEdit = computed(() => !!form.id)

const rules: FormRules = {
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' },
    { pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, message: '配置键只能包含字母、数字和下划线，且不能以数字开头', trigger: 'blur' }
  ],
  configName: [
    { required: true, message: '请输入配置名称', trigger: 'blur' }
  ]
}

const loadConfigList = async () => {
  try {
    loading.value = true
    const queryParams = {
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    
    const response = await configApi.list(queryParams)
    
    if (response && response.data) {
      const responseData = response.data
      if (responseData.data && Array.isArray(responseData.data)) {
        configList.value = responseData.data
        total.value = responseData.total || 0
      } else if (Array.isArray(responseData)) {
        configList.value = responseData
        total.value = responseData.length
      }
    }
  } catch (error) {
    console.error('加载配置列表失败:', error)
    ElMessage.error('加载配置列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadConfigList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    configKey: '',
    configName: '',
    status: undefined
  })
  handleSearch()
}

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    configKey: '',
    configValue: '',
    configName: '',
    description: '',
    status: 1
  })
  formDialogVisible.value = true
}

const handleEdit = (config: Config) => {
  Object.assign(form, {
    id: config.id,
    configKey: config.configKey,
    configValue: config.configValue,
    configName: config.configName,
    description: config.description,
    status: config.status
  })
  formDialogVisible.value = true
}

const handleDelete = async (config: Config) => {
  try {
    await configApi.delete(config.id)
    ElMessage.success('删除成功')
    loadConfigList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  try {
    const ids = selectedConfigs.value.map(c => c.id)
    await configApi.batchDelete(ids)
    ElMessage.success('批量删除成功')
    loadConfigList()
    selectedConfigs.value = []
  } catch (error) {
    ElMessage.error('批量删除失败')
  }
}

const handleStatusChange = async (config: Config) => {
  try {
    if (config.status === 1) {
      await configApi.enable(config.id)
      ElMessage.success('启用成功')
    } else {
      await configApi.disable(config.id)
      ElMessage.success('禁用成功')
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    config.status = config.status === 1 ? 0 : 1
  }
}

const handleRefreshCache = async () => {
  try {
    await configApi.refreshCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    ElMessage.error('缓存刷新失败')
  }
}

const checkConfigKey = async () => {
  if (!form.configKey || isEdit.value) return
  
  try {
    const response = await configApi.checkKey(form.configKey)
    if (response.data === false) {
      ElMessage.warning('配置键已存在')
    }
  } catch (error) {
    console.error('检查配置键失败:', error)
  }
}

const handleSelectionChange = (selection: Config[]) => {
  selectedConfigs.value = selection
}

const handleSizeChange = () => {
  pagination.page = 1
  loadConfigList()
}

const handleCurrentChange = () => {
  loadConfigList()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      configKey: form.configKey,
      configValue: form.configValue,
      configName: form.configName,
      description: form.description,
      status: form.status
    }

    if (isEdit.value && form.id) {
      await configApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await configApi.create(submitData)
      ElMessage.success('创建成功')
    }

    handleCloseDialog()
    loadConfigList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleCloseDialog = () => {
  formRef.value?.resetFields()
  formDialogVisible.value = false
}

onMounted(() => {
  loadConfigList()
})
</script>

<style lang="scss" scoped>
.form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>