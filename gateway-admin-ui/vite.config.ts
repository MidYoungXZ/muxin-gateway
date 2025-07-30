import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    
    // 自动导入 Vue 和 Element Plus API
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: [
        'vue',
        'vue-router',
        'pinia',
        {
          'element-plus': [
            'ElMessage',
            'ElMessageBox',
            'ElNotification',
            'ElLoading'
          ]
        }
      ],
      // 自动导入 stores 目录
      dirs: [
        'src/stores',
        'src/composables'
      ],
      dts: 'src/auto-imports.d.ts',
    }),
    
    // 自动导入组件
    Components({
      resolvers: [
        ElementPlusResolver({
          // 不自动导入样式，统一在main.ts中导入
          importStyle: false,
        }),
        // Element Plus 图标自动导入
        (componentName) => {
          // 处理 Element Plus 图标组件
          if (componentName.match(/^(Arrow|ArrowDown|Document|Edit|Delete|Plus|Minus|Close|Check|Search|Setting|User|Lock|House|Monitor|Connection|Warning|Timer|TrendCharts|Bottom|FullScreen|CircleCheck|CircleClose|Bell|Operation|UserFilled|Cpu|More|SwitchButton|Expand|Fold|Aim)$/)) {
            return {
              name: componentName,
              from: '@element-plus/icons-vue'
            }
          }
        }
      ],
      dirs: ['src/components'],
      dts: 'src/components.d.ts',
    }),
  ],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/styles/variables.scss" as *;`,
        api: 'modern-compiler',
      },
    },
  },
  
  server: {
    host: '0.0.0.0',
    port: 3000,
    open: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log(`🔀 代理请求: ${req.method} ${req.url} -> ${proxyReq.getHeader('host')}${proxyReq.path}`)
          })
          proxy.on('proxyRes', (proxyRes, req) => {
            console.log(`🔙 代理响应: ${req.url} -> ${proxyRes.statusCode}`)
          })
          proxy.on('error', (err, req) => {
            console.error(`❌ 代理错误: ${req.url}`, err.message)
          })
        }
      }
    }
  },
  
  build: {
    target: 'es2015',
    outDir: 'dist',
    sourcemap: false,
    minify: 'terser',
    
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
        pure_funcs: ['console.log', 'console.warn'],
        dead_code: true,
        unused: true,
      },
      mangle: {
        safari10: true,
      },
    },
    
    rollupOptions: {
      output: {
        // 简化的分块策略
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus', '@element-plus/icons-vue'],
          'utils': ['axios', 'dayjs', 'lodash-es'],
        },
        
        chunkFileNames: 'assets/js/[name]-[hash:8].js',
        entryFileNames: 'assets/js/[name]-[hash:8].js',
        assetFileNames: 'assets/[ext]/[name]-[hash:8].[ext]',
      },
    },
    
    chunkSizeWarningLimit: 1000,
  },
  
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'element-plus/es',
      'element-plus/es/locale/lang/zh-cn',
      '@element-plus/icons-vue',
      'axios',
      'dayjs',
      'lodash-es'
    ],
  },
  
  define: {
    __VUE_OPTIONS_API__: true,
    __VUE_PROD_DEVTOOLS__: false,
  },
}) 