<template>
  <!-- 后台主界面：左侧边栏 + 右侧（顶栏 + 内容区） -->
  <el-container class="admin-layout">
    <!-- 左侧：侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <SidebarCom :collapsed="collapsed" />
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <HeaderCom :collapsed="collapsed" @toggle-collapse="collapsed = !collapsed" />
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <!-- 后台主页：数据概览仪表盘 -->
        <template v-if="isDashboard">
          <div class="page-head">
            <div>
              <h1 class="page-title">数据概览</h1>
              <p class="page-sub">欢迎回来，{{ adminName }} · 这里是平台整体运营情况</p>
            </div>
            <el-button :icon="Refresh" @click="loadDashboard" :loading="loading">
              刷新数据
            </el-button>
          </div>

          <!-- 统计卡片 -->
          <div class="stat-grid">
            <div v-for="s in statCards" :key="s.key" class="stat-card eb-card">
              <div class="stat-icon" :style="{ background: s.bg, color: s.color }">
                <el-icon :size="22"><component :is="s.icon" /></el-icon>
              </div>
              <div class="stat-body">
                <div class="stat-label">{{ s.label }}</div>
                <div class="stat-value">
                  <span class="stat-num">{{ s.prefix || '' }}{{ s.value }}</span>
                  <span v-if="s.unit" class="stat-unit">{{ s.unit }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 图表 + 热销榜 -->
          <el-row :gutter="18" class="chart-row">
            <el-col :span="15">
              <div class="eb-card panel">
                <div class="panel-head">
                  <span class="panel-title">各品类销售金额</span>
                  <span class="panel-sub">单位：元</span>
                </div>
                <div ref="chartRef" class="chart-box"></div>
              </div>
            </el-col>

            <el-col :span="9">
              <div class="eb-card panel">
                <div class="panel-head">
                  <span class="panel-title">热销商品 TOP 5</span>
                </div>
                <ul class="rank-list">
                  <li v-for="(g, i) in hotGoods" :key="i" class="rank-item">
                    <span class="rank-no" :class="'rank-' + (i + 1)">{{ i + 1 }}</span>
                    <div class="rank-info">
                      <div class="rank-name eb-ellipsis">{{ g.goodsName }}</div>
                      <div class="rank-meta">
                        销量 {{ Number(g.sales || 0) }} 件
                        <span class="rank-sub">· 成交 {{ Number(g.totalNum || 0) }} 件</span>
                      </div>
                    </div>
                    <el-progress
                      :percentage="progressOf(g)"
                      :show-text="false"
                      :stroke-width="5"
                      color="#e1251b"
                      class="rank-bar"
                    />
                  </li>
                  <li v-if="!hotGoods.length" class="rank-empty">
                    <el-empty description="暂无销售数据" :image-size="70" />
                  </li>
                </ul>
              </div>
            </el-col>
          </el-row>
        </template>

        <!-- 其它子页面 -->
        <router-view v-else v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import {
  Refresh,
  Goods,
  User,
  List,
  Money,
  Bell
} from '@element-plus/icons-vue'
import SidebarCom from '@/components/SidebarCom.vue'
import HeaderCom from '@/components/HeaderCom.vue'
import { adminApi } from '@/utils/api'
import { useUserStore } from '@/stores/user'
import { moneyThousands } from '@/utils/format'

const route = useRoute()
const userStore = useUserStore()

// 侧边栏折叠状态
const collapsed = ref(false)

// 是否在后台主页（主页直接渲染仪表盘，其余走 router-view）
const isDashboard = computed(() => route.path === '/admin/home')

const adminName = computed(() => {
  const info = userStore.userInfo
  return info && info.aname ? info.aname : '管理员'
})

/* ---------- 概览数据 ---------- */
const loading = ref(false)
const overview = ref({})
const hotGoods = ref([])
const salesData = ref([])

const chartRef = ref(null)
let chart = null

// 统计卡片配置
const statCards = computed(() => [
  {
    key: 'goodsCount',
    label: '在售商品',
    value: overview.value.goodsCount || 0,
    unit: '件',
    icon: Goods,
    color: '#e1251b',
    bg: '#fff1f0'
  },
  {
    key: 'userCount',
    label: '注册用户',
    value: overview.value.userCount || 0,
    unit: '人',
    icon: User,
    color: '#1f4e79',
    bg: '#eef4fb'
  },
  {
    key: 'orderCount',
    label: '订单总数',
    value: overview.value.orderCount || 0,
    unit: '笔',
    icon: List,
    color: '#00b42a',
    bg: '#e8ffea'
  },
  {
    key: 'totalAmount',
    label: '销售总额',
    prefix: '￥',
    value: moneyThousands(overview.value.totalAmount || 0),
    icon: Money,
    color: '#ff7d00',
    bg: '#fff7e8'
  },
  {
    key: 'pendingOrderCount',
    label: '待处理订单',
    value: overview.value.pendingOrderCount || 0,
    unit: '笔',
    icon: Bell,
    color: '#8b5cf6',
    bg: '#f5f0ff'
  }
])

// 进度条百分比（相对最高销量）—— 与展示口径保持一致，统一取 sales
function progressOf(g) {
  const value = (x) => Number(x.sales || 0)
  const max = Math.max(...hotGoods.value.map(value), 1)
  return Math.round((value(g) / max) * 100)
}

/* ---------- 加载仪表盘数据 ---------- */
const loadDashboard = async () => {
  loading.value = true
  try {
    const [dashRes, salesRes, hotRes] = await Promise.allSettled([
      adminApi.getDashboard(),
      adminApi.getSalesByType(),
      adminApi.getHotGoods(5)
    ])

    if (dashRes.status === 'fulfilled') {
      overview.value = dashRes.value.data || {}
    }
    if (salesRes.status === 'fulfilled') {
      salesData.value = salesRes.value.data || []
      renderChart()
    }
    if (hotRes.status === 'fulfilled') {
      hotGoods.value = hotRes.value.data || []
    }
  } finally {
    loading.value = false
  }
}

/* ---------- 渲染品类销售柱状图 ---------- */
const renderChart = async () => {
  if (!isDashboard.value) return
  await nextTick()
  if (!chartRef.value) return

  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const names = salesData.value.map((d) => d.typeName)
  const values = salesData.value.map((d) => Number(d.totalAmount || 0))

  chart.setOption({
    grid: { left: 10, right: 20, top: 30, bottom: 10, containLabel: true },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (p) => `${p[0].name}<br/>销售额：￥${moneyThousands(p[0].value)}`
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLine: { lineStyle: { color: '#e5e6eb' } },
      axisTick: { show: false },
      axisLabel: { color: '#86909c', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f2f3f5' } },
      axisLabel: { color: '#86909c', fontSize: 12 }
    },
    series: [
      {
        type: 'bar',
        data: values,
        barMaxWidth: 42,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f96a5e' },
            { offset: 1, color: '#e1251b' }
          ])
        }
      }
    ]
  })
  chart.resize()
}

