<template>
  <div class="step-plugins">
    <div class="section-title">插件类型筛选</div>
    <div class="type-filter">
      <el-radio-group
        :model-value="filterType"
        @update:model-value="filterType = $event"
      >
        <el-radio-button label="">全部 ({{ plugins.length }})</el-radio-button>
        <el-radio-button label="AUTH">认证鉴权 ({{ authCount }})</el-radio-button>
        <el-radio-button label="FILTER">请求处理 ({{ filterCount }})</el-radio-button>
      </el-radio-group>
    </div>
    <div class="type-tip">
      注：路由匹配由断言器(Predicate)完成，已在 Step 2 配置
    </div>

    <div class="section-title">可选插件</div>
    
    <template v-if="filteredPlugins.length > 0">
      <div class="plugin-category" v-for="type in ['AUTH', 'FILTER']" :key="type">
        <div class="category-title" v-if="getPluginsByType(type).length > 0 && (filterType === '' || filterType === type)">
          {{ type === 'AUTH' ? '认证鉴权' : '请求处理' }} ({{ type }})
        </div>
        <div class="plugin-grid" v-if="filterType === '' || filterType === type">
          <div
            v-for="plugin in getPluginsByType(type)"
            :key="plugin.id"
            class="plugin-card"
            :class="{
              configured: isPluginConfigured(plugin.id),
              disabled: !plugin.enabled
            }"
          >
            <div class="plugin-header">
              <span class="plugin-icon">{{ type === 'AUTH' ? '🔐' : '⚡' }}</span>
              <span class="plugin-name">{{ plugin.pluginName }}</span>
            </div>
            <div class="plugin-desc">{{ plugin.description }}</div>
            <div class="plugin-meta">
              <el-tag size="small" :type="type === 'AUTH' ? 'warning' : ''">{{ type }}</el-tag>
              <span class="plugin-priority">优先级: {{ plugin.defaultPriority }}</span>
            </div>
            <div class="plugin-status" v-if="isPluginConfigured(plugin.id)">
              <el-tag type="success" size="small">
                <el-icon><Check /></el-icon> 已配置
              </el-tag>
              <span class="config-summary">{{ getConfigSummary(plugin.id) }}</span>
            </div>
            <div class="plugin-actions">
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
            </div>
          </div>
        </div>
      </div>
    </template>
    <el-empty v-else description="暂无可配置的插件" :image-size="60" />

    <div class="section-title" v-if="modelValue.plugins.length > 0">
      已配置插件列表（按执行优先级排序）
    </div>
    <div class="plugin-list" v-if="modelValue.plugins.length > 0">
      <div class="list-tip">执行顺序: AUTH阶段 → FILTER阶段 → 转发到后端</div>
      <el-table :data="sortedPlugins" size="small">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="pluginName" label="插件名称" />
        <el-table-column prop="pluginType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.pluginType === 'AUTH' ? 'warning' : ''">
              {{ row.pluginType }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editPluginById(row.pluginId)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="list-footer">↑ 优先级数值越大越先执行，同类型插件按优先级排序</div>
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
import { Check } from '@element-plus/icons-vue'
import type { RouteFormState, RoutePlugin, PluginInfo } from '@/api/routes'
import { pluginsApi } from '@/api/routes'
import PluginConfigDrawer from './PluginConfigDrawer.vue'

const props = defineProps<{
  modelValue: RouteFormState
}>()

const emit = defineEmits<{
  'update:modelValue': [value: RouteFormState]
}>()

const filterType = ref('')
const plugins = ref<PluginInfo[]>([])
const drawerVisible = ref(false)
const currentPlugin = ref<PluginInfo | null>(null)
const currentConfig = ref<Record<string, any>>({})

const authCount = computed(() => plugins.value.filter(p => p.pluginType === 'AUTH').length)
const filterCount = computed(() => plugins.value.filter(p => p.pluginType === 'FILTER').length)

const filteredPlugins = computed(() => {
  if (!filterType.value) return plugins.value
  return plugins.value.filter(p => p.pluginType === filterType.value)
})

const sortedPlugins = computed(() => {
  return [...props.modelValue.plugins].sort((a, b) => {
    const priorityA = getEffectivePriority(a)
    const priorityB = getEffectivePriority(b)
    return priorityB - priorityA
  })
})

function getPluginsByType(type: string): PluginInfo[] {
  return filteredPlugins.value.filter(p => p.pluginType === type)
}

function isPluginConfigured(pluginId: number): boolean {
  return props.modelValue.plugins.some(p => p.pluginId === pluginId)
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
    if (res?.data) {
      plugins.value = res.data.filter(p => p.pluginType !== 'MATCH')
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
  const plugins = props.modelValue.plugins.filter(p => p.pluginId !== pluginId)
  emit('update:modelValue', { ...props.modelValue, plugins })
}

function savePluginConfig(config: Record<string, any>, priorityOverride?: number) {
  if (!currentPlugin.value) return
  
  const newPlugin: RoutePlugin = {
    pluginId: currentPlugin.value.id,
    pluginName: currentPlugin.value.pluginName,
    pluginType: currentPlugin.value.pluginType as 'AUTH' | 'FILTER',
    config,
    priorityOverride,
    enabled: true
  }
  
  const existingIndex = props.modelValue.plugins.findIndex(
    p => p.pluginId === currentPlugin.value!.id
  )
  
  let plugins: RoutePlugin[]
  if (existingIndex >= 0) {
    plugins = [...props.modelValue.plugins]
    plugins[existingIndex] = newPlugin
  } else {
    plugins = [...props.modelValue.plugins, newPlugin]
  }
  
  emit('update:modelValue', { ...props.modelValue, plugins })
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
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.type-filter {
  margin-bottom: 8px;
}

.type-tip {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-bottom: 16px;
}

.plugin-category {
  margin-bottom: 24px;
}

.category-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
  margin-bottom: 12px;
}

.plugin-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.plugin-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 16px;
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary-light-5);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }

  &.configured {
    border-color: var(--el-color-success-light-5);
    background: var(--el-color-success-light-9);
  }

  &.disabled {
    opacity: 0.6;
  }
}

.plugin-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.plugin-icon {
  font-size: 18px;
}

.plugin-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.plugin-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.plugin-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.plugin-priority {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.plugin-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.config-summary {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.plugin-actions {
  display: flex;
  gap: 8px;
}

.plugin-list {
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  padding: 16px;
}

.list-tip {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 12px;
}

.list-footer {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-top: 8px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}
</style>