<template>
  <el-dialog
    v-model="visible"
    title="分配角色"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="user-info">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userData?.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userData?.nickname }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="role-selection" style="margin-top: 20px;">
      <h4>选择角色</h4>
      <el-checkbox-group v-model="selectedRoleIds">
        <div class="role-list">
          <el-checkbox
            v-for="role in roleList"
            :key="role.id"
            :label="role.id"
            class="role-item"
          >
            <div class="role-info">
              <div class="role-name">{{ role.roleName }}</div>
              <div class="role-desc">{{ role.description }}</div>
            </div>
          </el-checkbox>
        </div>
      </el-checkbox-group>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/users'
import type { User, Role } from '@/types/system'

interface Props {
  modelValue: boolean
  userData?: Partial<User>
  roleList: Role[]
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  userData: () => ({})
})

const emit = defineEmits<Emits>()

// 响应式数据
const loading = ref(false)
const selectedRoleIds = ref<number[]>([])

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 监听用户数据变化
watch(
  () => props.userData,
  (userData) => {
    if (userData?.roles) {
      selectedRoleIds.value = userData.roles.map(role => role.id)
    } else {
      selectedRoleIds.value = []
    }
  },
  { immediate: true, deep: true }
)

// 方法
const handleSubmit = async () => {
  if (!props.userData?.id) return

  try {
    loading.value = true
    await userApi.assignRoles(props.userData.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    emit('success')
  } catch (error) {
    console.error('角色分配失败:', error)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  selectedRoleIds.value = []
  visible.value = false
}
</script>

<style lang="scss" scoped>
.user-info {
  margin-bottom: 20px;
}

.role-selection {
  h4 {
    margin: 0 0 15px 0;
    color: var(--text-primary);
    font-weight: 600;
  }

  .role-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-height: 300px;
    overflow-y: auto;

    .role-item {
      margin: 0;
      padding: 12px;
      border: 1px solid var(--border-primary);
      border-radius: 8px;
      transition: all 0.3s;

      &:hover {
        border-color: var(--primary-color);
        background-color: var(--primary-50);
      }

      :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
        color: var(--primary-color);
      }

      .role-info {
        .role-name {
          font-weight: 500;
          color: var(--text-primary);
          margin-bottom: 4px;
        }

        .role-desc {
          font-size: 12px;
          color: var(--text-secondary);
          line-height: 1.4;
        }
      }
    }
  }
}
</style> 