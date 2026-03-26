<template>
  <div class="page-list-container">
    <div class="page-title-bar">
      <span class="title">用户管理</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <div class="search-bar">
      <el-tree-select
        v-model="searchForm.deptId"
        :data="deptOptions"
        :props="{ label: 'deptName', value: 'id' }"
        placeholder="选择部门"
        clearable
        check-strictly
        style="width: 200px"
      />
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
        <el-table-column prop="createTime" label="创建时间" min-width="180" align="center">
          <template #default="{ row }">
            <span class="time-cell">{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button v-if="canManageUser(row)" type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canManageUser(row)" type="primary" size="small" link @click="handleAssignRoles(row)">分配角色</el-button>
            <el-button v-if="canManageUser(row)" type="primary" size="small" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-popconfirm v-if="canManageUser(row) && row.id !== currentUserId" title="确定删除？" @confirm="handleDelete(row)">
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

    <UserFormDialog
      v-model="formDialogVisible"
      :user-data="currentUser"
      :dept-tree="managedDeptOptions"
      :role-list="roleList"
      @success="loadUserList"
    />

    <RoleAssignDialog v-model="roleDialogVisible" :user-data="currentUser" :role-list="roleList" @success="loadUserList" />
    
    <!-- 查看用户详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="用户详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ currentUser?.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser?.nickname }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser?.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser?.mobile || '-' }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentUser?.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser?.status === 1 ? 'success' : 'danger'">
            {{ currentUser?.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(currentUser?.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="角色" :span="2">
          <el-tag v-for="role in currentUser?.roles" :key="role.id" style="margin-right: 8px;">
            {{ role.roleName }}
          </el-tag>
          <span v-if="!currentUser?.roles?.length">-</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi, type User, type UserQueryParams } from '@/api/users'
import { roleApi, type Role } from '@/api/roles'
import { departmentApi, type Department } from '@/api/departments'
import { useUserStore } from '@/stores/user'
import RoleAssignDialog from './components/RoleAssignDialog.vue'
import UserFormDialog from './components/UserFormDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const userList = ref<User[]>([])
const total = ref(0)
const deptOptions = ref<Department[]>([])
const managedDeptIds = ref<number[]>([])
const currentUserId = ref<number>(0)

const formDialogVisible = ref(false)
const roleDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentUser = ref<Partial<User>>({})
const roleList = ref<Role[]>([])

const searchForm = reactive<UserQueryParams>({ username: '', nickname: '', status: undefined, deptId: undefined })
const pagination = reactive({ page: 1, size: 20 })

const managedDeptOptions = computed(() => {
  if (managedDeptIds.value.length === 0) {
    return deptOptions.value
  }
  return filterDeptsByIds(deptOptions.value, managedDeptIds.value)
})

const canManageUser = (user: User): boolean => {
  if (managedDeptIds.value.length === 0) {
    return true
  }
  if (!user.deptId) {
    return true
  }
  return managedDeptIds.value.includes(user.deptId)
}

const filterDeptsByIds = (depts: Department[], ids: number[]): Department[] => {
  return depts.filter(dept => {
    if (ids.includes(dept.id)) {
      if (dept.children && dept.children.length > 0) {
        dept.children = filterDeptsByIds(dept.children, ids)
      }
      return true
    }
    if (dept.children && dept.children.length > 0) {
      const filteredChildren = filterDeptsByIds(dept.children, ids)
      if (filteredChildren.length > 0) {
        dept.children = filteredChildren
        return true
      }
    }
    return false
  }).map(dept => ({ ...dept }))
}

const loadDeptOptions = async () => {
  try {
    const response = await departmentApi.getOptions()
    if (response?.data) {
      deptOptions.value = response.data
    }
  } catch (error) {
    console.error('加载部门选项失败:', error)
  }
}

const loadManagedDeptIds = async () => {
  try {
    const response = await userApi.getManagedDeptIds()
    if (response?.data) {
      managedDeptIds.value = response.data
    }
  } catch (error) {
    console.error('加载可管理部门失败:', error)
  }
}

const loadUserList = async () => {
  try {
    loading.value = true
    const response = await userApi.list({ ...searchForm, page: pagination.page, size: pagination.size })
    if (response?.data) {
      userList.value = response.data.data || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; loadUserList() }
const handleReset = () => { 
  Object.assign(searchForm, { username: '', nickname: '', status: undefined, deptId: undefined })
  handleSearch() 
}

const handleAdd = () => {
  currentUser.value = {}
  formDialogVisible.value = true
}

const handleView = async (user: User) => {
  try {
    const response = await userApi.getDetail(user.id)
    if (response?.data) {
      currentUser.value = response.data
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
  }
}

const handleEdit = (user: User) => {
  currentUser.value = { ...user }
  formDialogVisible.value = true
}

const handleDelete = async (user: User) => {
  try {
    await userApi.delete(user.id)
    ElMessage.success('删除成功')
    loadUserList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const handleStatusChange = async (user: User) => {
  try {
    await (user.status === 1 ? userApi.enable(user.id) : userApi.disable(user.id))
    ElMessage.success(user.status === 1 ? '启用成功' : '禁用成功')
  } catch (error) {
    console.error('状态更新失败:', error)
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
    if (error !== 'cancel') {
      console.error('密码重置失败:', error)
    }
  }
}

const handleSizeChange = () => { pagination.page = 1; loadUserList() }
const handleCurrentChange = () => loadUserList()

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

onMounted(() => {
  currentUserId.value = userStore.userInfo?.id || 0
  loadDeptOptions()
  loadManagedDeptIds()
  loadUserList()
})

// 时间格式化 - 标准格式不带T
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}
</script>

<style lang="scss" scoped>
.time-cell {
  white-space: nowrap;
}
</style>