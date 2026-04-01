<template>
  <div class="page-list-container">
    <div class="page-title-bar">
      <span class="title">服务</span>
      <el-button type="primary" @click="handleAddService">
        <el-icon><Plus /></el-icon>
        新增服务
      </el-button>
    </div>

    <div class="search-bar">
      <el-input 
        v-model="searchForm.serviceName" 
        placeholder="服务名称"
        clearable
        @keyup.enter="handleSearch"
      />
      <div class="search-actions">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="table-wrapper">
      <div class="table-toolbar">
        <span class="toolbar-right">共 {{ serviceStats.length }} 个服务</span>
      </div>

      <el-table 
        :data="serviceStats" 
        v-loading="loading"
        stripe
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
                    <div class="node-actions">
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
                    </div>
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleAddNode(row.serviceName)">
              添加节点
            </el-button>
            <el-button type="danger" size="small" link @click="handleDeleteService(row)">
              删除服务
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

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
      width="800px"
      :close-on-click-modal="false"
      @close="handleCloseServiceDialog"
    >
      <el-form
        ref="serviceFormRef"
        :model="serviceForm"
        :rules="serviceRules"
        label-width="120px"
      >
        <el-form-item label="服务名称" prop="serviceName">
          <el-input v-model="serviceForm.serviceName" placeholder="请输入服务名称，如 user-service" />
        </el-form-item>
        
        <el-form-item label="创建方式" prop="createMode">
          <el-radio-group v-model="serviceForm.createMode">
            <el-radio label="MANUAL">手动输入</el-radio>
            <el-radio label="DISCOVERY">注册中心发现</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="serviceForm.createMode === 'MANUAL'">
          <el-divider content-position="left">节点配置（可添加多个节点，或一个不添加）</el-divider>
          
          <div v-for="(node, index) in serviceForm.nodes" :key="index" class="node-item">
            <el-card shadow="never">
              <template #header>
                <div class="node-item-header">
                  <span>节点 {{ index + 1 }}</span>
                  <el-button type="danger" size="small" link @click="removeNode(index)">删除</el-button>
                </div>
              </template>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="节点名称">
                    <el-input v-model="node.nodeName" placeholder="留空则自动生成" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="node.port" :min="1" :max="65535" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="16">
                  <el-form-item label="节点地址">
                    <el-input v-model="node.address" placeholder="留空则使用 127.0.0.1" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="权重">
                    <el-input-number v-model="node.weight" :min="1" :max="100" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-card>
          </div>
          
          <el-button type="primary" plain @click="addNode" style="margin-top: 10px;">
            <el-icon><Plus /></el-icon>
            添加节点
          </el-button>
        </template>

        <template v-else-if="serviceForm.createMode === 'DISCOVERY'">
          <el-divider content-position="left">注册中心配置</el-divider>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="注册中心类型">
                <el-select v-model="serviceForm.discoveryConfig.registryType" style="width: 100%">
                  <el-option value="NACOS" label="Nacos" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="注册中心地址" required>
                <el-input v-model="serviceForm.discoveryConfig.serverAddr" placeholder="如: 127.0.0.1:8848" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="命名空间">
                <el-input v-model="serviceForm.discoveryConfig.namespace" placeholder="可选" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="分组">
                <el-input v-model="serviceForm.discoveryConfig.group" placeholder="默认: DEFAULT_GROUP" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名">
                <el-input v-model="serviceForm.discoveryConfig.username" placeholder="可选" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="密码">
                <el-input v-model="serviceForm.discoveryConfig.password" type="password" placeholder="可选" show-password />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item>
            <el-button type="primary" :loading="testLoading" @click="handleTestDiscovery">
              检测服务
            </el-button>
          </el-form-item>
          
          <template v-if="discoveredNodes.length > 0">
            <el-divider content-position="left">发现的节点（只读）</el-divider>
            <el-table :data="discoveredNodes" stripe size="small">
              <el-table-column prop="address" label="地址" />
              <el-table-column prop="port" label="端口" width="100" />
              <el-table-column prop="weight" label="权重" width="80" />
              <el-table-column label="健康状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.healthy ? 'success' : 'danger'" size="small">
                    {{ row.healthy ? '健康' : '不健康' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </template>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  nodesApi, 
  type ServiceStats, 
  type ServiceNode, 
  type ServiceNodeCreateRequest,
  type ServiceCreateRequest,
  type ServiceNodeDTO,
  type DiscoveryConfig,
  type DiscoveredNode,
  type RouteSimple
} from '@/api/nodes'

const router = useRouter()

const loading = ref(false)
const formLoading = ref(false)
const serviceFormLoading = ref(false)
const testLoading = ref(false)
const serviceStats = ref<ServiceStats[]>([])
const expandLoading = ref<Record<string, boolean>>({})
const expandedNodes = ref<Record<string, { data: ServiceNode[], total: number }>>({})
const nodePagination = ref<Record<string, { page: number, size: number }>>({})

const formDialogVisible = ref(false)
const serviceDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const serviceFormRef = ref<FormInstance>()
const isEdit = ref(false)
const discoveredNodes = ref<DiscoveredNode[]>([])

const searchForm = reactive({
  serviceName: ''
})

const emptyNode: ServiceNodeDTO = {
  nodeName: '',
  address: '',
  port: 8080,
  weight: 100
}

const serviceForm = reactive({
  serviceName: '',
  createMode: 'MANUAL',
  nodes: [{ ...emptyNode }] as ServiceNodeDTO[],
  discoveryConfig: {
    registryType: 'NACOS',
    serverAddr: '',
    namespace: '',
    username: '',
    password: '',
    group: ''
  } as DiscoveryConfig
})

const serviceRules: FormRules = {
  serviceName: [
    { required: true, message: '请输入服务名称', trigger: 'blur' },
    { max: 100, message: '服务名称长度不能超过100', trigger: 'blur' }
  ],
  createMode: [
    { required: true, message: '请选择创建方式', trigger: 'change' }
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
    createMode: 'MANUAL',
    nodes: [{ ...emptyNode }],
    discoveryConfig: {
      registryType: 'NACOS',
      serverAddr: '',
      namespace: '',
      username: '',
      password: '',
      group: ''
    }
  })
  discoveredNodes.value = []
  serviceDialogVisible.value = true
}

