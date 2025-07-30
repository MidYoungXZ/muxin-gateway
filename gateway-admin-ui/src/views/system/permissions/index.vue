<template>
  <div class="permission-management">
    <div class="page-header">
      <div class="header-left">
        <h1>权限管理</h1>
        <p>管理系统菜单权限，支持树形结构和权限配置</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增权限
        </el-button>
        <el-button :icon="Refresh" @click="loadMenuTree">
          刷新
        </el-button>
      </div>
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="权限名称">
          <el-input
            v-model="searchForm.menuName"
            placeholder="请输入权限名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="权限类型">
          <el-select v-model="searchForm.menuType" placeholder="请选择类型" clearable>
            <el-option label="目录" value="M" />
            <el-option label="菜单" value="C" />
            <el-option label="按钮" value="F" />
          </el-select>
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

    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <div class="table-actions">
          <span class="table-info">
            共 {{ menuCount }} 个权限
          </span>
        </div>
        <div class="table-controls">
          <el-button text @click="expandAll">展开全部</el-button>
          <el-button text @click="collapseAll">收起全部</el-button>
        </div>
      </div>

      <el-table
        ref="menuTableRef"
        v-loading="loading"
        :data="menuTableData"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
        border
      >
        <el-table-column prop="menuName" label="权限名称" min-width="200">
          <template #default="{ row }">
            <div class="menu-name-cell">
              <span>{{ row.menuName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="menuType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getMenuTypeTag(row.menuType).type"
              size="small"
            >
              {{ getMenuTypeTag(row.menuType).text }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="path" label="路由地址" min-width="150" show-overflow-tooltip />
        
        <el-table-column prop="component" label="组件路径" min-width="180" show-overflow-tooltip />
        
        <el-table-column prop="perms" label="权限标识" min-width="150" show-overflow-tooltip />
        
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        
        <el-table-column prop="visible" label="显示" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'" size="small">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              size="small"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              text
              :icon="Plus"
              @click="handleAddChild(row)"
            >
              新增
            </el-button>
            <el-button
              type="warning"
              size="small"
              text
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              text
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 表单对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑权限' : '新增权限'"
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
            <el-form-item label="父权限" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="parentMenuOptions"
                :props="{ label: 'menuName', value: 'id' }"
                check-strictly
                placeholder="请选择父权限"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限类型" prop="menuType">
              <el-radio-group v-model="form.menuType" @change="handleMenuTypeChange">
                <el-radio label="M">目录</el-radio>
                <el-radio label="C">菜单</el-radio>
                <el-radio label="F">按钮</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限名称" prop="menuName">
              <el-input
                v-model="form.menuName"
                placeholder="请输入权限名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="sortOrder">
              <el-input-number
                v-model="form.sortOrder"
                :min="0"
                :max="999"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="form.menuType !== 'F'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="路由地址" prop="path">
                <el-input
                  v-model="form.path"
                  placeholder="请输入路由地址"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="菜单图标" prop="icon">
                <el-input
                  v-model="form.icon"
                  placeholder="请输入图标名称"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row v-if="form.menuType === 'C'" :gutter="20">
            <el-col :span="24">
              <el-form-item label="组件路径" prop="component">
                <el-input
                  v-model="form.component"
                  placeholder="请输入组件路径，如：system/user/index"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="权限标识" prop="perms">
              <el-input
                v-model="form.perms"
                placeholder="请输入权限标识，如：system:user:list"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="显示状态" prop="visible">
              <el-radio-group v-model="form.visible">
                <el-radio :label="1">显示</el-radio>
                <el-radio :label="0">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限状态" prop="status">
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Delete, Search, Refresh, Edit } from '@element-plus/icons-vue'
import { menuApi, type Menu } from '@/api/menus'

const loading = ref(false)
const formLoading = ref(false)
const menuTableData = ref<Menu[]>([])
const menuCount = ref(0)
const formDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const menuTableRef = ref()

const searchForm = reactive({
  menuName: '',
  menuType: '',
  status: undefined
})

const form = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  menuName: '',
  i18nCode: '',
  menuType: 'C' as 'M' | 'C' | 'F',
  path: '',
  component: '',
  perms: '',
  icon: '',
  sortOrder: 0,
  visible: 1,
  status: 1
})

const parentMenuOptions = ref<Menu[]>([])
const isEdit = computed(() => !!form.id)

const rules: FormRules = {
  menuName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '权限名称长度在2-50个字符', trigger: 'blur' }
  ],
  menuType: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ],
  sortOrder: [
    { required: true, message: '请输入显示排序', trigger: 'blur' }
  ]
}

const getMenuTypeTag = (type: string) => {
  const typeMap = {
    'M': { text: '目录', type: 'info' },
    'C': { text: '菜单', type: 'success' },
    'F': { text: '按钮', type: 'warning' }
  }
  return typeMap[type as keyof typeof typeMap] || { text: '未知', type: 'danger' }
}

