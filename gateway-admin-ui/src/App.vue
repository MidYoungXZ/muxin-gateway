<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
#app {
  height: 100%;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: background-color 0.5s ease, color 0.3s ease;
}

html.dark #app,
body.dark #app {
  background: linear-gradient(135deg, #0a0a0a 0%, #111111 35%, #0d0d15 70%, #0a0a0a 100%);
  min-height: 100vh;
  position: relative;
}

html.dark #app::before,
body.dark #app::before {
  content: '';
  position: fixed;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: 
    radial-gradient(ellipse 600px 400px at 20% 30%, rgba(139, 92, 246, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse 500px 350px at 80% 70%, rgba(124, 58, 237, 0.06) 0%, transparent 50%),
    radial-gradient(ellipse 400px 300px at 50% 50%, rgba(167, 139, 250, 0.04) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
  animation: bgFloat 30s ease-in-out infinite;
}

html.dark #app::after,
body.dark #app::after {
  content: '';
  position: fixed;
  inset: 0;
  background: 
    radial-gradient(circle at 10% 90%, rgba(139, 92, 246, 0.05) 0%, transparent 40%),
    radial-gradient(circle at 90% 10%, rgba(124, 58, 237, 0.04) 0%, transparent 40%);
  pointer-events: none;
  z-index: 0;
}

@keyframes bgFloat {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(2%, 1%) rotate(1deg);
  }
  66% {
    transform: translate(-1%, 2%) rotate(-1deg);
  }
}
</style>