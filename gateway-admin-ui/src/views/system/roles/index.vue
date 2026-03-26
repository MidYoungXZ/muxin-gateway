<template>
  <div class="page-list-container">
    <div class="page-title-bar">
      <span class="title">角色管理</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增角色
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchForm.roleName"
        placeholder="角色名称"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-input
        v-model="searchForm.roleCode"
        placeholder="角色编码"
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
        <div class="toolbar-left">
          <el-button 
            type="danger" 
            :disabled="selectedRoles.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
        <span class="toolbar-right">共 {{ total }} 条，已选 {{ selectedRoles.length }} 条</span>
      </div>

      <el-table 
        v-loading="loading"
        :data="roleList" 
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="角色ID" width="80" align="center" />
        <el-table-column prop="roleCode" label="角色编码" width="150" align="center" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userCount" label="用户数量" width="100" align="center">
          <template #default="{ row }">
            <el-link 
              type="primary" 
              :underline="false"
              @click="handleViewUsers(row)"
            >
              {{ row.userCount || 0 }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-text="启用"
              inactive-text="禁用"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
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
              type="primary" 
              size="small" 
              link
              @click="handleAssignMenus(row)"
            >
              权限
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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
    </div>

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
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
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="form.roleCode"
            placeholder="请输入角色编码，如：ADMIN、USER等"
            :disabled="isEdit"
            @blur="checkRoleCode"
          />
          <div class="form-tip">角色编码只能包含大写字母和下划线</div>
        </el-form-item>
        
        <el-form-item label="角色名称" prop="roleName">
          <el-input
            v-model="form.roleName"
            placeholder="请输入角色名称"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog
      v-model="menuDialogVisible"
      title="分配菜单权限"
      width="500px"
      :close-on-click-modal="false"
      @close="handleCloseMenuDialog"
    >
      <div class="role-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="角色名称">{{ currentRole?.roleName }}</el-descriptions-item>
          <el-descriptions-item label="角色编码">{{ currentRole?.roleCode }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="menu-tree" style="margin-top: 20px;">
        <h4>选择菜单权限</h4>
        <el-tree
          ref="menuTreeRef"
          :data="menuTreeData"
          :props="{ children: 'children', label: 'menuName' }"
          node-key="id"
          show-checkbox
          default-expand-all
          :default-checked-keys="selectedMenuIds"
        >
          <template #default="{ node, data }">
            <span class="menu-tree-node">
              <el-icon v-if="data.menuType === 'M'" style="color: #409EFF;"><Folder /></el-icon>
              <el-icon v-else-if="data.menuType === 'C'" style="color: #67C23A;"><Document /></el-icon>
              <el-icon v-else-if="data.menuType === 'F'" style="color: #E6A23C;"><Key /></el-icon>
              <span style="margin-left: 6px;">{{ data.menuName }}</span>
              <el-tag 
                v-if="data.menuType" 
                size="small" 
                :type="data.menuType === 'M' ? 'primary' : data.menuType === 'C' ? 'success' : 'warning'"
                style="margin-left: 8px;"
              >
                {{ data.menuType === 'M' ? '目录' : data.menuType === 'C' ? '菜单' : '按钮' }}
              </el-tag>
            </span>
          </template>
        </el-tree>
      </div>

      <template #footer>
        <el-button @click="handleCloseMenuDialog">取消</el-button>
        <el-button type="primary" :loading="menuLoading" @click="handleSubmitMenus">
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
import { Plus, Folder, Document, Key } from '@element-plus/icons-vue'
import { roleApi, type Role, type RoleQueryParams } from '@/api/roles'
import { menuApi } from '@/api/menus'

// 数据定义
const loading = ref(false)
const formLoading = ref(false)
const menuLoading = ref(false)
const roleList = ref<Role[]>([])
const total = ref(0)
const selectedRoles = ref<Role[]>([])

// 表单和对话框
const formDialogVisible = ref(false)
const menuDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const menuTreeRef = ref()

// 搜索表单
const searchForm = reactive<RoleQueryParams>({
  roleName: '',
  roleCode: '',
  status: undefined,
  page: 1,
  size: 20
})

// 角色表单
const form = reactive({
  id: undefined as number | undefined,
  roleCode: '',
  roleName: '',
  description: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20
})

// 权限相关
const currentRole = ref<Role>()
const menuTreeData = ref([])
const selectedMenuIds = ref<number[]>([])

// 计算属性
const isEdit = computed(() => !!form.id)

// 表单验证规则
const rules: FormRules = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '角色名称长度在2-50个字符', trigger: 'blur' }
  ]
}

