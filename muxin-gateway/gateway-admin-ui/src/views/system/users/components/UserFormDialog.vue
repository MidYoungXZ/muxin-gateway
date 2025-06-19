<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
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
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="昵称" prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="请输入昵称"
              clearable
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
              clearable
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
              clearable
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
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" prop="mobile">
            <el-input
              v-model="form.mobile"
              placeholder="请输入手机号"
              clearable
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属部门" prop="deptId">
            <el-tree-select
              v-model="form.deptId"
              :data="deptTree"
              :props="{ label: 'deptName', value: 'id' }"
              placeholder="请选择部门"
              clearable
              check-strictly
              :render-after-expand="false"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="角色" prop="roleIds">
        <el-select
          v-model="form.roleIds"
          multiple
          placeholder="请选择角色"
          style="width: 100%"
          clearable
        >
          <el-option
            v-for="role in roleList"
            :key="role.id"
            :label="role.roleName"
            :value="role.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="头像">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
          action="#"
          :auto-upload="false"
        >
          <img v-if="form.avatar" :src="form.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { userApi } from '@/api/users'
import type { User, Role, DepartmentTree } from '@/types/system'

interface Props {
  modelValue: boolean
  userData: Partial<User>
  deptTree: DepartmentTree[]
  roleList: Role[]
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref()
const loading = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEdit = computed(() => !!props.userData.id)

// 表单数据
const form = reactive({
  id: 0,
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  mobile: '',
  avatar: '',
  deptId: undefined as number | undefined,
  status: 1 as 0 | 1,
  roleIds: [] as number[]
})

// 表单验证规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: !isEdit.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: !isEdit.value, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (value !== form.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email' as const, message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  mobile: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  deptId: [
    { required: true, message: '请选择所属部门', trigger: 'change' }
  ]
})

// 监听用户数据变化
watch(
  () => props.userData,
  (newData) => {
    if (newData && visible.value) {
      Object.assign(form, {
        id: newData.id || 0,
        username: newData.username || '',
        nickname: newData.nickname || '',
        email: newData.email || '',
        mobile: newData.mobile || '',
        avatar: newData.avatar || '',
        deptId: newData.deptId,
        status: (newData.status ?? 1) as 0 | 1,
        roleIds: newData.roles?.map(role => role.id) || [],
        password: '',
        confirmPassword: ''
      })
    }
  },
  { immediate: true }
)

// 监听对话框显示状态
watch(visible, (newVisible) => {
  if (newVisible) {
    nextTick(() => {
      formRef.value?.clearValidate()
    })
  } else {
    resetForm()
  }
})

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: 0,
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    email: '',
    mobile: '',
    avatar: '',
    deptId: undefined,
    status: 1 as 0 | 1,
    roleIds: []
  })
}

// 处理关闭
const handleClose = () => {
  visible.value = false
}

// 处理提交
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true

    if (isEdit.value) {
      // 编辑用户
      const updateData = {
        nickname: form.nickname,
        email: form.email,
        mobile: form.mobile,
        avatar: form.avatar,
        deptId: form.deptId,
        status: form.status
      }
      await userApi.update(form.id, updateData)
      
      // 分配角色
      if (form.roleIds.length > 0) {
        await userApi.assignRoles(form.id, form.roleIds)
      }
      
      ElMessage.success('用户更新成功')
    } else {
      // 新增用户
      const createData = {
        username: form.username,
        password: form.password,
        nickname: form.nickname,
        email: form.email,
        mobile: form.mobile,
        avatar: form.avatar,
        deptId: form.deptId,
        status: form.status,
        roleIds: form.roleIds
      }
      await userApi.create(createData)
      ElMessage.success('用户创建成功')
    }

    emit('success')
    handleClose()
  } catch (error) {
    ElMessage.error(isEdit.value ? '用户更新失败' : '用户创建失败')
  } finally {
    loading.value = false
  }
}

// 处理头像上传成功
const handleAvatarSuccess = (response: any) => {
  form.avatar = response.data.url
}

// 头像上传前验证
const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  return true
}
</script>

<style lang="scss" scoped>
.avatar-uploader {
  .avatar {
    width: 78px;
    height: 78px;
    display: block;
    border-radius: 6px;
  }
  
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 78px;
    height: 78px;
    text-align: center;
    line-height: 78px;
  }
}

.dialog-footer {
  text-align: right;
}
</style> 