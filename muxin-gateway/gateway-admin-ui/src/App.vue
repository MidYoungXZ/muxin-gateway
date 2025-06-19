<template>
  <router-view />
</template>

<script setup lang="ts">
// App.vue 是应用的根组件
// 在组件挂载时确保应用亮色主题
onMounted(() => {
  // 确保使用亮色主题
  document.documentElement.classList.remove('dark')
  document.documentElement.classList.add('light')
  
  // 同时设置body的类名
  document.body.classList.remove('dark')
  document.body.classList.add('light')
  
  // 设置默认背景色，避免闪烁
  document.body.style.backgroundColor = '#ffffff'
  document.body.style.color = '#111827'
})
</script>

<style>
#app {
  height: 100%;
  background-color: var(--bg-primary, #ffffff);
  color: var(--text-primary, #111827);
}

/* 确保整个应用使用亮色主题 */
html, body {
  background-color: #ffffff !important;
  color: #111827 !important;
}

/* 覆盖任何可能的暗色主题样式 */
html.dark, body.dark {
  background-color: #ffffff !important;
  color: #111827 !important;
}
</style>
