<template>
  <div class="page-list-container">
    <div class="page-title-bar">
      <span class="title">部门管理</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增部门
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchForm.deptName"
        placeholder="部门名称"
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
        <span class="toolbar-right">共 {{ deptCount }} 个部门</span>
      </div>

      <el-tree
        ref="deptTreeRef"
        v-loading="loading"
        :data="filteredDeptTree"
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
              <el-tag v-if="data.deptCode" type="info" size="small" style="margin-left: 8px;">
                {{ data.deptCode }}
              </el-tag>
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
              <el-button type="primary" size="small" link @click="handleAddChild(data)">添加</el-button>
              <el-button type="primary" size="small" link @click="handleEdit(data)">编辑</el-button>
              <el-switch
                v-model="data.status"
                :active-value="1"
                :inactive-value="0"
                size="small"
                style="margin: 0 8px;"
                @change="handleStatusChange(data)"
              />
              <el-button type="danger" size="small" link @click="handleDelete(data)">删除</el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

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
                @blur="checkDeptName"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门编码" prop="deptCode">
              <el-input
                v-model="form.deptCode"
                placeholder="请输入部门编码"
                @blur="checkDeptCode"
              />
            </el-form-item>
          </el-col>
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
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="leader">
              <el-input
                v-model="form.leader"
                placeholder="请输入负责人"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="请输入联系电话"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="form.email"
                placeholder="请输入邮箱"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, OfficeBuilding } from '@element-plus/icons-vue'
import { departmentApi, type Department } from '@/api/departments'

const loading = ref(false)
const formLoading = ref(false)
const deptTreeData = ref<Department[]>([])
const deptCount = ref(0)
const isAllExpanded = ref(true)

const formDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const deptTreeRef = ref()

const searchForm = reactive({
  deptName: '',
  status: undefined as number | undefined
})

const form = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  deptName: '',
  deptCode: '',
  orderNum: 0,
  leader: '',
  phone: '',
  email: '',
  status: 1
})

const parentDeptOptions = ref<Department[]>([])
const isEdit = computed(() => !!form.id)

const treeProps = {
  children: 'children',
  label: 'deptName'
}

const rules: FormRules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '部门名称长度在2-50个字符', trigger: 'blur' }
  ],
  deptCode: [
    { max: 50, message: '部门编码长度不能超过50个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const filteredDeptTree = computed(() => {
  if (!searchForm.deptName && searchForm.status === undefined) {
    return deptTreeData.value
  }
  return filterTree(deptTreeData.value)
})

const filterTree = (nodes: Department[]): Department[] => {
  return nodes.reduce((result: Department[], node) => {
    const nameMatch = !searchForm.deptName || node.deptName.includes(searchForm.deptName)
    const statusMatch = searchForm.status === undefined || node.status === searchForm.status
    
    if (nameMatch && statusMatch) {
      result.push({
        ...node,
        children: node.children ? filterTree(node.children) : undefined
      })
    } else if (node.children && node.children.length > 0) {
      const filteredChildren = filterTree(node.children)
      if (filteredChildren.length > 0) {
        result.push({
          ...node,
          children: filteredChildren
        })
      }
    }
    return result
  }, [])
}

const loadDeptTree = async () => {
  try {
    loading.value = true
    const response = await departmentApi.getTree()
    
    if (response && response.data) {
      deptTreeData.value = response.data
      deptCount.value = countDepts(response.data)
      isAllExpanded.value = true
    } else {
      deptTreeData.value = []
      deptCount.value = 0
      isAllExpanded.value = true
    }
  } catch (error) {
    console.error('加载部门树失败:', error)
    ElMessage.error('加载部门树失败')
    deptTreeData.value = []
    deptCount.value = 0
    isAllExpanded.value = true
  } finally {
    loading.value = false
  }
}

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

const handleSearch = () => {
  // 过滤已通过 computed 自动处理
}

const handleReset = () => {
  searchForm.deptName = ''
  searchForm.status = undefined
}

const toggleExpandAll = async () => {
  await nextTick()
  const keys = getAllNodeKeys(deptTreeData.value)
  if (isAllExpanded.value) {
    keys.forEach(key => {
      const node = deptTreeRef.value?.store?.nodesMap?.[key]
      if (node) {
        node.expanded = false
      }
    })
    isAllExpanded.value = false
  } else {
    keys.forEach(key => {
      const node = deptTreeRef.value?.store?.nodesMap?.[key]
      if (node) {
        node.expanded = true
      }
    })
    isAllExpanded.value = true
  }
}

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