const handleResize = () => chart && chart.resize()

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})

// 从子页面切回主页时重新渲染图表
watch(
  () => route.path,
  (p) => {
    if (p === '/admin/home') {
      if (chartRef.value) renderChart()
    }
  }
)
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  transition: width 0.28s var(--eb-ease);
  overflow-x: hidden;
}

.header {
  padding: 0;
  height: 64px;
}

.main {
  background: var(--eb-bg-page);
  padding: 22px;
  overflow-y: auto;
}

/* ---------- 页头 ---------- */
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.4px;
  color: var(--eb-text-primary);
  margin-bottom: 4px;
}

.page-sub {
  font-size: var(--eb-font-sm);
  color: var(--eb-text-secondary);
}

/* ---------- 统计卡片 ---------- */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  transition: all var(--eb-duration) var(--eb-ease);
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--eb-shadow-md);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  border-radius: var(--eb-radius-md);
}

.stat-body {
  min-width: 0;
}

.stat-label {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
  margin-bottom: 4px;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 3px;
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: var(--eb-text-primary);
  font-variant-numeric: tabular-nums;
}

.stat-unit {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

/* ---------- 图表区 ---------- */
.chart-row {
  align-items: stretch;
}

.panel {
  padding: 18px 20px 20px;
  height: 100%;
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-title {
  font-size: var(--eb-font-lg);
  font-weight: 600;
  color: var(--eb-text-primary);
}

.panel-sub {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

.chart-box {
  width: 100%;
  height: 300px;
}

/* ---------- 热销榜 ---------- */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-no {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  border-radius: var(--eb-radius-sm);
  background: var(--eb-text-placeholder);
}

.rank-1 {
  background: linear-gradient(135deg, #f96a5e, #e1251b);
}

.rank-2 {
  background: linear-gradient(135deg, #ffb45e, #ff7d00);
}

.rank-3 {
  background: linear-gradient(135deg, #7ba6da, #1f4e79);
}

.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-name {
  font-size: var(--eb-font-sm);
  font-weight: 500;
  color: var(--eb-text-primary);
}

.rank-meta {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

.rank-sub {
  color: var(--eb-text-placeholder);
}

.rank-bar {
  width: 64px;
  flex-shrink: 0;
}

.rank-empty {
  padding: 10px 0;
}

/* ---------- 路由过渡 ---------- */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ---------- 响应式适配（桌面分辨率）----------
 * 后台为「侧边栏 + 内容区」结构，内容区随窗口自适应；
 * 统计卡片按可用宽度逐级降列，避免卡片被压成窄条。
 */
@media (max-width: 1440px) {
  .stat-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1280px) {
  .stat-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1024px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
