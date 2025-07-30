<template>
  <div class="department-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>部门管理</h1>
        <p>管理组织部门结构，支持树形展示和层级管理</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增部门
        </el-button>
        <el-button :icon="Refresh" @click="loadDeptTree">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="部门名称">
          <el-input
            v-model="searchForm.deptName"
            placeholder="请输入部门名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 部门树 -->
    <el-card class="tree-card" shadow="never">
      <div class="tree-header">
        <div class="tree-actions">
          <span class="tree-info">
            共 {{ deptCount }} 个部门
          </span>
        </div>
        <div class="tree-controls">
          <el-button text @click="expandAll">
            <el-icon><CaretBottom /></el-icon>
            展开全部
          </el-button>
          <el-button text @click="collapseAll">
            <el-icon><CaretRight /></el-icon>
            收起全部
          </el-button>
        </div>
      </div>

      <el-tree
        ref="deptTreeRef"
        v-loading="loading"
        :data="deptTreeData"
        :props="treeProps"
        node-key="id"
        :expand-on-click-node="false"
        :default-expand-all="true"
        draggable
        @node-drop="handleNodeDrop"
        @allow-drop="allowDrop"
      >
        <template #default="{ node, data }">
          <div class="dept-node">
            <div class="dept-info">
              <el-icon class="dept-icon">
                <OfficeBuilding />
              </el-icon>
              <span class="dept-name">{{ data.deptName }}</span>
              <el-tag 
                v-if="data.status === 0" 
                type="danger" 
                size="small"
                style="margin-left: 8px;"
              >
                禁用
              </el-tag>
              <span v-if="data.leader" class="dept-leader">
                (负责人: {{ data.leader }})
              </span>
            </div>
            <div class="dept-actions">
              <el-button
                type="primary"
                size="small"
                text
                :icon="Plus"
                @click="handleAddChild(data)"
              >
                添加
              </el-button>
              <el-button
                type="warning"
                size="small"
                text
                :icon="Edit"
                @click="handleEdit(data)"
              >
                编辑
              </el-button>
              <el-switch
                v-model="data.status"
                :active-value="1"
                :inactive-value="0"
                size="small"
                style="margin: 0 8px;"
                @change="handleStatusChange(data)"
              />
              <el-button
                type="danger"
                size="small"
                text
                :icon="Delete"
                @click="handleDelete(data)"
              >
                删除
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 部门表单对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑部门' : '新增部门'"
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="父部门" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="parentDeptOptions"
                :props="{ label: 'deptName', value: 'id' }"
                check-strictly
                placeholder="请选择父部门"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门名称" prop="deptName">
              <el-input
                v-model="form.deptName"
                placeholder="请输入部门名称"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number
                v-model="form.orderNum"
                :min="0"
                :max="999"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="leader">
              <el-input
                v-model="form.leader"
                placeholder="请输入负责人"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="请输入联系电话"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="form.email"
                placeholder="请输入邮箱"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="部门状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  Plus, 
  Delete, 
  Search, 
  Refresh, 
  Edit, 
  OfficeBuilding,
  CaretBottom,
  CaretRight
} from '@element-plus/icons-vue'
import { departmentApi, type Department } from '@/api/departments'

// 数据定义
const loading = ref(false)
const formLoading = ref(false)
const deptTreeData = ref<Department[]>([])
const deptCount = ref(0)

// 表单和对话框
const formDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const deptTreeRef = ref()

// 搜索表单
const searchForm = reactive({
  deptName: '',
  status: undefined
})

// 部门表单
const form = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  deptName: '',
  orderNum: 0,
  leader: '',
  phone: '',
  email: '',
  status: 1
})

// 父部门选项
const parentDeptOptions = ref<Department[]>([])

// 计算属性
const isEdit = computed(() => !!form.id)

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'deptName'
}