const handleAdd = () => {
  resetForm()
  form.parentId = 0
  parentDeptOptions.value = buildParentOptions(deptTreeData.value)
  formDialogVisible.value = true
}

const handleAddChild = (parent: Department) => {
  resetForm()
  form.parentId = parent.id
  parentDeptOptions.value = buildParentOptions(deptTreeData.value)
  formDialogVisible.value = true
}

const handleEdit = (dept: Department) => {
  resetForm()
  Object.assign(form, {
    id: dept.id,
    parentId: dept.parentId,
    deptName: dept.deptName,
    deptCode: dept.deptCode || '',
    orderNum: dept.orderNum || 0,
    leader: dept.leader || '',
    phone: dept.phone || '',
    email: dept.email || '',
    status: dept.status
  })
  parentDeptOptions.value = buildParentOptions(deptTreeData.value, dept.id)
  formDialogVisible.value = true
}

const handleDelete = async (dept: Department) => {
  if (dept.children && dept.children.length > 0) {
    ElMessage.warning('存在子部门，不允许删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除部门"${dept.deptName}"吗？`, '删除确认', {
      type: 'warning'
    })
    
    await departmentApi.delete(dept.id)
    ElMessage.success('删除成功')
    loadDeptTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

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
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    dept.status = dept.status === 1 ? 0 : 1
  }
}

const handleNodeDrop = async (dragNode: any, dropNode: any, dropType: string) => {
  try {
    const dragDeptId = dragNode.data.id
    let targetParentId = 0
    
    if (dropType === 'inner') {
      targetParentId = dropNode.data.id
    } else {
      targetParentId = dropNode.data.parentId
    }
    
    await departmentApi.move(dragDeptId, targetParentId)
    ElMessage.success('移动成功')
    loadDeptTree()
  } catch (error) {
    console.error('移动失败:', error)
    ElMessage.error('移动失败')
    loadDeptTree()
  }
}

const allowDrop = (dragNode: any, dropNode: any, type: string) => {
  if (type === 'inner') {
    return !isDescendant(dragNode.data.id, dropNode.data.id)
  }
  return true
}

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

const buildParentOptions = (depts: Department[], excludeId?: number): Department[] => {
  const options: Department[] = [
    { id: 0, parentId: -1, deptName: '根部门', orderNum: 0, status: 1, createTime: '', updateTime: '' }
  ]
  
  const traverse = (nodes: Department[]) => {
    nodes.forEach(node => {
      if (excludeId && (node.id === excludeId || isDescendant(excludeId, node.id))) {
        return
      }
      options.push({
        ...node,
        children: undefined
      })
      if (node.children) {
        traverse(node.children)
      }
    })
  }
  
  traverse(depts)
  return options
}

const checkDeptName = async () => {
  if (!form.deptName) return
  
  try {
    const response = await departmentApi.checkName(form.deptName, form.parentId || 0, form.id)
    if (response.data === false) {
      ElMessage.warning('部门名称已存在')
    }
  } catch (error) {
    console.error('检查部门名称失败:', error)
  }
}

const checkDeptCode = async () => {
  if (!form.deptCode) return
  
  try {
    const response = await departmentApi.checkCode(form.deptCode, form.id)
    if (response.data === false) {
      ElMessage.warning('部门编码已存在')
    }
  } catch (error) {
    console.error('检查部门编码失败:', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      parentId: form.parentId,
      deptName: form.deptName,
      deptCode: form.deptCode,
      orderNum: form.orderNum,
      leader: form.leader,
      phone: form.phone,
      email: form.email,
      status: form.status
    }

    if (isEdit.value && form.id) {
      await departmentApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await departmentApi.create(submitData)
      ElMessage.success('创建成功')
    }

    handleCloseDialog()
    loadDeptTree()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    parentId: 0,
    deptName: '',
    deptCode: '',
    orderNum: 0,
    leader: '',
    phone: '',
    email: '',
    status: 1
  })
}

const handleCloseDialog = () => {
  formRef.value?.resetFields()
  formDialogVisible.value = false
}

onMounted(() => {
  loadDeptTree()
})
</script>

<style lang="scss" scoped>
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
  }
  
  &:hover .dept-actions {
    opacity: 1;
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