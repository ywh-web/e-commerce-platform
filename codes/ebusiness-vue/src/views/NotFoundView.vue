<template>
  <!-- 404 页面：路由未匹配时展示，替代原先"直接重定向首页"的做法 -->
  <div class="not-found">
    <HeaderView />

    <div class="eb-container main">
      <div class="eb-card panel">
        <div class="code">404</div>
        <h2 class="eb-section-title">页面走丢了</h2>
        <p class="desc">
          你访问的地址
          <span class="path">{{ badPath }}</span>
          不存在，可能是链接输错了或者页面已被移除。
        </p>
        <div class="actions">
          <el-button type="primary" size="large" @click="goHome">返回首页</el-button>
          <el-button size="large" @click="goBack">回到上一页</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HeaderView from '@/components/HeaderView.vue'

const route = useRoute()
const router = useRouter()

/** 回显用户访问的错误地址，方便排查——但截断长度，避免超长串撑破布局 */
const badPath = computed(() => {
  const p = route.fullPath || ''
  return p.length > 60 ? p.slice(0, 60) + '…' : p
})

const goHome = () => router.push('/before/index')
const goBack = () => (window.history.length > 1 ? router.back() : router.push('/before/index'))
</script>

<style scoped>
.main {
  padding: 80px 0;
}

.panel {
  max-width: 560px;
  margin: 0 auto;
  padding: 56px 40px;
  text-align: center;
}

.code {
  font-size: 96px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: 4px;
  color: var(--eb-brand-500, #e1251b);
  opacity: 0.9;
}

.eb-section-title {
  margin: 20px 0 12px;
  text-align: center;
}

.desc {
  color: var(--eb-text-secondary);
  font-size: var(--eb-font-md);
  line-height: 1.8;
  margin: 0 0 32px;
}

.path {
  display: inline-block;
  max-width: 100%;
  padding: 1px 6px;
  margin: 0 2px;
  border-radius: var(--eb-radius-sm);
  background: var(--eb-bg-page);
  color: var(--eb-text-regular);
  font-family: var(--eb-font-mono);
  font-size: var(--eb-font-sm);
  word-break: break-all;
  vertical-align: bottom;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

@media (max-width: 1024px) {
  .main {
    padding: 48px 0;
  }

  .panel {
    padding: 40px 24px;
  }

  .code {
    font-size: 72px;
  }
}
</style>
