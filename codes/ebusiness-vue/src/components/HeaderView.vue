<template>
  <!-- 电商前台顶部导航栏 -->
  <header class="eb-header">
    <div class="eb-container inner">
      <!-- 左侧：Logo -->
      <div class="logo" @click="goHome">
        <div class="logo-badge">
          <el-icon :size="20"><Shop /></el-icon>
        </div>
        <span class="logo-text">电子商务平台</span>
      </div>

      <!-- 中间：主导航 -->
      <nav class="nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          @click="handleNav(item, $event)"
        >
          {{ item.label }}
          <span v-if="isActive(item.path)" class="nav-underline"></span>
        </router-link>
      </nav>

      <!-- 右侧：购物车 + 用户区 -->
      <div class="actions">
        <!-- 购物车（带角标） -->
        <el-badge
          :value="cartCount"
          :hidden="!userStore.isLogin || cartCount === 0"
          :max="99"
          class="cart-badge"
        >
          <div class="action-btn" @click="handleNav({ path: '/before/myCart', auth: true })">
            <el-icon :size="18"><ShoppingCart /></el-icon>
            <span>购物车</span>
          </div>
        </el-badge>

        <!-- 已登录 -->
        <el-dropdown v-if="userStore.isLogin" trigger="click" @command="handleCommand">
          <div class="user-chip">
            <div class="avatar">{{ avatarText }}</div>
            <span class="user-name">{{ displayName }}</span>
            <el-icon :size="12"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="/before/myselfInfo">
                <el-icon><User /></el-icon> 个人信息
              </el-dropdown-item>
              <el-dropdown-item command="/before/myOrder">
                <el-icon><List /></el-icon> 我的订单
              </el-dropdown-item>
              <el-dropdown-item command="/before/myFocus">
                <el-icon><Star /></el-icon> 我的收藏
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon> 安全退出
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 未登录 -->
        <template v-else>
          <el-button text class="btn-login" @click="goLogin">登录</el-button>
          <el-button type="primary" class="btn-register" @click="goRegister">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Shop,
  ShoppingCart,
  User,
  List,
  Star,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { beforeApi } from '@/utils/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const cartCount = ref(0)

// 主导航配置
const navItems = [
  { path: '/before/index', label: '首页' },
  { path: '/before/myOrder', label: '我的订单', auth: true },
  { path: '/before/myFocus', label: '我的收藏', auth: true },
  { path: '/before/myselfInfo', label: '个人信息', auth: true }
]

// 当前高亮
const isActive = (path) => route.path === path

// 显示名：直接用 store 的 getter（昵称 > 邮箱前缀）
const displayName = computed(() => userStore.displayName)

// 头像文字：取显示名首字
const avatarText = computed(() => (displayName.value || 'U').charAt(0).toUpperCase())

const goHome = () => router.push('/before/index')
const goLogin = () => router.push({ path: '/login', query: { redirect: route.fullPath } })
const goRegister = () => router.push('/before/register')

// 导航点击：需要登录的页面未登录时先跳登录
const handleNav = (item, e) => {
  if (item.auth && !userStore.isLogin) {
    if (e) e.preventDefault()
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: item.path } })
    return
  }
  router.push(item.path)
}

// 下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else {
    router.push(command)
  }
}

// 安全退出
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      userStore.logout()
      ElMessage.success('已安全退出')
      cartCount.value = 0
      router.push('/before/index')
    })
    .catch(() => {})
}

// 查询购物车件数
const loadCartCount = async () => {
  if (!userStore.isLogin) {
    cartCount.value = 0
    return
  }
  try {
    const res = await beforeApi.getCartCount()
    cartCount.value = res.data || 0
  } catch (e) {
    cartCount.value = 0
  }
}

onMounted(loadCartCount)

// 登录态变化时刷新角标
watch(() => userStore.isLogin, loadCartCount)
// 路由变化时刷新（例如加购后回到首页）
watch(() => route.fullPath, loadCartCount)
</script>

<style scoped>
.eb-header {
  position: sticky;
  top: 0;
  z-index: var(--eb-z-header);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: saturate(180%) blur(12px);
  border-bottom: 1px solid var(--eb-border-color-light);
  box-shadow: var(--eb-shadow-xs);
}

.inner {
  display: flex;
  align-items: center;
  height: var(--eb-header-height);
  gap: var(--eb-space-lg);
}

/* ---------- Logo ---------- */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  white-space: nowrap;
  user-select: none;
}

.logo-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--eb-radius-md);
  color: #fff;
  background: linear-gradient(135deg, var(--eb-brand-400), var(--eb-brand-600));
  box-shadow: 0 4px 12px rgba(225, 37, 27, 0.32);
}

.logo-text {
  font-size: var(--eb-font-lg);
  font-weight: 700;
  letter-spacing: -0.3px;
  color: var(--eb-text-primary);
}

/* ---------- 主导航 ---------- */
.nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.nav-item {
  position: relative;
  padding: 8px 14px;
  font-size: var(--eb-font-md);
  font-weight: 500;
  color: var(--eb-text-regular);
  border-radius: var(--eb-radius-md);
  transition: all var(--eb-duration-fast) var(--eb-ease);
}

.nav-item:hover {
  color: var(--eb-brand-500);
  background: var(--eb-brand-50);
}

.nav-item.active {
  color: var(--eb-brand-500);
  font-weight: 600;
}

.nav-underline {
  position: absolute;
  left: 50%;
  bottom: -1px;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  border-radius: var(--eb-radius-full);
  background: var(--eb-brand-500);
}

/* ---------- 右侧操作区 ---------- */
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 7px 12px;
  font-size: var(--eb-font-sm);
  color: var(--eb-text-regular);
  border-radius: var(--eb-radius-md);
  cursor: pointer;
  transition: all var(--eb-duration-fast) var(--eb-ease);
}

.action-btn:hover {
  color: var(--eb-brand-500);
  background: var(--eb-brand-50);
}

.cart-badge :deep(.el-badge__content) {
  border: none;
  background: var(--eb-brand-500);
  box-shadow: 0 2px 6px rgba(225, 37, 27, 0.4);
}

/* ---------- 用户区 ---------- */
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px 5px 5px;
  border-radius: var(--eb-radius-full);
  background: var(--eb-bg-page);
  cursor: pointer;
  outline: none;
  transition: all var(--eb-duration-fast) var(--eb-ease);
}

.user-chip:hover {
  background: var(--eb-brand-50);
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: var(--eb-font-sm);
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, var(--eb-brand-400), var(--eb-brand-600));
}

.user-name {
  font-size: var(--eb-font-sm);
  font-weight: 500;
  color: var(--eb-text-primary);
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.btn-login {
  font-weight: 500;
}

.btn-register {
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(225, 37, 27, 0.28);
}

/* ---------- 响应式适配（桌面分辨率）---------- */
@media (max-width: 1280px) {
  .inner {
    gap: var(--eb-space-md);
  }
}

@media (max-width: 1024px) {
  .inner {
    gap: var(--eb-space-sm);
  }
}
</style>