// 表单验证规则
const rules: FormRules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 30, message: '部门名称长度在2-30个字符', trigger: 'blur' }
  ],
  orderNum: [
    { required: true, message: '请输入显示排序', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 加载部门树
const loadDeptTree = async () => {
  try {
    loading.value = true
    console.log('🔄 开始加载部门树...')
    
    const response = await departmentApi.getTree()
    console.log('📨 API响应:', response)
    
    if (response && response.data) {
      deptTreeData.value = response.data
      deptCount.value = countDepts(response.data)
      console.log('✅ 部门树加载成功:', deptCount.value, '个部门')
    } else {
      console.warn('⚠️ 部门数据为空')
      deptTreeData.value = []
      deptCount.value = 0
    }
  } catch (error) {
    console.error('❌ 加载部门树失败:', error)
    ElMessage.error('加载部门树失败')
    
    // 模拟数据
    const mockDepts: Department[] = [
      {
        id: 1,
        parentId: 0,
        deptName: '总公司',
        orderNum: 1,
        leader: '张总',
        phone: '13800138000',
        email: 'ceo@company.com',
        status: 1,
        createTime: '2024-01-01 10:00:00',
        updateTime: '2024-01-01 10:00:00',
        children: [
          {
            id: 2,
            parentId: 1,
            deptName: '研发部',
            orderNum: 1,
            leader: '李经理',
            phone: '13800138001',
            email: 'dev@company.com',
            status: 1,
            createTime: '2024-01-02 10:00:00',
            updateTime: '2024-01-02 10:00:00'
          },
          {
            id: 3,
            parentId: 1,
            deptName: '市场部',
            orderNum: 2,
            leader: '王经理',
            phone: '13800138002',
            email: 'market@company.com',
            status: 1,
            createTime: '2024-01-03 10:00:00',
            updateTime: '2024-01-03 10:00:00'
          }
        ]
      }
    ]
    deptTreeData.value = mockDepts
    deptCount.value = countDepts(mockDepts)
    ElMessage.warning('后端服务异常，当前显示模拟数据仅供界面测试')
  } finally {
    loading.value = false
  }
}

// 统计部门数量
const countDepts = (depts: Department[]): number => {
  let count = 0
  const traverse = (nodes: Department[]) => {
    nodes.forEach(node => {
      count++
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    })
  }
  traverse(depts)
  return count
}

// 搜索
const handleSearch = () => {
  // TODO: 实现搜索逻辑
  ElMessage.info('搜索功能开发中...')
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    deptName: '',
    status: undefined
  })
  loadDeptTree()
}

// 展开全部
const expandAll = () => {
  const keys = getAllNodeKeys(deptTreeData.value)
  deptTreeRef.value?.setExpandedKeys(keys)
}

// 收起全部
const collapseAll = () => {
  deptTreeRef.value?.setExpandedKeys([])
}

// 获取所有节点key
const getAllNodeKeys = (nodes: Department[]): number[] => {
  const keys: number[] = []
  const traverse = (list: Department[]) => {
    list.forEach(node => {
      keys.push(node.id)
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    })
  }
  traverse(nodes)
  return keys
}

// 新增部门
const handleAdd = () => {
  resetForm()
  form.parentId = 0
  parentDeptOptions.value = buildParentOptions(deptTreeData.value)
  formDialogVisible.value = true
}

// 新增子部门
const handleAddChild = (parent: Department) => {
  resetForm()
  form.parentId = parent.id
  parentDeptOptions.value = buildParentOptions(deptTreeData.value)
  formDialogVisible.value = true
}

// 编辑部门
const handleEdit = (dept: Department) => {
  resetForm()
  Object.assign(form, {
    id: dept.id,
    parentId: dept.parentId,
    deptName: dept.deptName,
    orderNum: dept.orderNum,
    leader: dept.leader,
    phone: dept.phone,
    email: dept.email,
    status: dept.status
  })
  parentDeptOptions.value = buildParentOptions(deptTreeData.value, dept.id)
  formDialogVisible.value = true
}