const loadMenuTree = async () => {
  try {
    loading.value = true
    const response = await menuApi.getMenuTree()
    
    if (response && response.data) {
      menuTableData.value = response.data
      menuCount.value = countMenus(response.data)
    } else {
      menuTableData.value = []
      menuCount.value = 0
    }
  } catch (error) {
    console.error('加载权限树失败:', error)
    ElMessage.error('加载权限树失败')
    
    // 模拟数据
    const mockMenus: Menu[] = [
      {
        id: 1,
        parentId: 0,
        menuName: '系统管理',
        i18nCode: 'system',
        menuType: 'M',
        path: '/system',
        component: '',
        perms: '',
        icon: 'setting',
        sortOrder: 1,
        visible: 1,
        status: 1,
        createTime: '2024-01-01 10:00:00',
        updateTime: '2024-01-01 10:00:00',
        children: [
          {
            id: 2,
            parentId: 1,
            menuName: '用户管理',
            i18nCode: 'system.user',
            menuType: 'C',
            path: '/system/users',
            component: 'system/users/index',
            perms: 'system:user:list',
            icon: 'user',
            sortOrder: 1,
            visible: 1,
            status: 1,
            createTime: '2024-01-02 10:00:00',
            updateTime: '2024-01-02 10:00:00'
          }
        ]
      }
    ]
    menuTableData.value = mockMenus
    menuCount.value = countMenus(mockMenus)
    ElMessage.warning('后端服务异常，当前显示模拟数据仅供界面测试')
  } finally {
    loading.value = false
  }
}

const countMenus = (menus: Menu[]): number => {
  let count = 0
  const traverse = (nodes: Menu[]) => {
    nodes.forEach(node => {
      count++
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    })
  }
  traverse(menus)
  return count
}

const handleSearch = () => {
  ElMessage.info('搜索功能开发中...')
}

const handleReset = () => {
  Object.assign(searchForm, {
    menuName: '',
    menuType: '',
    status: undefined
  })
  loadMenuTree()
}

const expandAll = () => {
  // TODO: 实现展开全部
}

const collapseAll = () => {
  // TODO: 实现收起全部
}

const handleAdd = () => {
  resetForm()
  form.parentId = 0
  parentMenuOptions.value = buildParentOptions(menuTableData.value)
  formDialogVisible.value = true
}

const handleAddChild = (parent: Menu) => {
  resetForm()
  form.parentId = parent.id
  if (parent.menuType === 'M') {
    form.menuType = 'C'
  } else if (parent.menuType === 'C') {
    form.menuType = 'F'
  }
  parentMenuOptions.value = buildParentOptions(menuTableData.value)
  formDialogVisible.value = true
}

const handleEdit = (menu: Menu) => {
  resetForm()
  Object.assign(form, {
    id: menu.id,
    parentId: menu.parentId,
    menuName: menu.menuName,
    i18nCode: menu.i18nCode,
    menuType: menu.menuType,
    path: menu.path,
    component: menu.component,
    perms: menu.perms,
    icon: menu.icon,
    sortOrder: menu.sortOrder,
    visible: menu.visible,
    status: menu.status
  })
  parentMenuOptions.value = buildParentOptions(menuTableData.value, menu.id)
  formDialogVisible.value = true
}

const handleDelete = async (menu: Menu) => {
  if (menu.children && menu.children.length > 0) {
    ElMessage.warning('存在子权限，不允许删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除权限"${menu.menuName}"吗？`, '删除确认', {
      type: 'warning'
    })
    
    await menuApi.delete(menu.id)
    ElMessage.success('删除成功')
    loadMenuTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleStatusChange = async (menu: Menu) => {
  try {
    if (menu.status === 1) {
      await menuApi.enable(menu.id)
      ElMessage.success('启用成功')
    } else {
      await menuApi.disable(menu.id)
      ElMessage.success('禁用成功')
    }
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    menu.status = menu.status === 1 ? 0 : 1
  }
}

const handleMenuTypeChange = (type: string) => {
  if (type === 'F') {
    form.path = ''
    form.component = ''
    form.icon = ''
  }
}

const buildParentOptions = (menus: Menu[], excludeId?: number): Menu[] => {
  const options: Menu[] = [
    { 
      id: 0, 
      parentId: -1, 
      menuName: '根权限', 
      i18nCode: '',
      menuType: 'M', 
      path: '',
      component: '',
      perms: '',
      icon: '',
      sortOrder: 0, 
      visible: 1, 
      status: 1, 
      createTime: '', 
      updateTime: '' 
    }
  ]
  
  const traverse = (nodes: Menu[]) => {
    nodes.forEach(node => {
      if (excludeId && node.id === excludeId) {
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
  
  traverse(menus)
  return options
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      parentId: form.parentId,
      menuName: form.menuName,
      i18nCode: form.i18nCode,
      menuType: form.menuType,
      path: form.path,
      component: form.component,
      perms: form.perms,
      icon: form.icon,
      sortOrder: form.sortOrder,
      visible: form.visible,
      status: form.status
    }

    if (isEdit.value && form.id) {
      await menuApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await menuApi.create(submitData)
      ElMessage.success('创建成功')
    }

    handleCloseDialog()
    loadMenuTree()
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
    menuName: '',
    i18nCode: '',
    menuType: 'C',
    path: '',
    component: '',
    perms: '',
    icon: '',
    sortOrder: 0,
    visible: 1,
    status: 1
  })
}

const handleCloseDialog = () => {
  formRef.value?.resetFields()
  formDialogVisible.value = false
}

onMounted(() => {
  loadMenuTree()
})
</script>

<style lang="scss" scoped>
.permission-management {
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
      
      .table-info {
        color: var(--text-secondary);
        font-size: 14px;
      }
      
      .table-controls {
        .el-button + .el-button {
          margin-left: 8px;
        }
      }
    }
    
    .menu-name-cell {
      display: flex;
      align-items: center;
    }
  }
}
</style>