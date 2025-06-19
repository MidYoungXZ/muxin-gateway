<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <logo />
        <h2 class="login-title">欢迎登录</h2>
        <p class="login-subtitle">Muxin Gateway 智能网关管理系统</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        label-position="top"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <!-- 验证码 - 当需要时显示 -->
        <el-form-item v-if="showCaptcha" label="验证码" prop="captcha">
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              size="large"
              style="flex: 1;"
            />
            <div 
              class="captcha-image" 
              @click="refreshCaptcha"
              style="width: 100px; height: 40px; background: #f0f0f0; border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 14px; color: #666;"
            >
              点击刷新
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
        </el-form-item>
        
        <!-- 账号锁定提示 -->
        <el-form-item v-if="accountLocked">
          <el-alert
            title="账号已被锁定"
            :description="`登录失败次数过多，请 ${formatLockTime} 后重试`"
            type="warning"
            :closable="false"
            show-icon
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="accountLocked"
            @click="handleLogin"
            class="login-button"
          >
            {{ accountLocked ? `锁定中 ${formatLockTime}` : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <el-link type="primary" underline="never">忘记密码？</el-link>
        <el-link type="primary" underline="never">注册账号</el-link>
      </div>
    </div>
    
    <div class="login-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import Logo from '@/components/Logo.vue'

const router = useRouter()
const userStore = useUserStore()

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
})

// 表单验证规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
})

// 状态
const loading = ref(false)
const showCaptcha = ref(false)
const failedAttempts = ref(0)
const accountLocked = ref(false)
const lockTime = ref(0)
const lockTimer = ref<number | null>(null)

// 表单引用
const loginFormRef = ref<any>(null)

// 记住用户名功能
onMounted(() => {
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
  }
})

// 清理定时器
onUnmounted(() => {
  if (lockTimer.value) {
    clearInterval(lockTimer.value)
  }
})

// 登录处理
const handleLogin = async () => {
  if (accountLocked.value) {
    ElMessage.warning('账号已被锁定，请稍后再试')
    return
  }

  if (!loginFormRef.value) return

  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    loading.value = true

    // 模拟验证码检查
    if (showCaptcha.value && !loginForm.captcha) {
      ElMessage.error('请输入验证码')
      loading.value = false
      return
    }

    // 执行登录
    await userStore.loginAction(loginForm)
    
    // 重置失败次数
    failedAttempts.value = 0
    showCaptcha.value = false
    
    // 记住用户名
    if (loginForm.rememberMe) {
      localStorage.setItem('rememberedUsername', loginForm.username)
    } else {
      localStorage.removeItem('rememberedUsername')
    }

    ElMessage.success('登录成功')
    await router.push('/')

  } catch (error: any) {
    console.error('登录失败:', error)
    
    // 增加失败次数
    failedAttempts.value++
    
    // 3次失败后显示验证码
    if (failedAttempts.value >= 3) {
      showCaptcha.value = true
    }
    
    // 5次失败后锁定账号
    if (failedAttempts.value >= 5) {
      lockAccount()
    }
    
    // 显示错误信息
    const message = error.message || '登录失败，请检查用户名和密码'
    ElMessage.error(message)
    
  } finally {
    loading.value = false
  }
}

// 锁定账号
const lockAccount = () => {
  accountLocked.value = true
  lockTime.value = 300 // 5分钟 = 300秒
  
  ElMessage.error(`登录失败次数过多，账号已被锁定 ${lockTime.value} 秒`)
  
  // 倒计时
  lockTimer.value = window.setInterval(() => {
    lockTime.value--
    
    if (lockTime.value <= 0) {
      // 解锁
      accountLocked.value = false
      failedAttempts.value = 0
      showCaptcha.value = false
      
      if (lockTimer.value) {
        clearInterval(lockTimer.value)
        lockTimer.value = null
      }
      
      ElMessage.success('账号已解锁，可以重新登录')
    }
  }, 1000)
}

// 重置表单
const resetForm = () => {
  if (loginFormRef.value) {
    loginFormRef.value.resetFields()
  }
  failedAttempts.value = 0
  showCaptcha.value = false
}

// 刷新验证码
const refreshCaptcha = () => {
  // 刷新验证码逻辑
  console.log('刷新验证码')
}

// 格式化锁定时间
const formatLockTime = computed(() => {
  const minutes = Math.floor(lockTime.value / 60)
  const seconds = lockTime.value % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
})
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-primary);
  position: relative;
  overflow: hidden;
  
  .login-card {
    width: 420px;
    background: var(--card-bg);
    border-radius: var(--radius-xl);
    padding: 40px;
    box-shadow: var(--shadow-xl);
    position: relative;
    z-index: 10;
    
    .login-header {
      text-align: center;
      margin-bottom: 40px;
      
      :deep(.logo-container) {
        justify-content: center;
        margin-bottom: 24px;
        
        .logo-icon {
          width: 64px;
          height: 64px;
        }
      }
      
      .login-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
      }
      
      .login-subtitle {
        font-size: 14px;
        color: var(--text-secondary);
        margin: 0;
      }
    }
    
    .login-form {
      .login-button {
        width: 100%;
        height: 42px;
        font-size: 16px;
        background: var(--gradient-primary);
        border: none;
        
        &:hover {
          opacity: 0.9;
        }
      }
    }
    
    .login-footer {
      display: flex;
      justify-content: space-between;
      margin-top: 20px;
      
      .el-link {
        font-size: 14px;
      }
    }
  }
  
  .login-bg {
    position: absolute;
    inset: 0;
    z-index: 1;
    
    .bg-shape {
      position: absolute;
      border-radius: 50%;
      filter: blur(100px);
      opacity: 0.3;
      animation: float 20s ease-in-out infinite;
      
      &.bg-shape-1 {
        width: 400px;
        height: 400px;
        background: var(--gradient-primary);
        top: -200px;
        right: -200px;
      }
      
      &.bg-shape-2 {
        width: 300px;
        height: 300px;
        background: var(--gradient-secondary);
        bottom: -150px;
        left: -150px;
        animation-delay: -5s;
      }
      
      &.bg-shape-3 {
        width: 500px;
        height: 500px;
        background: var(--gradient-accent);
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        animation-delay: -10s;
      }
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  33% {
    transform: translateY(-20px) rotate(120deg);
  }
  66% {
    transform: translateY(20px) rotate(240deg);
  }
}

// 响应式
@media (max-width: 480px) {
  .login-container {
    padding: 20px;
    
    .login-card {
      width: 100%;
      padding: 30px 20px;
    }
  }
}
</style>