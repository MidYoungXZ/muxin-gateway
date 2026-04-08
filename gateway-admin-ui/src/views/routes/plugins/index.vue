<template>
  <div class="page-list-container">
    <div class="page-title-bar">
      <span class="title">插件管理</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增插件
      </el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="filterType" placeholder="插件类型" clearable style="width: 150px">
        <el-option label="全部" value="" />
        <el-option label="请求处理" value="FILTER" />
      </el-select>
      <el-input
        v-model="searchName"
        placeholder="插件名称"
        clearable
        @keyup.enter="loadPlugins"
        style="width: 200px"
      />
      <div class="search-actions">
        <el-button type="primary" @click="loadPlugins">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="table-wrapper">
      <el-table :data="pluginList" v-loading="loading" stripe>
        <el-table-column prop="pluginName" label="插件名称" min-width="140" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="defaultPriority" label="默认优先级" width="100" />
        <el-table-column label="系统内置" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isSystem ? 'info' : 'success'" size="small">
              {{ row.isSystem ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
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
                type="danger" 
                size="small" 
                link 
                @click="handleDeleteClick(row)"
                :disabled="row.isSystem"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalPlugins"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑插件' : '新增插件'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="插件名称" prop="pluginName">
          <el-input v-model="form.pluginName" placeholder="请输入插件名称" />
        </el-form-item>
        <el-form-item label="插件类型" prop="pluginType">
          <el-select v-model="form.pluginType" placeholder="请选择类型" style="width: 100%">
            <el-option label="请求处理 (FILTER)" value="FILTER" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="默认优先级">
          <el-input-number v-model="form.defaultPriority" :min="1" :max="99999" style="width: 100%" />
          <div class="field-tip">数值越大越先执行</div>
        </el-form-item>
        <el-form-item label="执行阶段">
          <el-select v-model="form.phase" placeholder="请选择阶段" style="width: 100%">
            <el-option label="前置处理 (FILTER_PRE)" value="FILTER_PRE" />
            <el-option label="后置处理 (FILTER_POST)" value="FILTER_POST" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="插件详情" width="800px" class="plugin-detail-dialog">
      <div class="plugin-detail-content">
        <!-- 基本信息卡片 -->
        <el-card shadow="never" class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><Document /></el-icon>
              <span>基本信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="插件名称">
              <span class="plugin-name">{{ currentPlugin?.pluginName }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentPlugin?.enabled ? 'success' : 'danger'" size="small">
                {{ currentPlugin?.enabled ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="插件类型">
              <el-tag size="small">{{ currentPlugin?.pluginType }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="执行阶段">
              <el-tag :type="currentPlugin?.phase === 'FILTER_PRE' ? 'primary' : 'warning'" size="small">
                {{ currentPlugin?.phase === 'FILTER_PRE' ? '前置处理' : '后置处理' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ currentPlugin?.description || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="默认优先级">
              <el-tag type="info" size="small">{{ currentPlugin?.defaultPriority }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="系统内置">
              <el-tag :type="currentPlugin?.isSystem ? 'info' : 'success'" size="small">
                {{ currentPlugin?.isSystem ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 配置 Schema 卡片 -->
        <el-card v-if="currentPlugin?.schema" shadow="never" class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><Setting /></el-icon>
              <span>配置 Schema</span>
              <el-button 
                size="small" 
                text 
                @click="copyToClipboard(JSON.stringify(currentPlugin.schema, null, 2), 'Schema')"
                style="margin-left: auto"
              >
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
            </div>
          </template>
          <div class="code-block-wrapper">
            <pre class="code-block"><code>{{ JSON.stringify(currentPlugin.schema, null, 2) }}</code></pre>
          </div>
        </el-card>

        <!-- 默认配置卡片 -->
        <el-card v-if="currentPlugin?.defaultConfig" shadow="never" class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><Setting /></el-icon>
              <span>默认配置</span>
              <el-button 
                size="small" 
                text 
                @click="copyToClipboard(JSON.stringify(currentPlugin.defaultConfig, null, 2), '默认配置')"
                style="margin-left: auto"
              >
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
            </div>
          </template>
          <div class="code-block-wrapper">
            <pre class="code-block"><code>{{ JSON.stringify(currentPlugin.defaultConfig, null, 2) }}</code></pre>
          </div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Document, Setting, CopyDocument } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { pluginsApi, type PluginInfo } from '@/api/plugins'

const loading = ref(false)
const formLoading = ref(false)
const pluginList = ref<PluginInfo[]>([])
const filterType = ref('')
const searchName = ref('')

const currentPage = ref(1)
const pageSize = ref(10)
const totalPlugins = ref(0)

const formDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const currentPlugin = ref<PluginInfo | null>(null)
const isEdit = ref(false)

const form = reactive({
  id: undefined as number | undefined,
  pluginName: '',
  pluginType: 'FILTER',
  description: '',
  defaultPriority: 5000,
  phase: 'FILTER_PRE',
  enabled: true
})

const rules: FormRules = {
  pluginName: [{ required: true, message: '请输入插件名称', trigger: 'blur' }],
  pluginType: [{ required: true, message: '请选择插件类型', trigger: 'change' }]
}

const loadPlugins = async () => {
  try {
    loading.value = true
    const response = await pluginsApi.list({ 
      type: filterType.value || undefined,
      pluginName: searchName.value || undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (response?.data) {
      pluginList.value = response.data.data || []
      totalPlugins.value = response.data.total || 0
    }
  } catch (error) {
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  filterType.value = ''
  searchName.value = ''
  currentPage.value = 1
  loadPlugins()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadPlugins()
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadPlugins()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: undefined,
    pluginName: '',
    pluginType: 'FILTER',
    description: '',
    defaultPriority: 5000,
    phase: 'FILTER_PRE',
    enabled: true
  })
  formDialogVisible.value = true
}

const handleView = async (plugin: PluginInfo) => {
  currentPlugin.value = plugin
  detailDialogVisible.value = true
}

const handleEdit = (plugin: PluginInfo) => {
  isEdit.value = true
  Object.assign(form, {
    id: plugin.id,
    pluginName: plugin.pluginName,
    pluginType: plugin.pluginType,
    description: plugin.description,
    defaultPriority: plugin.defaultPriority,
    phase: plugin.phase,
    enabled: plugin.enabled
  })
  formDialogVisible.value = true
}

const handleDeleteClick = async (plugin: PluginInfo) => {
  try {
    await ElMessageBox.confirm(`确定要删除插件"${plugin.pluginName}"吗？`, '删除确认', {
      type: 'warning'
    })
    await pluginsApi.delete(plugin.id)
    ElMessage.success('删除成功')
    loadPlugins()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDelete = async (plugin: PluginInfo) => {
  try {
    await pluginsApi.delete(plugin.id)
    ElMessage.success('删除成功')
    loadPlugins()
  } catch (error) {
  }
}

const handleStatusChange = async (plugin: PluginInfo) => {
  try {
    if (plugin.enabled) {
      await pluginsApi.enable(plugin.id)
      ElMessage.success('启用成功')
    } else {
      await pluginsApi.disable(plugin.id)
      ElMessage.success('禁用成功')
    }
  } catch (error) {
    plugin.enabled = !plugin.enabled
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    formLoading.value = true
    
    if (isEdit.value && form.id) {
      await pluginsApi.update(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await pluginsApi.create(form)
      ElMessage.success('创建成功')
    }
    
    formDialogVisible.value = false
    loadPlugins()
  } catch (error) {
  } finally {
    formLoading.value = false
  }
}

onMounted(() => {
  loadPlugins()
})

const copyToClipboard = async (text: string, name: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(`${name}已复制到剪贴板`)
  } catch (error) {
    ElMessage.error('复制失败')
  }
}
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--border-primary);
  flex-shrink: 0;
  background: var(--bg-secondary);
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
}

.table-actions {
  display: flex;
  gap: 4px;
  align-items: center;
  white-space: nowrap;
}

.field-tip {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.config-json {
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 12px;
  font-family: Monaco, Menlo, monospace;
  font-size: 12px;
  max-height: 200px;
  overflow: auto;
  margin: 0;
}

.plugin-detail-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.plugin-detail-content {
  .detail-card {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    :deep(.el-card__header) {
      padding: 12px 16px;
      border-bottom: 1px solid var(--border-primary);
    }
    
    :deep(.el-card__body) {
      padding: 16px;
    }
  }
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
    
    .header-icon {
      font-size: 18px;
      color: var(--primary-color);
    }
  }
  
  .plugin-name {
    font-weight: 600;
    font-size: 15px;
  }
  
  .code-block-wrapper {
    max-height: 400px;
    overflow: auto;
  }
  
  .code-block {
    background: var(--bg-tertiary);
    border: 1px solid var(--border-primary);
    border-radius: var(--radius-md);
    padding: 16px;
    margin: 0;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 13px;
    line-height: 1.6;
    color: var(--text-primary);
    overflow-x: auto;
    white-space: pre;
    
    code {
      font-family: inherit;
      color: inherit;
    }
    
    &::-webkit-scrollbar {
      width: 6px;
      height: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    
    &::-webkit-scrollbar-thumb {
      background: var(--border-secondary);
      border-radius: 3px;
      
      &:hover {
        background: var(--border-hover);
      }
    }
  }
}

// 暗色模式优化
.dark {
  .plugin-detail-content {
    .code-block {
      background: #1a1a1a;
      border-color: #2a2a2a;
      color: #c4b5fd;
    }
  }
}
</style>