// 删除部门
const handleDelete = async (dept: Department) => {
  if (dept.children && dept.children.length > 0) {
    ElMessage.warning('存在子部门，不允许删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除部门"${dept.deptName}"吗？`, '删除确认', {
      type: 'warning'
    })
    
    console.log('📤 删除部门:', dept.id)
    await departmentApi.delete(dept.id)
    ElMessage.success('删除成功')
    loadDeptTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ 删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (dept: Department) => {
  try {
    if (dept.status === 1) {
      await departmentApi.enable(dept.id)
      ElMessage.success('启用成功')
    } else {
      await departmentApi.disable(dept.id)
      ElMessage.success('禁用成功')
    }
  } catch (error) {
    console.error('❌ 状态更新失败:', error)
    ElMessage.error('状态更新失败')
    // 恢复原状态
    dept.status = dept.status === 1 ? 0 : 1
  }
}

// 拖拽移动部门
const handleNodeDrop = async (dragNode: any, dropNode: any, dropType: string) => {
  try {
    const dragDeptId = dragNode.data.id
    let targetParentId = 0
    
    if (dropType === 'inner') {
      targetParentId = dropNode.data.id
    } else {
      targetParentId = dropNode.data.parentId
    }
    
    console.log('📤 移动部门:', dragDeptId, '->', targetParentId)
    await departmentApi.move(dragDeptId, targetParentId)
    ElMessage.success('移动成功')
    loadDeptTree()
  } catch (error) {
    console.error('❌ 移动失败:', error)
    ElMessage.error('移动失败')
    loadDeptTree() // 重新加载恢复原状态
  }
}

// 允许拖拽
const allowDrop = (dragNode: any, dropNode: any, type: string) => {
  // 不允许拖拽到自己的子节点
  if (type === 'inner') {
    return !isDescendant(dragNode.data.id, dropNode.data.id)
  }
  return true
}

// 检查是否为子孙节点
const isDescendant = (ancestorId: number, nodeId: number): boolean => {
  const findNode = (nodes: Department[], id: number): Department | null => {
    for (const node of nodes) {
      if (node.id === id) return node
      if (node.children) {
        const found = findNode(node.children, id)
        if (found) return found
      }
    }
    return null
  }
  
  const checkDescendant = (node: Department, targetId: number): boolean => {
    if (!node.children) return false
    for (const child of node.children) {
      if (child.id === targetId) return true
      if (checkDescendant(child, targetId)) return true
    }
    return false
  }
  
  const ancestorNode = findNode(deptTreeData.value, ancestorId)
  return ancestorNode ? checkDescendant(ancestorNode, nodeId) : false
}

// 构建父部门选项（排除自己和子部门）
const buildParentOptions = (depts: Department[], excludeId?: number): Department[] => {
  const options: Department[] = [
    { id: 0, parentId: -1, deptName: '根部门', orderNum: 0, leader: '', phone: '', email: '', status: 1, createTime: '', updateTime: '' }
  ]
  
  const traverse = (nodes: Department[]) => {
    nodes.forEach(node => {
      if (excludeId && (node.id === excludeId || isDescendant(excludeId, node.id))) {
        return
      }
      options.push({
        ...node,
        children: undefined // 不需要子节点信息
      })
      if (node.children) {
        traverse(node.children)
      }
    })
  }
  
  traverse(depts)
  return options
}

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      parentId: form.parentId,
      deptName: form.deptName,
      orderNum: form.orderNum,
      leader: form.leader,
      phone: form.phone,
      email: form.email,
      status: form.status
    }

    if (isEdit.value && form.id) {
      console.log('📤 更新部门:', form.id, submitData)
      await departmentApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      console.log('📤 创建部门:', submitData)
      await departmentApi.create(submitData)
      ElMessage.success('创建成功')
    }

    handleCloseDialog()
    loadDeptTree()
  } catch (error) {
    console.error('❌ 提交失败:', error)
    if (error instanceof Error) {
      ElMessage.error(`操作失败: ${error.message}`)
    } else {
      ElMessage.error('操作失败')
    }
  } finally {
    formLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    parentId: 0,
    deptName: '',
    orderNum: 0,
    leader: '',
    phone: '',
    email: '',
    status: 1
  })
}

// 关闭对话框
const handleCloseDialog = () => {
  formRef.value?.resetFields()
  formDialogVisible.value = false
}

// 初始化
onMounted(() => {
  loadDeptTree()
})
</script>

<style lang="scss" scoped>
.department-management {
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
  
  .tree-card {
    .tree-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .tree-info {
        color: var(--text-secondary);
        font-size: 14px;
      }
      
      .tree-controls {
        .el-button + .el-button {
          margin-left: 8px;
        }
      }
    }
    
    .dept-node {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-right: 8px;
      
      .dept-info {
        display: flex;
        align-items: center;
        
        .dept-icon {
          margin-right: 8px;
          color: var(--el-color-primary);
        }
        
        .dept-name {
          font-weight: 500;
          margin-right: 8px;
        }
        
        .dept-leader {
          color: var(--text-secondary);
          font-size: 12px;
          margin-left: 8px;
        }
      }
      
      .dept-actions {
        display: flex;
        align-items: center;
        opacity: 0;
        transition: opacity 0.3s;
        
        .el-button {
          margin-left: 4px;
        }
      }
      
      &:hover .dept-actions {
        opacity: 1;
      }
    }
  }
}

:deep(.el-tree-node__content) {
  height: 36px;
  
  &:hover {
    background-color: var(--el-fill-color-light);
  }
}

:deep(.el-tree-node__expand-icon) {
  color: var(--el-color-primary);
}
</style> 