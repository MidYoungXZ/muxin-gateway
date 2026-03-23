<template>
  <div class="user-management">
    <div class="page-header">
      <div class="header-left">
        <h1>用户管理</h1>
        <p>管理系统用户，包括用户信息维护、角色分配等</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
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
            <el-icon><RefreshRight /></el-icon>
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
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              link
              @click="handleAssignRoles(row)"
            >
              分配角色
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="handleResetPassword(row)"
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
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="600px"
      :close-on-click-modal="false"
      @close="handleCloseDialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                :disabled="isEdit"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="form.nickname"
                placeholder="请输入昵称"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="!isEdit">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                show-password
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
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
            <el-form-item label="手机号" prop="mobile">
              <el-input
                v-model="form.mobile"
                placeholder="请输入手机号"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
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

    <!-- 角色分配对话框 -->
    <RoleAssignDialog
      v-model="roleDialogVisible"
      :user-data="currentUser"
      :role-list="roleList"
      @success="loadUserList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi, type User, type UserQueryParams } from '@/api/users'
import { roleApi, type Role } from '@/api/roles'
import RoleAssignDialog from './components/RoleAssignDialog.vue'

const loading = ref(false)
const formLoading = ref(false)
const userList = ref<User[]>([])
const total = ref(0)
const selectedUsers = ref<User[]>([])

const formDialogVisible = ref(false)
const formRef = ref<FormInstance>()

const roleDialogVisible = ref(false)
const currentUser = ref<Partial<User>>({})
const roleList = ref<Role[]>([])

const searchForm = reactive<UserQueryParams>({
  username: '',
  nickname: '',
  status: undefined
})

const form = reactive({
  id: undefined as number | undefined,
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  mobile: '',
  status: 1 as 0 | 1
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20
})

// 计算属性
const isEdit = computed(() => !!form.id)

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: !isEdit.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: !isEdit.value, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!isEdit.value && value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在2-20个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  mobile: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

// 加载用户列表
const loadUserList = async () => {
  try {
    loading.value = true
    
    const queryParams = {
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    
    const response = await userApi.list(queryParams)
    
    if (response && response.data) {
      const pageData = response.data
      userList.value = pageData.data || []
      total.value = pageData.total || 0
    } else {
      userList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
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
    status: undefined
  })
  handleSearch()
}

// 新增用户
const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    email: '',
    mobile: '',
    status: 1
  })
  formDialogVisible.value = true
}

// 编辑用户
const handleEdit = (user: User) => {
  Object.assign(form, {
    id: user.id,
    username: user.username,
    nickname: user.nickname,
    email: user.email,
    mobile: user.mobile,
    status: user.status,
    password: '',
    confirmPassword: ''
  })
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

// 重置密码
const handleResetPassword = async (user: User) => {
  try {
    const { value: newPassword } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
      inputType: 'password',
      inputValidator: (value: string) => {
        if (!value || value.length < 6) {
          return '密码长度不能少于6位'
        }
        return true
      }
    })
    
    console.log('📤 重置用户密码:', user.id)
    await userApi.resetPassword(user.id, newPassword)
    ElMessage.success('密码重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ 密码重置失败:', error)
      ElMessage.error('密码重置失败：' + (error as Error).message)
    }
  }
}

// 选择变更
const handleSelectionChange = (selection: User[]) => {
  selectedUsers.value = selection
}

// 分页变更
const handleSizeChange = () => {
  pagination.page = 1
  loadUserList()
}

const handleCurrentChange = () => {
  loadUserList()
}

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      username: form.username,
      nickname: form.nickname,
      email: form.email,
      mobile: form.mobile,
      status: form.status
    }

    if (isEdit.value && form.id) {
      console.log('📤 更新用户:', form.id, submitData)
      await userApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      const createData = {
        ...submitData,
        password: form.password
      }
      console.log('📤 创建用户:', createData)
      await userApi.create(createData)
      ElMessage.success('创建成功')
    }

    handleCloseDialog()
    loadUserList()
  } catch (error) {
    console.error('❌ 提交失败:', error)
    ElMessage.error('操作失败：' + (error as Error).message)
  } finally {
    formLoading.value = false
  }
}

// 关闭对话框
const handleCloseDialog = () => {
  formRef.value?.resetFields()
  formDialogVisible.value = false
}

// 加载角色列表
const loadRoleList = async () => {
  try {
    const response = await roleApi.listAll()
    if (response && response.data) {
      roleList.value = response.data
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

// 分配角色
const handleAssignRoles = async (user: User) => {
  currentUser.value = user
  await loadRoleList()
  roleDialogVisible.value = true
}

// 初始化
onMounted(() => {
  loadUserList()
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