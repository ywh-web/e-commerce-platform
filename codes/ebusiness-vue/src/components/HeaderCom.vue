<template>
  <!-- 后台管理顶栏 -->
  <div class="header-com">
    <!-- 左侧：折叠按钮 + 面包屑 -->
    <div class="left">
      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon :size="18">
          <Fold v-if="!collapsed" />
          <Expand v-else />
        </el-icon>
      </div>

      <el-breadcrumb separator="/" class="crumb">
        <el-breadcrumb-item>管理后台</el-breadcrumb-item>
        <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 右侧：快捷入口 + 管理员 -->
    <div class="right">
      <el-tooltip content="返回前台商城" placement="bottom">
        <div class="icon-btn" @click="goFront">
          <el-icon :size="17"><Monitor /></el-icon>
        </div>
      </el-tooltip>

      <div class="divider"></div>

      <el-dropdown @command="handleCommand">
        <div class="admin-info">
          <div class="avatar">
            <el-icon :size="16"><UserFilled /></el-icon>
          </div>
          <div class="admin-meta">
            <span class="admin-name">{{ adminName }}</span>
            <span class="admin-role">系统管理员</span>
          </div>
          <el-icon :size="12" class="arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="front">
              <el-icon><Monitor /></el-icon> 前往前台商城
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon> 安全退出
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Fold,
  Expand,
  UserFilled,
  ArrowDown,
  SwitchButton,
  Monitor
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['toggle-collapse'])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前页面标题（取自路由 meta）
const pageTitle = computed(() => route.meta.title || '后台管理')

// 管理员名称
const adminName = computed(() => {
  const info = userStore.userInfo
  if (info && info.aname) return info.aname
  return '管理员'
})

const toggleCollapse = () => emit('toggle-collapse')

const goFront = () => router.push('/before/index')

const handleCommand = (command) => {
  if (command === 'front') {
    goFront()
    return
  }
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出后台管理系统吗？', '安全退出', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        userStore.logout()
        ElMessage.success('已安全退出')
        router.push('/adminLogin')
      })
      .catch(() => {})
  }
}
</script>

<style scoped>
.header-com {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 22px;
  background: #fff;
  border-bottom: 1px solid var(--eb-border-color-light);
  box-shadow: var(--eb-shadow-xs);
}

.left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: var(--eb-radius-md);
  color: var(--eb-text-regular);
  cursor: pointer;
  transition: all var(--eb-duration-fast) var(--eb-ease);
}

.collapse-btn:hover {
  color: var(--eb-admin-500);
  background: var(--eb-admin-50);
}

.crumb {
  font-size: var(--eb-font-sm);
}

.crumb :deep(.el-breadcrumb__inner) {
  color: var(--eb-text-secondary);
  font-weight: 400;
}

.crumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--eb-text-primary);
  font-weight: 600;
}

.right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: var(--eb-radius-md);
  color: var(--eb-text-regular);
  cursor: pointer;
  transition: all var(--eb-duration-fast) var(--eb-ease);
}

.icon-btn:hover {
  color: var(--eb-admin-500);
  background: var(--eb-admin-50);
}

.divider {
  width: 1px;
  height: 22px;
  background: var(--eb-border-color);
}

/* ---------- 管理员信息 ---------- */
.admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 12px 5px 5px;
  border-radius: var(--eb-radius-full);
  cursor: pointer;
  outline: none;
  transition: background var(--eb-duration-fast) var(--eb-ease);
}

.admin-info:hover {
  background: var(--eb-admin-50);
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, var(--eb-admin-400), var(--eb-admin-600));
}

.admin-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.admin-name {
  font-size: var(--eb-font-sm);
  font-weight: 600;
  color: var(--eb-text-primary);
}

.admin-role {
  font-size: 11px;
  color: var(--eb-text-secondary);
}

.arrow {
  color: var(--eb-text-secondary);
}
</style>