const addNode = () => {
  serviceForm.nodes.push({ ...emptyNode })
}

const removeNode = (index: number) => {
  serviceForm.nodes.splice(index, 1)
}

const handleTestDiscovery = async () => {
  if (!serviceForm.discoveryConfig.serverAddr) {
    ElMessage.warning('请输入注册中心地址')
    return
  }
  
  try {
    testLoading.value = true
    
    const testResult = await nodesApi.testDiscoveryConnection(serviceForm.discoveryConfig)
    
    if (testResult?.data?.success) {
      const discoverResult = await nodesApi.discoverNodes({
        registryType: serviceForm.discoveryConfig.registryType,
        serverAddr: serviceForm.discoveryConfig.serverAddr,
        serviceName: serviceForm.serviceName,
        namespace: serviceForm.discoveryConfig.namespace,
        username: serviceForm.discoveryConfig.username,
        password: serviceForm.discoveryConfig.password,
        group: serviceForm.discoveryConfig.group
      })
      
      if (discoverResult?.data && discoverResult.data.length > 0) {
        discoveredNodes.value = discoverResult.data
        ElMessage.success(`发现 ${discoverResult.data.length} 个节点`)
      } else {
        discoveredNodes.value = []
        ElMessage.warning('未发现任何节点')
      }
    } else {
      ElMessage.error(testResult?.data?.message || '连接失败')
    }
  } catch (error) {
    ElMessage.error('检测失败：' + (error as Error).message)
  } finally {
    testLoading.value = false
  }
}

const handleServiceSubmit = async () => {
  if (!serviceFormRef.value) return
  
  try {
    await serviceFormRef.value.validate()
    
    if (serviceForm.createMode === 'DISCOVERY' && !serviceForm.discoveryConfig.serverAddr) {
      ElMessage.warning('请输入注册中心地址')
      return
    }
    
    serviceFormLoading.value = true
    
    const request: ServiceCreateRequest = {
      serviceName: serviceForm.serviceName,
      createMode: serviceForm.createMode
    }
    
    if (serviceForm.createMode === 'MANUAL') {
      request.nodes = serviceForm.nodes.length > 0 ? serviceForm.nodes : undefined
    } else {
      request.discoveryConfig = serviceForm.discoveryConfig
    }
    
    await nodesApi.createService(request)
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
  serviceFormRef.value?.clearValidate()
  serviceDialogVisible.value = false
  discoveredNodes.value = []
}

const handleDeleteService = async (row: ServiceStats) => {
  try {
    const response = await nodesApi.getServiceRoutes(row.serviceName)
    const routes = response?.data || []
    
    if (routes.length > 0) {
      const routeList = routes.map(r => `• ${r.routeName} (${r.routeId})`).join('\n')
      await ElMessageBox.confirm(
        `该服务被以下路由引用，无法删除：\n\n${routeList}\n\n请先修改或删除相关路由。`,
        '服务被引用',
        {
          confirmButtonText: '查看路由',
          cancelButtonText: '关闭',
          type: 'warning',
          distinguishCancelAndClose: true
        }
      )
      if (routes.length === 1) {
        router.push(`/routes/list?id=${routes[0].id}`)
      } else {
        router.push('/routes/list')
      }
      return
    }
    
    await ElMessageBox.confirm(
      `确定要删除服务 "${row.serviceName}" 吗？\n这将删除该服务下的所有节点（共 ${row.totalNodes} 个）。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await nodesApi.deleteService(row.serviceName)
    ElMessage.success('删除服务成功')
    loadServiceStats()
  } catch (error: unknown) {
    const err = error as { message?: string }
    if (err.message !== 'cancel' && err.message !== 'close') {
      ElMessage.error('删除服务失败：' + (err.message || '未知错误'))
    }
  }
}

onMounted(() => {
  loadServiceStats()
})
</script>

<style lang="scss" scoped>
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

.expand-content {
  padding: 16px 48px;
  background: var(--bg-secondary);
  
  .expand-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .expand-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-primary);
    }
  }
  
  .expand-pagination {
    margin-top: 12px;
    display: flex;
    justify-content: flex-end;
  }
}

.text-muted {
  color: var(--text-secondary);
}

.node-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.node-item {
  margin-bottom: 12px;
  
  .node-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>