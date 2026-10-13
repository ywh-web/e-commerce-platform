<template>
  <!-- 销售统计：各品类销售额 + 热销商品 -->
  <div class="sales-statistics">
    <!-- 汇总卡片 -->
    <div class="stat-grid">
      <div v-for="s in summaryCards" :key="s.key" class="stat-card eb-card">
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

    <!-- 图表 -->
    <div class="eb-card panel">
      <div class="panel-head">
        <span class="panel-title">各品类销售额</span>
        <div class="panel-tools">
          <span class="panel-sub">单位：元</span>
          <el-radio-group v-model="chartType" size="small">
            <el-radio-button value="bar">柱状图</el-radio-button>
            <el-radio-button value="line">折线图</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div v-show="loading" class="chart-loading">
        <el-skeleton :rows="6" animated />
      </div>
      <div v-show="!loading && salesData.length" ref="chartRef" class="chart"></div>
      <el-empty
        v-if="!loading && salesData.length === 0"
        description="暂无销售统计数据"
      />
    </div>

    <!-- 月度销量趋势 -->
    <div class="eb-card panel">
      <div class="panel-head">
        <span class="panel-title">按月销量与成交金额</span>
        <div class="panel-tools">
          <span class="panel-sub">左轴：成交金额（元）｜右轴：成交件数</span>
          <el-radio-group v-model="monthRange" size="small" @change="loadMonth">
            <el-radio-button :value="6">近 6 个月</el-radio-button>
            <el-radio-button :value="12">近 12 个月</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div v-show="monthLoading" class="chart-loading">
        <el-skeleton :rows="6" animated />
      </div>
      <div v-show="!monthLoading && monthData.length" ref="monthChartRef" class="chart"></div>
      <el-empty
        v-if="!monthLoading && monthData.length === 0"
        description="暂无月度销售数据"
      />
    </div>

    <!-- 热销商品 -->
    <div class="eb-card panel">
      <div class="panel-head">
        <span class="panel-title">热销商品 TOP 10</span>
      </div>
      <el-table v-loading="hotLoading" :data="hotGoods" border stripe style="width: 100%">
        <el-table-column type="index" label="排名" width="70" align="center" />
        <el-table-column prop="goodsName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="单价" width="120" align="center">
          <template #default="{ row }">
            <span class="eb-price">￥{{ money(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="累计销量" width="110" align="center">
          <template #default="{ row }">{{ Number(row.sales || 0) }}</template>
        </el-table-column>
        <el-table-column label="成交件数" width="110" align="center">
          <template #default="{ row }">{{ Number(row.totalNum || 0) }}</template>
        </el-table-column>
        <el-table-column label="销售额" width="150" align="center">
          <template #default="{ row }">
            <span class="eb-price">￥{{ moneyThousands(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无热销商品数据" />
        </template>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { Money, ShoppingCart, PriceTag } from '@element-plus/icons-vue'
import { adminApi } from '@/utils/api'
import { money, moneyThousands } from '@/utils/format'

const loading = ref(false)
const hotLoading = ref(false)
const chartType = ref('bar')
const chartRef = ref(null)
const salesData = ref([])
const hotGoods = ref([])

// 月度统计
const monthLoading = ref(false)
const monthRange = ref(6)
const monthChartRef = ref(null)
const monthData = ref([])

let chart = null
let monthChart = null

// 汇总数据
const totalAmount = computed(() =>
  salesData.value.reduce((sum, i) => sum + Number(i.totalAmount || 0), 0)
)
const totalNum = computed(() =>
  salesData.value.reduce((sum, i) => sum + Number(i.totalNum || 0), 0)
)

const summaryCards = computed(() => [
  {
    key: 'amount',
    label: '销售总额',
    prefix: '￥',
    value: moneyThousands(totalAmount.value),
    icon: Money,
    color: '#1f4e79',
    bg: '#eef4fb'
  },
  {
    key: 'num',
    label: '总成交件数',
    value: totalNum.value,
    unit: '件',
    icon: ShoppingCart,
    color: '#e1251b',
    bg: '#fff1f0'
  },
  {
    key: 'type',
    label: '品类数',
    value: salesData.value.length,
    unit: '类',
    icon: PriceTag,
    color: '#00b42a',
    bg: '#e8ffea'
  }
])

/* ---- 渲染图表 ---- */
const renderChart = async () => {
  if (!salesData.value.length) return
  await nextTick()
  if (!chartRef.value) return

  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const names = salesData.value.map((d) => d.typeName)
  const values = salesData.value.map((d) => Number(d.totalAmount || 0))

  chart.setOption(
    {
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
          type: chartType.value,
          data: values,
          smooth: true,
          barMaxWidth: 42,
          symbolSize: 7,
          lineStyle: { width: 3 },
          itemStyle: {
            borderRadius: chartType.value === 'bar' ? [6, 6, 0, 0] : 0,
            color: '#1f4e79'
          },
          areaStyle:
            chartType.value === 'line'
              ? {
                  color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(31,78,121,0.25)' },
                    { offset: 1, color: 'rgba(31,78,121,0.02)' }
                  ])
                }
              : undefined
        }
      ]
    },
    true
  )
  chart.resize()
}

/* ---- 加载数据 ---- */
const loadSales = async () => {
  loading.value = true
  try {
    const res = await adminApi.getSalesByType()
    salesData.value = res.data || []
  } catch (e) {
    // 拦截器已统一提示
    salesData.value = []
  } finally {
    loading.value = false
  }
  // loading 关闭后再渲染，确保图表容器已可见（否则宽高为 0）
  renderChart()
}

const loadHotGoods = async () => {
  hotLoading.value = true
  try {
    const res = await adminApi.getHotGoods(10)
    hotGoods.value = res.data || []
  } catch (e) {
    // 拦截器已统一提示
    hotGoods.value = []
  } finally {
    hotLoading.value = false
  }
}

/* ---- 渲染月度图表：柱状图看金额，折线看件数（双 Y 轴） ---- */
const renderMonthChart = async () => {
  if (!monthData.value.length) return
  await nextTick()
  if (!monthChartRef.value) return

  if (!monthChart) {
    monthChart = echarts.init(monthChartRef.value)
  }

  const months = monthData.value.map((d) => d.month)
  const amounts = monthData.value.map((d) => Number(d.totalAmount || 0))
  const nums = monthData.value.map((d) => Number(d.totalNum || 0))

  monthChart.setOption(
    {
      grid: { left: 10, right: 20, top: 44, bottom: 10, containLabel: true },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (p) => {
          const amount = p.find((x) => x.seriesName === '成交金额')
          const num = p.find((x) => x.seriesName === '成交件数')
          return [
            p[0].axisValue,
            `成交金额：￥${moneyThousands(amount ? amount.value : 0)}`,
            `成交件数：${num ? num.value : 0} 件`
          ].join('<br/>')
        }
      },
      legend: {
        top: 0,
        right: 0,
        itemWidth: 12,
        itemHeight: 8,
        textStyle: { color: '#86909c', fontSize: 12 }
      },
      xAxis: {
        type: 'category',
        data: months,
        axisLine: { lineStyle: { color: '#e5e6eb' } },
        axisTick: { show: false },
        axisLabel: { color: '#86909c', fontSize: 12 }
      },
      yAxis: [
        {
          type: 'value',
          name: '金额',
          splitLine: { lineStyle: { color: '#f2f3f5' } },
          axisLabel: { color: '#86909c', fontSize: 12 }
        },
        {
          type: 'value',
          name: '件数',
          // 件数是整数，坐标轴只允许出现整数刻度
          minInterval: 1,
          splitLine: { show: false },
          axisLabel: { color: '#86909c', fontSize: 12 }
        }
      ],
      series: [
        {
          name: '成交金额',
          type: 'bar',
          barMaxWidth: 42,
          data: amounts,
          itemStyle: { borderRadius: [6, 6, 0, 0], color: '#1f4e79' }
        },
        {
          name: '成交件数',
          type: 'line',
          yAxisIndex: 1,
          smooth: false,
          symbolSize: 7,
          data: nums,
          itemStyle: { color: '#e1251b' },
          lineStyle: { width: 3 }
        }
      ]
    },
    true
  )
  monthChart.resize()
}

/* ---- 加载月度数据 ---- */
const loadMonth = async () => {
  monthLoading.value = true
  try {
    const res = await adminApi.getSalesByMonth(monthRange.value)
    monthData.value = res.data || []
  } catch (e) {
    // 拦截器已统一提示
    monthData.value = []
  } finally {
    monthLoading.value = false
  }
  // loading 关闭后再渲染，确保图表容器已可见（否则宽高为 0）
  renderMonthChart()
}

watch(chartType, () => renderChart())

const handleResize = () => {
  chart && chart.resize()
  monthChart && monthChart.resize()
}

onMounted(() => {
  loadSales()
  loadMonth()
  loadHotGoods()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
  if (monthChart) {
    monthChart.dispose()
    monthChart = null
  }
})
</script>

<style scoped>
/* ---------- 汇总卡片 ---------- */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
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

/* ---------- 面板 ---------- */
.panel {
  padding: 18px 20px 20px;
  margin-bottom: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-title {
  font-size: var(--eb-font-lg);
  font-weight: 600;
  color: var(--eb-text-primary);
}

.panel-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-sub {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

.chart {
  width: 100%;
  height: 340px;
}

.chart-loading {
  padding: 40px;
}

/* ---------- 响应式适配（桌面分辨率）---------- */
@media (max-width: 1024px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart {
    height: 300px;
  }
}
</style>
