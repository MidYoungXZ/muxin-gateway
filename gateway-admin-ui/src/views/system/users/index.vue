<template>
  <div class="page-list-container">
    <!-- 标题栏 -->
    <div class="page-title-bar">
      <span class="title">用户管理</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <!-- 搜索区 -->
    <div class="search-bar">
      <el-input v-model="searchForm.username" placeholder="用户名" clearable @keyup.enter="handleSearch" />
      <el-input v-model="searchForm.nickname" placeholder="昵称" clearable @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable>
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <div class="search-actions">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 表格区 -->
    <div class="table-wrapper">
      <div class="table-toolbar">
        <span class="toolbar-right">共 {{ total }} 条</span>
      </div>

      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column prop="nickname" label="昵称" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="mobile" label="手机号" width="120" />
        <el-table-column prop="deptName" label="部门" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" size="small" link @click="handleAssignRoles(row)">分配角色</el-button>
            <el-button type="primary" size="small" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" size="small" link>删除</el-button>
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
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 用户表单对话框 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="550px" :close-on-click-modal="false" @close="handleCloseDialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16" v-if="!isEdit">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="mobile">
              <el-input v-model="form.mobile" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 角色分配对话框 -->
    <RoleAssignDialog v-model="roleDialogVisible" :user-data="currentUser" :role-list="roleList" @success="loadUserList" />
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

const formDialogVisible = ref(false)
const formRef = ref<FormInstance>()

const roleDialogVisible = ref(false)
const currentUser = ref<Partial<User>>({})
const roleList = ref<Role[]>([])

const searchForm = reactive<UserQueryParams>({ username: '', nickname: '', status: undefined })
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
const pagination = reactive({ page: 1, size: 20 })

const isEdit = computed(() => !!form.id)

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 20, message: '用户名长度在2-20个字符', trigger: 'blur' }],
  password: [{ required: !isEdit.value, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }],
  confirmPassword: [
    { required: !isEdit.value, message: '请再次输入密码', trigger: 'blur' },
    { validator: (rule, value, callback) => { if (!isEdit.value && value !== form.password) callback(new Error('两次输入的密码不一致')); else callback() }, trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }, { min: 2, max: 20, message: '昵称长度在2-20个字符', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  mobile: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }]
}

const loadUserList = async () => {
  try {
    loading.value = true
    const response = await userApi.list({ ...searchForm, pageNum: pagination.page, pageSize: pagination.size })
    if (response?.data) {
      userList.value = response.data.data || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; loadUserList() }
const handleReset = () => { Object.assign(searchForm, { username: '', nickname: '', status: undefined }); handleSearch() }

const handleAdd = () => {
  Object.assign(form, { id: undefined, username: '', password: '', confirmPassword: '', nickname: '', email: '', mobile: '', status: 1 })
  formDialogVisible.value = true
}

const handleEdit = (user: User) => {
  Object.assign(form, { id: user.id, username: user.username, nickname: user.nickname, email: user.email, mobile: user.mobile, status: user.status, password: '', confirmPassword: '' })
  formDialogVisible.value = true
}

const handleDelete = async (user: User) => {
  try {
    await userApi.delete(user.id)
    ElMessage.success('删除成功')
    loadUserList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleStatusChange = async (user: User) => {
  try {
    await (user.status === 1 ? userApi.enable(user.id) : userApi.disable(user.id))
    ElMessage.success(user.status === 1 ? '启用成功' : '禁用成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    user.status = user.status === 1 ? 0 : 1
  }
}

const handleResetPassword = async (user: User) => {
  try {
    const { value: newPassword } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
      inputType: 'password',
      inputValidator: (value: string) => (!value || value.length < 6) ? '密码长度不能少于6位' : true
    })
    await userApi.resetPassword(user.id, newPassword)
    ElMessage.success('密码重置成功')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('密码重置失败')
  }
}

const handleSizeChange = () => { pagination.page = 1; loadUserList() }
const handleCurrentChange = () => loadUserList()

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    formLoading.value = true
    const submitData = { username: form.username, nickname: form.nickname, email: form.email, mobile: form.mobile, status: form.status }
    if (isEdit.value && form.id) {
      await userApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await userApi.create({ ...submitData, password: form.password })
      ElMessage.success('创建成功')
    }
    handleCloseDialog()
    loadUserList()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleCloseDialog = () => { formRef.value?.resetFields(); formDialogVisible.value = false }

const loadRoleList = async () => {
  try {
    const response = await roleApi.listAll()
    if (response?.data) roleList.value = response.data
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

const handleAssignRoles = async (user: User) => {
  currentUser.value = user
  await loadRoleList()
  roleDialogVisible.value = true
}

onMounted(() => loadUserList())
</script>

<style lang="scss" scoped>
</style>