<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2>个人中心</h2>
    </div>

    <div class="profile-content">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="avatar-card">
            <template #header>
              <span>头像</span>
            </template>
            <div class="avatar-wrapper">
              <el-avatar :size="120" :src="userInfo.avatar">
                <el-icon :size="60"><User /></el-icon>
              </el-avatar>
              <el-upload
                class="avatar-upload"
                action="#"
                :show-file-list="false"
                :auto-upload="false"
                @change="handleAvatarChange"
              >
                <el-button type="primary" size="small">更换头像</el-button>
              </el-upload>
            </div>
          </el-card>
        </el-col>

        <el-col :span="16">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
                <el-button type="primary" size="small" @click="handleEdit" v-if="!isEditing">
                  编辑资料
                </el-button>
              </div>
            </template>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-width="80px"
              :disabled="!isEditing"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户名">
                    <el-input v-model="userInfo.username" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="form.nickname" placeholder="请输入昵称" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
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

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="部门">
                    <el-input v-model="userInfo.deptName" disabled placeholder="未设置" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="角色">
                    <el-tag
                      v-for="role in userInfo.roles"
                      :key="role"
                      style="margin-right: 8px"
                    >
                      {{ role }}
                    </el-tag>
                    <span v-if="!userInfo.roles?.length">暂无角色</span>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item v-if="isEditing">
                <el-button type="primary" @click="handleSave" :loading="loading">保存</el-button>
                <el-button @click="handleCancel">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="security-card">
        <template #header>
          <span>安全设置</span>
        </template>

        <div class="security-item">
          <div class="security-info">
            <span class="security-label">账号密码</span>
            <span class="security-desc">定期修改密码可以提高账号安全性</span>
          </div>
          <el-button type="primary" size="small" @click="showPasswordDialog = true">
            修改密码
          </el-button>
        </div>
      </el-card>
    </div>

    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi, type User as UserType, type ProfileUpdateRequest, type PasswordUpdateRequest } from '@/api/users'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const passwordLoading = ref(false)
const isEditing = ref(false)
const showPasswordDialog = ref(false)

const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

const userInfo = ref<Partial<UserType>>({})
const form = reactive<ProfileUpdateRequest>({
  nickname: '',
  email: '',
  mobile: '',
  avatar: ''
})

const passwordForm = reactive<PasswordUpdateRequest>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  nickname: [
    { min: 2, max: 20, message: '昵称长度在2-20个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  mobile: [
    { pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadUserInfo = async () => {
  try {
    const response = await userApi.getCurrentUser()
    if (response.data) {
      userInfo.value = response.data
      Object.assign(form, {
        nickname: response.data.nickname || '',
        email: response.data.email || '',
        mobile: response.data.mobile || '',
        avatar: response.data.avatar || ''
      })
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const handleEdit = () => {
  isEditing.value = true
}

const handleCancel = () => {
  isEditing.value = false
  Object.assign(form, {
    nickname: userInfo.value.nickname || '',
    email: userInfo.value.email || '',
    mobile: userInfo.value.mobile || '',
    avatar: userInfo.value.avatar || ''
  })
  formRef.value?.clearValidate()
}

const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    await userApi.updateProfile(form)
    ElMessage.success('保存成功')
    isEditing.value = false

    await loadUserInfo()
    userStore.getUserInfoAction()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAvatarChange = (file: any) => {
  console.log('上传头像:', file)
  ElMessage.info('头像上传功能暂未实现')
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    await userApi.updatePassword(userInfo.value.id!, passwordForm)
    ElMessage.success('密码修改成功，请重新登录')
    showPasswordDialog.value = false

    Object.assign(passwordForm, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    setTimeout(() => {
      userStore.logout()
      window.location.href = '/login'
    }, 1500)
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-container {
  padding: var(--space-6);
  background: var(--bg-secondary);
  min-height: 100%;
}

.profile-header {
  margin-bottom: var(--space-6);

  h2 {
    margin: 0;
    font-size: var(--text-xl);
    font-weight: var(--font-semibold);
    color: var(--text-primary);
  }
}

.profile-content {
  .avatar-card,
  .info-card,
  .security-card {
    margin-bottom: var(--space-6);
  }
}

.avatar-card {
  .avatar-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: var(--space-6);

    .el-avatar {
      margin-bottom: var(--space-4);
      background: var(--primary-color);
    }

    .avatar-upload {
      text-align: center;
    }
  }
}

.info-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.security-card {
  .security-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--space-4);

    .security-info {
      .security-label {
        font-weight: var(--font-medium);
        color: var(--text-primary);
        margin-right: var(--space-4);
      }

      .security-desc {
        font-size: var(--text-sm);
        color: var(--text-secondary);
      }
    }
  }
}
</style>