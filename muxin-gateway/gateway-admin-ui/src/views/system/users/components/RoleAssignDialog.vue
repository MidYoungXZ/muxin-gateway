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
        <el-descriptions-item label="用户名">{{ userData.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userData.nickname }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userData.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userData.mobile }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="role-selection">
      <h4>选择角色</h4>
      <el-transfer
        v-model="selectedRoleIds"
        :data="roleOptions"
        :titles="['可选角色', '已分配角色']"
        :button-texts="['移除', '添加']"
        :format="{
          noChecked: '${total}',
          hasChecked: '${checked}/${total}'
        }"
        filterable
        filter-placeholder="搜索角色"
        style="text-align: left; display: inline-block"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/users'
import type { User, Role } from '@/types/system'

interface Props {
  modelValue: boolean
  userData: Partial<User>
  roleList: Role[]
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const selectedRoleIds = ref<number[]>([])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 角色选项
const roleOptions = computed(() => {
  return props.roleList.map(role => ({
    key: role.id,
    label: role.roleName,
    disabled: false
  }))
})

// 监听用户数据变化
watch(
  () => props.userData,
  async (newData) => {
    if (newData && newData.id && visible.value) {
      try {
        // 获取用户当前的角色
        const { data } = await userApi.getUserRoleIds(newData.id)
        selectedRoleIds.value = data.data || []
      } catch (error) {
        console.error('获取用户角色失败:', error)
        selectedRoleIds.value = []
      }
    }
  },
  { immediate: true }
)

// 监听对话框显示状态
watch(visible, (newVisible) => {
  if (!newVisible) {
    selectedRoleIds.value = []
  }
})

// 处理关闭
const handleClose = () => {
  visible.value = false
}

// 处理提交
const handleSubmit = async () => {
  if (!props.userData.id) {
    ElMessage.error('用户信息错误')
    return
  }

  try {
    loading.value = true
    await userApi.assignRoles(props.userData.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    emit('success')
    handleClose()
  } catch (error) {
    ElMessage.error('角色分配失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.user-info {
  margin-bottom: 20px;
}

.role-selection {
  h4 {
    margin: 0 0 12px 0;
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
  
  :deep(.el-transfer) {
    .el-transfer-panel {
      width: 200px;
    }
  }
}

.dialog-footer {
  text-align: right;
}
</style> 