// 加载角色列表
const loadRoleList = async () => {
  try {
    loading.value = true
    
    const queryParams = {
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    
    const response = await roleApi.list(queryParams)
    
    if (response && response.data) {
      const pageData = response.data
      roleList.value = pageData.data || []
      total.value = pageData.total || 0
    } else {
      roleList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载角色列表失败')
    roleList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadRoleList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    roleName: '',
    roleCode: '',
    status: undefined
  })
  handleSearch()
}

// 新增角色
const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    roleCode: '',
    roleName: '',
    description: ''
  })
  formDialogVisible.value = true
}

// 编辑角色
const handleEdit = (role: Role) => {
  Object.assign(form, {
    id: role.id,
    roleCode: role.roleCode,
    roleName: role.roleName,
    description: role.description
  })
  formDialogVisible.value = true
}

// 删除角色
const handleDelete = async (role: Role) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色"${role.roleName}"吗？`, '删除确认', {
      type: 'warning'
    })
    
    console.log('📤 删除角色:', role.id)
    await roleApi.delete(role.id)
    ElMessage.success('删除成功')
    loadRoleList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ 删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除选中的角色吗？', '批量删除', {
      type: 'warning'
    })
    
    const ids = selectedRoles.value.map(role => role.id)
    console.log('📤 批量删除角色:', ids)
    await roleApi.batchDelete(ids)
    ElMessage.success('批量删除成功')
    loadRoleList()
    selectedRoles.value = []
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ 批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (role: Role) => {
  try {
    if (role.status === 1) {
      await roleApi.enable(role.id)
      ElMessage.success('启用成功')
    } else {
      await roleApi.disable(role.id)
      ElMessage.success('禁用成功')
    }
  } catch (error) {
    console.error('❌ 状态更新失败:', error)
    ElMessage.error('状态更新失败')
    // 恢复原状态
    role.status = role.status === 1 ? 0 : 1
  }
}

// 分配菜单权限
const handleAssignMenus = async (role: Role) => {
  currentRole.value = role
  
  try {
    // 加载菜单树
    const menuResponse = await menuApi.getMenuTree()
    menuTreeData.value = menuResponse.data || []
    
    // 加载当前角色的菜单权限
    const menuIdsResponse = await roleApi.getRoleMenuIds(role.id)
    selectedMenuIds.value = menuIdsResponse.data || []
    
    menuDialogVisible.value = true
  } catch (error) {
    console.error('❌ 加载菜单权限失败:', error)
    ElMessage.error('加载菜单权限失败')
  }
}

// 查看角色用户
const handleViewUsers = (role: Role) => {
  ElMessage.info(`角色"${role.roleName}"下共有 ${role.userCount} 个用户`)
  // TODO: 这里可以跳转到用户列表页面，并过滤显示该角色的用户
}

// 选择变更
const handleSelectionChange = (selection: Role[]) => {
  selectedRoles.value = selection
}

// 分页变更
const handleSizeChange = () => {
  pagination.page = 1
  loadRoleList()
}

const handleCurrentChange = () => {
  loadRoleList()
}

// 检查角色编码
const checkRoleCode = async () => {
  if (!form.roleCode) return
  
  try {
    const response = await roleApi.checkRoleCode(form.roleCode, form.id)
    if (!response.data) {
      ElMessage.warning('角色编码已存在')
    }
  } catch (error) {
    console.error('检查角色编码失败:', error)
  }
}

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      roleCode: form.roleCode,
      roleName: form.roleName,
      description: form.description
    }

    if (isEdit.value && form.id) {
      console.log('📤 更新角色:', form.id, submitData)
      await roleApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      console.log('📤 创建角色:', submitData)
      await roleApi.create(submitData)
      ElMessage.success('创建成功')
    }

    handleCloseDialog()
    loadRoleList()
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

// 提交菜单权限
const handleSubmitMenus = async () => {
  if (!currentRole.value) return
  
  try {
    menuLoading.value = true
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
    const menuIds = [...checkedKeys, ...halfCheckedKeys]
    
    console.log('📤 分配菜单权限:', currentRole.value.id, menuIds)
    await roleApi.assignMenus(currentRole.value.id, menuIds)
    ElMessage.success('权限分配成功')
    handleCloseMenuDialog()
  } catch (error) {
    console.error('❌ 权限分配失败:', error)
    ElMessage.error('权限分配失败')
  } finally {
    menuLoading.value = false
  }
}

// 关闭对话框
const handleCloseDialog = () => {
  formRef.value?.resetFields()
  formDialogVisible.value = false
}

const handleCloseMenuDialog = () => {
  menuDialogVisible.value = false
  currentRole.value = undefined
  selectedMenuIds.value = []
}

// 时间格式化
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

// 初始化
onMounted(() => {
  loadRoleList()
})
</script>

<style lang="scss" scoped>
.form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.role-info {
  margin-bottom: 20px;
}

.menu-tree {
  h4 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
  }
  
  .menu-tree-node {
    display: flex;
    align-items: center;
    font-size: 14px;
  }
}
</style> 