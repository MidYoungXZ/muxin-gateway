<template>
  <div class="step-plugins">
    <div class="section-title">可选插件</div>
    
    <template v-if="plugins.length > 0">
      <div class="plugin-grid">
        <div
          v-for="plugin in plugins"
          :key="plugin.id"
          class="plugin-card"
          :class="{
            configured: isPluginConfigured(plugin.id),
            disabled: !plugin.enabled
          }"
        >
          <div class="plugin-header">
            <div class="plugin-title">
              <span class="plugin-icon">⚡</span>
              <span class="plugin-name">{{ plugin.pluginName }}</span>
              <el-tag v-if="isRecommendedPlugin(plugin.pluginName)" type="warning" size="small" class="recommend-tag">推荐</el-tag>
            </div>
            <div v-if="isPluginConfigured(plugin.id)" class="plugin-status-badge">
              <el-icon class="status-icon"><CircleCheckFilled /></el-icon>
              <span class="status-text">已配置</span>
            </div>
          </div>
          
          <div class="plugin-desc">{{ plugin.description }}</div>
          
          <div class="plugin-meta">
            <el-tag size="small">{{ getPhaseLabel(plugin.phase) }}</el-tag>
            <span class="plugin-priority">优先级: {{ plugin.defaultPriority }}</span>
          </div>
          
          <div class="plugin-actions">
            <template v-if="!readonly">
              <template v-if="isPluginConfigured(plugin.id)">
                <el-button size="small" @click="editPlugin(plugin)">编辑</el-button>
                <el-button size="small" type="danger" link @click="removePlugin(plugin.id)">删除</el-button>
              </template>
              <template v-else>
                <el-button
                  size="small"
                  type="primary"
                  @click="selectPlugin(plugin)"
                  :disabled="!plugin.enabled"
                >
                  选择+配置
                </el-button>
              </template>
            </template>
          </div>
        </div>
      </div>
    </template>
    <el-empty v-else description="暂无可配置的插件" :image-size="60" />

    <div class="section-title" v-if="modelValue.plugins.length > 0">
      已配置插件列表（按执行优先级排序）
    </div>
    <div class="plugin-list" v-if="modelValue.plugins.length > 0">
      <div class="list-tip">执行顺序: 前置插件 → 转发到后端 → 后置插件</div>
      <el-table :data="sortedPlugins" size="small">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="pluginName" label="插件名称" />
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            {{ getEffectivePriority(row) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配置摘要">
          <template #default="{ row }">
            <span class="config-summary">{{ getConfigSummary(row.pluginId) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!readonly" label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editPluginById(row.pluginId)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="list-footer">↑ 优先级数值越大越先执行</div>
    </div>

    <PluginConfigDrawer
      v-model="drawerVisible"
      :plugin="currentPlugin"
      :config="currentConfig"
      @save="savePluginConfig"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Check, CircleCheckFilled } from '@element-plus/icons-vue'
import type { RouteFormState, RoutePlugin } from '@/api/routes'
import { pluginsApi, type PluginInfo } from '@/api/plugins'
import PluginConfigDrawer from './PluginConfigDrawer.vue'

const props = defineProps<{
  modelValue: RouteFormState
  readonly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: RouteFormState]
}>()

const plugins = ref<PluginInfo[]>([])
const drawerVisible = ref(false)
const currentPlugin = ref<PluginInfo | null>(null)
const currentConfig = ref<Record<string, any>>({})

const sortedPlugins = computed(() => {
  return [...props.modelValue.plugins].sort((a, b) => {
    const priorityA = getEffectivePriority(a)
    const priorityB = getEffectivePriority(b)
    return priorityB - priorityA
  })
})

function isPluginConfigured(pluginId: number): boolean {
  return props.modelValue.plugins.some(p => p.pluginId === pluginId)
}

function isRecommendedPlugin(pluginName: string): boolean {
  const recommendedPlugins = ['timeout', 'request-rewrite']
  return recommendedPlugins.includes(pluginName)
}

function getPhaseLabel(phase: string | undefined): string {
  if (phase === 'FILTER_POST') return '后置插件'
  return '前置插件'
}

function getConfigSummary(pluginId: number): string {
  const plugin = props.modelValue.plugins.find(p => p.pluginId === pluginId)
  if (!plugin || !plugin.config) return ''
  const keys = Object.keys(plugin.config).slice(0, 2)
  return keys.map(k => `${k}: ${plugin.config[k]}`).join(', ')
}

function getEffectivePriority(plugin: RoutePlugin): number {
  return plugin.priorityOverride || 
    plugins.value.find(p => p.id === plugin.pluginId)?.defaultPriority || 0
}

async function loadPlugins() {
  try {
    const res = await pluginsApi.list()
    if (res?.data?.data) {
      plugins.value = res.data.data.filter(p => p.pluginType === 'FILTER' && p.enabled === true)
    }
  } catch (error) {
    console.error('加载插件列表失败', error)
  }
}

function selectPlugin(plugin: PluginInfo) {
  currentPlugin.value = plugin
  currentConfig.value = { ...plugin.defaultConfig } || {}
  drawerVisible.value = true
}

function editPlugin(plugin: PluginInfo) {
  currentPlugin.value = plugin
  const existing = props.modelValue.plugins.find(p => p.pluginId === plugin.id)
  currentConfig.value = existing?.config || { ...plugin.defaultConfig } || {}
  drawerVisible.value = true
}

function editPluginById(pluginId: number) {
  const plugin = plugins.value.find(p => p.id === pluginId)
  if (plugin) editPlugin(plugin)
}

function removePlugin(pluginId: number) {
  const newPlugins = props.modelValue.plugins.filter(p => p.pluginId !== pluginId)
  emit('update:modelValue', { ...props.modelValue, plugins: newPlugins })
}

function savePluginConfig(config: Record<string, any>, priorityOverride?: number) {
  if (!currentPlugin.value) return
  
  const newPlugin: RoutePlugin = {
    pluginId: currentPlugin.value.id,
    pluginName: currentPlugin.value.pluginName,
    pluginType: 'FILTER',
    config,
    priorityOverride,
    enabled: true
  }
  
  const existingIndex = props.modelValue.plugins.findIndex(
    p => p.pluginId === currentPlugin.value!.id
  )
  
  let newPlugins: RoutePlugin[]
  if (existingIndex >= 0) {
    newPlugins = [...props.modelValue.plugins]
    newPlugins[existingIndex] = newPlugin
  } else {
    newPlugins = [...props.modelValue.plugins, newPlugin]
  }
  
  emit('update:modelValue', { ...props.modelValue, plugins: newPlugins })
  drawerVisible.value = false
}

onMounted(() => {
  loadPlugins()
})

defineExpose({ validate: () => Promise.resolve(true) })
</script>

<style lang="scss" scoped>
.step-plugins {
  padding: 0 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--border-primary);
}

.plugin-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.plugin-card {
  background: var(--card-bg);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 12px;
  transition: all 0.2s ease;
  height: 160px;
  display: flex;
  flex-direction: column;

  &:hover {
    border-color: var(--el-color-primary-light-5);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }

  &.configured {
    border-color: var(--el-color-success-light-5);
    background: var(--bg-tertiary);
    
    .status-icon {
      color: var(--el-color-success);
    }
    
    .status-text {
      color: var(--el-color-success);
    }
  }

  &.disabled {
    opacity: 0.6;
  }
}

.plugin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  min-height: 24px;
}

.plugin-title {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

.plugin-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.plugin-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-tag {
  margin-left: 8px;
  font-size: 11px;
}

.plugin-status-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  
  .status-icon {
    font-size: 14px;
  }
  
  .status-text {
    font-size: 12px;
    font-weight: 500;
  }
}

.plugin-desc {
  font-size: 12px;
  color: var(--text-tertiary);
  line-height: 1.4;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0;
  min-height: 17px;
}

.plugin-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  flex-shrink: 0;
  min-height: 24px;
}

.plugin-priority {
  font-size: 11px;
  color: var(--text-tertiary);
}

.plugin-actions {
  display: flex;
  gap: 6px;
  margin-top: auto;
  flex-shrink: 0;
  min-height: 32px;
  align-items: center;
}

.plugin-list {
  background: var(--bg-tertiary);
  border-radius: 8px;
  padding: 12px;
}

.list-tip {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 8px;
}

.list-footer {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 6px;
}
</style>