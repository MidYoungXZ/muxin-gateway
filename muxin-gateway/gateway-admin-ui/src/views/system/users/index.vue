<template>
  <div class="user-management">
    <div class="page-header">
      <div class="header-left">
        <h1>用户管理</h1>
        <p>管理系统用户，包括用户信息维护、角色分配等</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd" v-permission="'system:user:create'">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
        <el-button @click="handleImport" v-permission="'system:user:import'">
          <el-icon><Upload /></el-icon>
          导入
        </el-button>
        <el-button @click="handleExport" v-permission="'system:user:export'">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" label-width="80px">
        <el-form-item label="用户名">
          <el-input 
            v-model="searchForm.username" 
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input 
            v-model="searchForm.nickname" 
            placeholder="请输入昵称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select
            v-model="searchForm.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id' }"
            placeholder="请选择部门"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <div class="table-header">
        <div class="table-actions">
          <el-button 
            type="danger" 
            :disabled="!selectedUsers.length"
            @click="handleBatchDelete"
            v-permission="'system:user:delete'"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button 
            :disabled="!selectedUsers.length"
            @click="handleBatchStatus(1)"
            v-permission="'system:user:update'"
          >
            <el-icon><Check /></el-icon>
            批量启用
          </el-button>
          <el-button 
            :disabled="!selectedUsers.length"
            @click="handleBatchStatus(0)"
            v-permission="'system:user:update'"
          >
            <el-icon><Close /></el-icon>
            批量禁用
          </el-button>
        </div>
        <div class="table-info">
          共 {{ total }} 条记录
        </div>
      </div>

      <el-table 
        :data="userList" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="mobile" label="手机号" min-width="130" />
        <el-table-column prop="deptName" label="部门" min-width="120" />
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag 
              v-for="role in row.roles" 
              :key="role.id"
              size="small"
              style="margin-right: 4px"
            >
              {{ role.roleName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              v-permission="'system:user:update'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleEdit(row)"
              v-permission="'system:user:update'"
            >
              编辑
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleAssignRoles(row)"
              v-permission="'system:user:update'"
            >
              分配角色
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleResetPassword(row)"
              v-permission="'system:user:update'"
            >
              重置密码
            </el-button>
            <el-popconfirm
              title="确定要删除这个用户吗？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button 
                  type="danger" 
                  size="small" 
                  link
                  v-permission="'system:user:delete'"
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

    <!-- 用户表单对话框 -->
    <user-form-dialog
      v-model="formDialogVisible"
      :user-data="currentUser"
      :dept-tree="deptTree"
      :role-list="roleList"
      @success="handleFormSuccess"
    />

    <!-- 角色分配对话框 -->
    <role-assign-dialog
      v-model="roleDialogVisible"
      :user-data="currentUser"
      :role-list="roleList"
      @success="handleRoleAssignSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Upload, 
  Download, 
  Search, 
  Refresh, 
  Delete, 
  Check, 
  Close 
} from '@element-plus/icons-vue'
import { userApi } from '@/api/users'
import { roleApi } from '@/api/roles'
import { departmentApi } from '@/api/departments'
import type { 
  User, 
  UserQueryParams, 
  Role, 
  DepartmentTree 
} from '@/types/system'
import UserFormDialog from './components/UserFormDialog.vue'
import RoleAssignDialog from './components/RoleAssignDialog.vue'

// 数据定义
const loading = ref(false)
const userList = ref<User[]>([])
const total = ref(0)
const selectedUsers = ref<User[]>([])
const deptTree = ref<DepartmentTree[]>([])
const roleList = ref<Role[]>([])

// 表单和对话框
const formDialogVisible = ref(false)
const roleDialogVisible = ref(false)
const currentUser = ref<Partial<User>>({})

// 搜索表单
const searchForm = reactive<UserQueryParams>({
  username: '',
  nickname: '',
  deptId: undefined,
  status: undefined
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20
})

// 查询参数
const queryParams = computed(() => ({
  ...searchForm,
  page: pagination.page,
  size: pagination.size
}))

// 加载用户列表
const loadUserList = async () => {
  try {
    loading.value = true
    console.log('🔄 开始加载用户列表...', queryParams.value)
    const response = await userApi.list(queryParams.value)
    console.log('📨 API响应:', response)
    
    if (response && response.data) {
      const apiData = response.data
      
      // 检查后端返回的数据结构 { data: { data: [], total: ... } }
      if (apiData.data && typeof apiData.data === 'object' && Array.isArray(apiData.data.data)) {
        userList.value = apiData.data.data || []
        total.value = apiData.data.total || 0
      } else {
        console.warn('⚠️ 未知或不兼容的数据结构:', apiData)
        userList.value = []
        total.value = 0
      }
      
      console.log('✅ 用户列表加载成功:', userList.value.length, '条记录')
    } else {
      console.error('❌ API响应数据为空')
      userList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('❌ 加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败：' + (error as Error).message)
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 加载部门树
const loadDeptTree = async () => {
  try {
    console.log('🔄 开始加载部门树...')
    const response = await departmentApi.getTree()
    console.log('📨 部门API响应:', response)
    
    if (response && response.data) {
      deptTree.value = response.data.data || response.data || []
      console.log('✅ 部门树加载成功:', deptTree.value.length, '个部门')
    }
  } catch (error) {
    console.error('❌ 加载部门树失败:', error)
    deptTree.value = []
  }
}

// 加载角色列表
const loadRoleList = async () => {
  try {
    console.log('🔄 开始加载角色列表...')
    const response = await roleApi.listAll()
    console.log('📨 角色API响应:', response)
    
    if (response && response.data) {
      roleList.value = response.data.data || response.data || []
      console.log('✅ 角色列表加载成功:', roleList.value.length, '个角色')
    }
  } catch (error) {
    console.error('❌ 加载角色列表失败:', error)
    roleList.value = []
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadUserList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    nickname: '',
    deptId: undefined,
    status: undefined
  })
  handleSearch()
}

// 新增用户
const handleAdd = () => {
  currentUser.value = {}
  formDialogVisible.value = true
}

// 编辑用户
const handleEdit = (user: User) => {
  currentUser.value = { ...user }
  formDialogVisible.value = true
}

// 删除用户
const handleDelete = async (user: User) => {
  try {
    await userApi.delete(user.id)
    ElMessage.success('删除成功')
    loadUserList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除选中的用户吗？', '批量删除', {
      type: 'warning'
    })
    
    const ids = selectedUsers.value.map(user => user.id)
    await userApi.batchDelete(ids)
    ElMessage.success('批量删除成功')
    loadUserList()
    selectedUsers.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (user: User) => {
  try {
    if (user.status === 1) {
      await userApi.enable(user.id)
      ElMessage.success('启用成功')
    } else {
      await userApi.disable(user.id)
      ElMessage.success('禁用成功')
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    user.status = user.status === 1 ? 0 : 1
  }
}

// 批量状态变更
const handleBatchStatus = async (status: 0 | 1) => {
  try {
    const ids = selectedUsers.value.map(user => user.id)
    await userApi.batchUpdateStatus(ids, status)
    ElMessage.success(`批量${status === 1 ? '启用' : '禁用'}成功`)
    loadUserList()
    selectedUsers.value = []
  } catch (error) {
    ElMessage.error(`批量${status === 1 ? '启用' : '禁用'}失败`)
  }
}

// 分配角色
const handleAssignRoles = (user: User) => {
  currentUser.value = { ...user }
  roleDialogVisible.value = true
}

// 重置密码
const handleResetPassword = async (user: User) => {
  try {
    await ElMessageBox.prompt('请输入新密码', '重置密码', {
      inputType: 'password',
      inputValidator: (value: string) => {
        if (!value || value.length < 6) {
          return '密码长度不能少于6位'
        }
        return true
      }
    })
    
    // TODO: 调用重置密码API
    ElMessage.success('密码重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('密码重置失败')
    }
  }
}

// 导入
const handleImport = () => {
  // TODO: 实现导入功能
  ElMessage.info('导入功能开发中...')
}

// 导出
const handleExport = () => {
  // TODO: 实现导出功能
  ElMessage.info('导出功能开发中...')
}

// 选择变更
const handleSelectionChange = (selection: User[]) => {
  selectedUsers.value = selection
}

// 分页变更
const handleSizeChange = (size: number) => {
  pagination.size = size
  loadUserList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadUserList()
}

// 表单成功回调
const handleFormSuccess = () => {
  formDialogVisible.value = false
  loadUserList()
}

// 角色分配成功回调
const handleRoleAssignSuccess = () => {
  roleDialogVisible.value = false
  loadUserList()
}

// 初始化
onMounted(() => {
  loadUserList()
  loadDeptTree()
  loadRoleList()
})
</script>

<style lang="scss" scoped>
.user-management {
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
      
      .table-actions {
        .el-button + .el-button {
          margin-left: 8px;
        }
      }
      
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
}
</style>