<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">403</h1>
      <h2 class="error-title">无访问权限</h2>
      <p class="error-desc">抱歉，您没有权限访问此页面</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  
  .error-content {
    text-align: center;
    
    .error-code {
      font-size: 120px;
      font-weight: 700;
      color: var(--warning-color);
      margin: 0;
      line-height: 1;
    }
    
    .error-title {
      font-size: 24px;
      color: var(--text-primary);
      margin: 20px 0 10px;
    }
    
    .error-desc {
      font-size: 14px;
      color: var(--text-tertiary);
      margin-bottom: 30px;
    }
  }
}
</style>