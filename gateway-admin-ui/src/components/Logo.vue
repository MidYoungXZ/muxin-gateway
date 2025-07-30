<template>
  <div class="logo-container" :class="{ 'logo-only': logoOnly }">
    <svg class="logo-icon" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景圆 -->
      <defs>
        <linearGradient id="logo-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
          <stop offset="100%" style="stop-color:#764ba2;stop-opacity:1" />
        </linearGradient>
      </defs>
      
      <!-- 主体设计 - 网关抽象图形 -->
      <g>
        <!-- 外圆 -->
        <circle cx="24" cy="24" r="22" fill="url(#logo-gradient)" opacity="0.1"/>
        
        <!-- 中心节点 -->
        <circle cx="24" cy="24" r="6" fill="url(#logo-gradient)"/>
        
        <!-- 连接线 - 形成网关的概念 -->
        <path d="M24 18 L24 6 M24 30 L24 42 M18 24 L6 24 M30 24 L42 24" 
              stroke="url(#logo-gradient)" 
              stroke-width="2" 
              stroke-linecap="round"/>
        
        <!-- 四角节点 -->
        <circle cx="24" cy="6" r="3" fill="url(#logo-gradient)"/>
        <circle cx="24" cy="42" r="3" fill="url(#logo-gradient)"/>
        <circle cx="6" cy="24" r="3" fill="url(#logo-gradient)"/>
        <circle cx="42" cy="24" r="3" fill="url(#logo-gradient)"/>
        
        <!-- 斜向连接 -->
        <path d="M19 19 L11 11 M29 19 L37 11 M19 29 L11 37 M29 29 L37 37" 
              stroke="url(#logo-gradient)" 
              stroke-width="1.5" 
              stroke-linecap="round"
              opacity="0.6"/>
        
        <!-- 斜向小节点 -->
        <circle cx="11" cy="11" r="2" fill="url(#logo-gradient)" opacity="0.8"/>
        <circle cx="37" cy="11" r="2" fill="url(#logo-gradient)" opacity="0.8"/>
        <circle cx="11" cy="37" r="2" fill="url(#logo-gradient)" opacity="0.8"/>
        <circle cx="37" cy="37" r="2" fill="url(#logo-gradient)" opacity="0.8"/>
      </g>
    </svg>
    
    <span v-if="!logoOnly" class="logo-text">
      <span class="logo-text-main">Muxin</span>
      <span class="logo-text-sub">Gateway</span>
    </span>
  </div>
</template>

<script setup lang="ts">
interface Props {
  logoOnly?: boolean
}

defineProps<Props>()
</script>

<style lang="scss" scoped>
.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  
  &.logo-only {
    justify-content: center;
  }
  
  .logo-icon {
    width: 36px;
    height: 36px;
    flex-shrink: 0;
  }
  
  .logo-text {
    display: flex;
    flex-direction: column;
    line-height: 1.2;
    
    .logo-text-main {
      font-size: 20px;
      font-weight: 700;
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .logo-text-sub {
      font-size: 12px;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
  }
}
</style> 