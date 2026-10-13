<template>
  <!-- 后台管理侧边栏 -->
  <div class="sidebar-wrap">
    <el-menu
      class="sidebar-menu"
      :default-active="activeIndex"
      :collapse="collapsed"
      :collapse-transition="false"
      background-color="transparent"
      text-color="#a8bccc"
      active-text-color="#ffffff"
      router
    >
      <!-- 顶部 Logo 区 -->
      <div class="sidebar-logo">
        <div class="logo-badge">
          <el-icon :size="20"><Shop /></el-icon>
        </div>
        <transition name="fade">
          <div v-show="!collapsed" class="logo-text">
            <strong>电商管理后台</strong>
            <span>Management</span>
          </div>
        </transition>
      </div>

      <div v-show="!collapsed" class="menu-group-label">数据概览</div>
      <el-menu-item index="/admin/home">
        <el-icon><HomeFilled /></el-icon>
        <template #title>后台主页</template>
      </el-menu-item>

      <div v-show="!collapsed" class="menu-group-label">商品管理</div>
      <el-menu-item index="/admin/typeManage">
        <el-icon><Menu /></el-icon>
        <template #title>商品类型管理</template>
      </el-menu-item>
      <el-menu-item index="/admin/goodsManage">
        <el-icon><Goods /></el-icon>
        <template #title>商品管理</template>
      </el-menu-item>

      <div v-show="!collapsed" class="menu-group-label">交易管理</div>
      <el-menu-item index="/admin/orderManage">
        <el-icon><List /></el-icon>
        <template #title>订单管理</template>
      </el-menu-item>
      <el-menu-item index="/admin/userManage">
        <el-icon><User /></el-icon>
        <template #title>用户管理</template>
      </el-menu-item>

      <div v-show="!collapsed" class="menu-group-label">统计分析</div>
      <el-menu-item index="/admin/salesStatistics">
        <el-icon><Histogram /></el-icon>
        <template #title>销售统计</template>
      </el-menu-item>
      <el-menu-item index="/admin/orderStatistics">
        <el-icon><PieChart /></el-icon>
        <template #title>订单统计</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Shop,
  HomeFilled,
  Menu,
  Goods,
  List,
  User,
  Histogram,
  PieChart
} from '@element-plus/icons-vue'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const activeIndex = computed(() => route.path)
</script>

<style scoped>
.sidebar-wrap {
  height: 100%;
  background: var(--eb-admin-sidebar-bg);
  overflow-x: hidden;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
  padding: 0 10px 20px;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

/* ---------- Logo ---------- */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 8px;
  margin: 0 -10px 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
  white-space: nowrap;
  overflow: hidden;
}

.logo-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: var(--eb-radius-md);
  color: #fff;
  background: linear-gradient(135deg, var(--eb-admin-400), var(--eb-admin-600));
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.logo-text strong {
  font-size: var(--eb-font-md);
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.2px;
}

.logo-text span {
  font-size: 10px;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.35);
}

/* ---------- 分组标签 ---------- */
.menu-group-label {
  padding: 14px 12px 6px;
  font-size: 11px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.3);
}

/* ---------- 菜单项 ---------- */
.sidebar-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin-bottom: 4px;
  border-radius: var(--eb-radius-md);
  font-size: var(--eb-font-md);
  transition: all var(--eb-duration-fast) var(--eb-ease);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: var(--eb-admin-sidebar-hover) !important;
  color: #fff !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, var(--eb-admin-500), var(--eb-admin-400)) !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(31, 78, 121, 0.45);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--eb-duration-fast) var(--eb-ease);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
