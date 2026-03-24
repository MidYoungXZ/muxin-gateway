<template>
  <div class="service-node-management">
    <div class="page-header">
      <div class="header-left">
        <h1>服务节点管理</h1>
        <p>管理服务及其节点，节点通过负载均衡策略被路由调用</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAddService">
          <el-icon><Plus /></el-icon>
          新增服务
        </el-button>
      </div>
    </div>

    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" label-width="80px">
        <el-form-item label="服务名称">
          <el-input 
            v-model="searchForm.serviceName" 
            placeholder="请输入服务名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
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
        <div class="table-info">
          共 {{ serviceStats.length }} 个服务
        </div>
      </div>

      <el-table 
        :data="serviceStats" 
        v-loading="loading"
        stripe
        style="width: 100%"
        @expand-change="handleExpandChange"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <div class="expand-header">
                <span class="expand-title">{{ row.serviceName }} - 节点列表</span>
                <el-button type="primary" size="small" @click="handleAddNode(row.serviceName)">
                  <el-icon><Plus /></el-icon>
                  添加节点
                </el-button>
              </div>
              <el-table 
                :data="expandedNodes[row.serviceName]?.data || []"
                v-loading="expandLoading[row.serviceName]"
                stripe
                size="small"
              >
                <el-table-column prop="nodeId" label="节点ID" width="150" />
                <el-table-column prop="nodeName" label="节点名称" width="150" />
                <el-table-column label="地址:端口" width="180">
                  <template #default="{ row: node }">
                    {{ node.address }}:{{ node.port }}
                  </template>
                </el-table-column>
                <el-table-column prop="weight" label="权重" width="80" />
                <el-table-column label="备份" width="80">
                  <template #default="{ row: node }">
                    <el-tag v-if="node.backup" type="warning" size="small">是</el-tag>
                    <span v-else>否</span>
                  </template>
                </el-table-column>
                <el-table-column label="健康状态" width="100">
                  <template #default="{ row: node }">
                    <el-tag v-if="node.healthy" type="success" size="small">健康</el-tag>
                    <el-tag v-else-if="node.lastCheckResult === 0" type="danger" size="small">不健康</el-tag>
                    <el-tag v-else type="info" size="small">未知</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="{ row: node }">
                    <el-tag :type="getStatusTagType(node.status)" size="small">
                      {{ node.statusDesc }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="健康检查" width="120">
                  <template #default="{ row: node }">
                    <span v-if="node.healthCheckEnabled">
                      {{ node.healthCheckPath }}
                    </span>
                    <span v-else class="text-muted">未启用</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="{ row: node }">
                    <el-button type="primary" size="small" link @click="handleEditNode(node)">
                      编辑
                    </el-button>
                    <el-dropdown @command="(cmd: string) => handleNodeCommand(cmd, node)">
                      <el-button type="primary" size="small" link>
                        状态<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="enable" :disabled="node.status === 1">启用</el-dropdown-item>
                          <el-dropdown-item command="disable" :disabled="node.status === 0">禁用</el-dropdown-item>
                          <el-dropdown-item command="maintenance" :disabled="node.status === 2">维护中</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                    <el-popconfirm title="确定要删除该节点吗？" @confirm="handleDeleteNode(node)">
                      <template #reference>
                        <el-button type="danger" size="small" link>删除</el-button>
                      </template>
                    </el-popconfirm>
                  </template>
                </el-table-column>
              </el-table>
              <div class="expand-pagination" v-if="expandedNodes[row.serviceName]?.total > 0">
                <el-pagination
                  v-model:current-page="nodePagination[row.serviceName].page"
                  v-model:page-size="nodePagination[row.serviceName].size"
                  :total="expandedNodes[row.serviceName]?.total || 0"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  small
                  @size-change="loadNodesByService(row.serviceName)"
                  @current-change="loadNodesByService(row.serviceName)"
                />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="serviceName" label="服务名称" min-width="180">
          <template #default="{ row }">
            <span class="service-name">{{ row.serviceName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="节点数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.totalNodes }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="健康" width="100" align="center">
          <template #default="{ row }">
            <span class="health-stats">
              <span class="healthy">{{ row.healthyNodes }}</span>
              <span class="separator">/</span>
              <span class="unhealthy">{{ row.unhealthyNodes }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态分布" min-width="200">
          <template #default="{ row }">
            <div class="status-distribution">
              <el-tag v-if="row.enabledNodes > 0" type="success" size="small" class="status-tag">
                启用: {{ row.enabledNodes }}
              </el-tag>
              <el-tag v-if="row.disabledNodes > 0" type="danger" size="small" class="status-tag">
                禁用: {{ row.disabledNodes }}
              </el-tag>
              <el-tag v-if="row.maintenanceNodes > 0" type="warning" size="small" class="status-tag">
                维护: {{ row.maintenanceNodes }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleAddNode(row.serviceName)">
              添加节点
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑节点' : '新增节点'"
      width="650px"
      :close-on-click-modal="false"
      @close="handleCloseDialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="服务名称">
          <el-input :value="form.serviceName" disabled />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="节点ID" prop="nodeId">
              <el-input v-model="form.nodeId" placeholder="请输入节点ID" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="节点名称" prop="nodeName">
              <el-input v-model="form.nodeName" placeholder="请输入节点名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="节点地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入IP地址" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="端口" prop="port">
              <el-input-number v-model="form.port" :min="1" :max="65535" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权重" prop="weight">
              <el-input-number v-model="form.weight" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备份节点">
              <el-switch v-model="form.backup" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">健康检查配置</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="启用健康检查">
              <el-switch v-model="form.healthCheckEnabled" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检查间隔(秒)">
              <el-input-number v-model="form.healthCheckInterval" :min="1" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检查超时(秒)">
              <el-input-number v-model="form.healthCheckTimeout" :min="1" :max="60" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检查路径">
              <el-input v-model="form.healthCheckPath" placeholder="/health" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="期望状态码">
          <el-select v-model="form.healthCheckExpectedStatus" multiple filterable allow-create placeholder="选择或输入状态码" style="width: 100%">
            <el-option :value="200" label="200" />
            <el-option :value="201" label="201" />
            <el-option :value="204" label="204" />
            <el-option :value="301" label="301" />
            <el-option :value="302" label="302" />
          </el-select>
        </el-form-item>
        
        <el-divider content-position="left">高级配置</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大失败次数">
              <el-input-number v-model="form.maxFails" :min="1" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失败超时(秒)">
              <el-input-number v-model="form.failTimeout" :min="1" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="serviceDialogVisible"
      title="新增服务"
      width="500px"
      :close-on-click-modal="false"
      @close="handleCloseServiceDialog"
    >
      <el-form
        ref="serviceFormRef"
        :model="serviceForm"
        :rules="serviceRules"
        label-width="100px"
      >
        <el-form-item label="服务名称" prop="serviceName">
          <el-input v-model="serviceForm.serviceName" placeholder="请输入服务名称，如 user-service" />
        </el-form-item>
        <el-divider content-position="left">首个节点配置（可选）</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="节点名称">
              <el-input v-model="serviceForm.nodeName" placeholder="留空则自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="端口">
              <el-input-number v-model="serviceForm.port" :min="1" :max="65535" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="节点地址">
              <el-input v-model="serviceForm.address" placeholder="留空则使用 127.0.0.1" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseServiceDialog">取消</el-button>
        <el-button type="primary" :loading="serviceFormLoading" @click="handleServiceSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { nodesApi, type ServiceStats, type ServiceNode, type ServiceNodeCreateRequest } from '@/api/nodes'

const loading = ref(false)
const formLoading = ref(false)
const serviceFormLoading = ref(false)
const serviceStats = ref<ServiceStats[]>([])
const expandLoading = ref<Record<string, boolean>>({})
const expandedNodes = ref<Record<string, { data: ServiceNode[], total: number }>>({})
const nodePagination = ref<Record<string, { page: number, size: number }>>({})

const formDialogVisible = ref(false)
const serviceDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const serviceFormRef = ref<FormInstance>()
const isEdit = ref(false)

const searchForm = reactive({
  serviceName: ''
})

const serviceForm = reactive({
  serviceName: '',
  nodeName: '',
  address: '',
  port: 8080
})

const serviceRules: FormRules = {
  serviceName: [
    { required: true, message: '请输入服务名称', trigger: 'blur' },
    { max: 100, message: '服务名称长度不能超过100', trigger: 'blur' }
  ]
}

const form = reactive({
  id: undefined as number | undefined,
  nodeId: '',
  serviceName: '',
  nodeName: '',
  address: '',
  port: 8080,
  weight: 100,
  backup: false,
  healthCheckEnabled: true,
  healthCheckInterval: 30,
  healthCheckTimeout: 5,
  healthCheckPath: '/health',
  healthCheckExpectedStatus: [200, 201],
  maxFails: 3,
  failTimeout: 30
})

const rules: FormRules = {
  nodeId: [
    { required: true, message: '请输入节点ID', trigger: 'blur' },
    { max: 100, message: '节点ID长度不能超过100', trigger: 'blur' }
  ],
  nodeName: [
    { required: true, message: '请输入节点名称', trigger: 'blur' },
    { max: 100, message: '节点名称长度不能超过100', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入节点地址', trigger: 'blur' }
  ],
  port: [
    { required: true, message: '请输入端口', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入权重', trigger: 'blur' }
  ]
}

const loadServiceStats = async () => {
  try {
    loading.value = true
    const response = await nodesApi.getServiceStats(searchForm.serviceName)
    if (response && response.data) {
      serviceStats.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载服务列表失败：' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

const loadNodesByService = async (serviceName: string) => {
  const pagination = nodePagination.value[serviceName] || { page: 1, size: 20 }
  nodePagination.value[serviceName] = pagination
  
  try {
    expandLoading.value[serviceName] = true
    const response = await nodesApi.getNodesByService(serviceName, pagination.page, pagination.size)
    if (response && response.data) {
      expandedNodes.value[serviceName] = {
        data: response.data.data || [],
        total: response.data.total || 0
      }
    }
  } catch (error) {
    ElMessage.error('加载节点列表失败')
  } finally {
    expandLoading.value[serviceName] = false
  }
}

const handleExpandChange = (row: ServiceStats, expandedRows: ServiceStats[]) => {
  const isExpanded = expandedRows.some(r => r.serviceName === row.serviceName)
  if (isExpanded && !expandedNodes.value[row.serviceName]) {
    nodePagination.value[row.serviceName] = { page: 1, size: 20 }
    loadNodesByService(row.serviceName)
  }
}

const handleSearch = () => {
  loadServiceStats()
}

const handleReset = () => {
  searchForm.serviceName = ''
  handleSearch()
}

const getStatusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

const handleAddNode = (serviceName: string) => {
  isEdit.value = false
  Object.assign(form, {
    id: undefined,
    nodeId: '',
    serviceName,
    nodeName: '',
    address: '',
    port: 8080,
    weight: 100,
    backup: false,
    healthCheckEnabled: true,
    healthCheckInterval: 30,
    healthCheckTimeout: 5,
    healthCheckPath: '/health',
    healthCheckExpectedStatus: [200, 201],
    maxFails: 3,
    failTimeout: 30
  })
  formDialogVisible.value = true
}

const handleEditNode = (node: ServiceNode) => {
  isEdit.value = true
  Object.assign(form, {
    id: node.id,
    nodeId: node.nodeId,
    serviceName: node.serviceName,
    nodeName: node.nodeName,
    address: node.address,
    port: node.port,
    weight: node.weight,
    backup: node.backup,
    healthCheckEnabled: node.healthCheckEnabled,
    healthCheckInterval: node.healthCheckInterval,
    healthCheckTimeout: node.healthCheckTimeout,
    healthCheckPath: node.healthCheckPath,
    healthCheckExpectedStatus: node.healthCheckExpectedStatus || [200, 201],
    maxFails: node.maxFails,
    failTimeout: node.failTimeout
  })
  formDialogVisible.value = true
}

const handleDeleteNode = async (node: ServiceNode) => {
  try {
    await nodesApi.delete(node.id)
    ElMessage.success('删除成功')
    loadNodesByService(node.serviceName)
    loadServiceStats()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleNodeCommand = async (command: string, node: ServiceNode) => {
  try {
    if (command === 'enable') {
      await nodesApi.enable(node.id)
      ElMessage.success('已启用')
    } else if (command === 'disable') {
      await nodesApi.disable(node.id)
      ElMessage.success('已禁用')
    } else if (command === 'maintenance') {
      await nodesApi.maintenance(node.id)
      ElMessage.success('已设为维护中')
    }
    loadNodesByService(node.serviceName)
    loadServiceStats()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    formLoading.value = true
    
    if (isEdit.value && form.id) {
      await nodesApi.update(form.id, {
        nodeName: form.nodeName,
        address: form.address,
        port: form.port,
        weight: form.weight,
        backup: form.backup,
        healthCheckEnabled: form.healthCheckEnabled,
        healthCheckInterval: form.healthCheckInterval,
        healthCheckTimeout: form.healthCheckTimeout,
        healthCheckPath: form.healthCheckPath,
        healthCheckExpectedStatus: form.healthCheckExpectedStatus,
        maxFails: form.maxFails,
        failTimeout: form.failTimeout
      })
      ElMessage.success('更新成功')
    } else {
      await nodesApi.create({
        nodeId: form.nodeId,
        serviceName: form.serviceName,
        nodeName: form.nodeName,
        address: form.address,
        port: form.port,
        weight: form.weight,
        backup: form.backup,
        healthCheckEnabled: form.healthCheckEnabled,
        healthCheckInterval: form.healthCheckInterval,
        healthCheckTimeout: form.healthCheckTimeout,
        healthCheckPath: form.healthCheckPath,
        healthCheckExpectedStatus: form.healthCheckExpectedStatus,
        maxFails: form.maxFails,
        failTimeout: form.failTimeout
      })
      ElMessage.success('创建成功')
    }
    
    handleCloseDialog()
    loadNodesByService(form.serviceName)
    loadServiceStats()
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

const handleAddService = () => {
  Object.assign(serviceForm, {
    serviceName: '',
    nodeName: '',
    address: '',
    port: 8080
  })
  serviceDialogVisible.value = true
}

const handleServiceSubmit = async () => {
  if (!serviceFormRef.value) return
  
  try {
    await serviceFormRef.value.validate()
    serviceFormLoading.value = true
    
    await nodesApi.createService({
      serviceName: serviceForm.serviceName,
      nodeName: serviceForm.nodeName || undefined,
      address: serviceForm.address || undefined,
      port: serviceForm.port
    })
    ElMessage.success('创建服务成功')
    handleCloseServiceDialog()
    loadServiceStats()
  } catch (error) {
    ElMessage.error('创建服务失败：' + (error as Error).message)
  } finally {
    serviceFormLoading.value = false
  }
}

const handleCloseServiceDialog = () => {
  serviceFormRef.value?.resetFields()
  serviceDialogVisible.value = false
}

onMounted(() => {
  loadServiceStats()
})
</script>

<style lang="scss" scoped>
.service-node-management {
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
      display: flex;
      gap: 12px;
    }
  }
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .table-card {
    .table-header {
      margin-bottom: 16px;
      
      .table-info {
        color: var(--text-secondary);
        font-size: 14px;
      }
    }
    
    .service-name {
      font-weight: 500;
    }
    
    .health-stats {
      .healthy {
        color: var(--el-color-success);
        font-weight: 500;
      }
      
      .unhealthy {
        color: var(--el-color-danger);
        font-weight: 500;
      }
      
      .separator {
        margin: 0 4px;
        color: var(--el-text-color-secondary);
      }
    }
    
    .status-distribution {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }
  
  .expand-content {
    padding: 16px 48px;
    background: var(--el-bg-color-page);
    
    .expand-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .expand-title {
        font-size: 14px;
        font-weight: 500;
      }
    }
    
    .expand-pagination {
      margin-top: 12px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  .text-muted {
    color: var(--el-text-color-secondary);
  }
}
</style>