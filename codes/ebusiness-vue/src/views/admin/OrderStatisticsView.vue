<template>
  <!-- 订单统计：订单状态分布 + 近 7 天订单趋势 -->
  <div class="order-statistics">
    <el-row :gutter="16">
      <!-- 左：状态分布环形图 -->
      <el-col :span="12">
        <div class="eb-card panel">
          <div class="panel-head">
            <span class="panel-title">订单状态分布</span>
          </div>

          <div class="chart-box">
            <div v-show="loading" class="chart-loading">
              <el-skeleton :rows="5" animated />
            </div>
            <div v-show="!loading && statusData.length" ref="pieRef" class="chart"></div>
            <div v-if="!loading && statusData.length" class="pie-center">
              <div class="pie-num">{{ totalOrders }}</div>
              <div class="pie-label">订单总数</div>
            </div>
            <el-empty
              v-if="!loading && statusData.length === 0"
              description="暂无订单状态数据"
            />
          </div>
        </div>
      </el-col>

      <!-- 右：状态明细表 -->
      <el-col :span="12">
        <div class="eb-card panel">
          <div class="panel-head">
            <span class="panel-title">状态明细</span>
          </div>
          <el-table v-loading="loading" :data="statusData" border stripe style="width: 100%">
            <el-table-column label="订单状态" min-width="120">
              <template #default="{ row }">
                <el-tag :type="orderStatusType(row.status)" size="small">
                  {{ row.statusName || orderStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="orderCount" label="订单数" width="100" align="center" />
            <el-table-column label="占比" width="100" align="center">
              <template #default="{ row }">{{ percent(row.orderCount) }}</template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无数据" :image-size="60" />
            </template>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <!-- 近 7 天订单趋势 -->
    <div class="eb-card panel">
      <div class="panel-head">
        <span class="panel-title">近 7 天订单趋势</span>
        <span class="panel-sub">左轴：订单数，右轴：成交金额（元）</span>
      </div>

      <div v-show="trendLoading" class="chart-loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-show="!trendLoading && trendData.length" ref="lineRef" class="chart chart-lg"></div>
      <el-empty
        v-if="!trendLoading && trendData.length === 0"
        description="暂无订单趋势数据"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '@/utils/api'
import { moneyThousands, orderStatusText, orderStatusType } from '@/utils/format'

const loading = ref(false)
const trendLoading = ref(false)
const statusData = ref([])
const trendData = ref([])

const pieRef = ref(null)
const lineRef = ref(null)
let pieChart = null
let lineChart = null

// 订单总数（环形图中心）
const totalOrders = computed(() =>
  statusData.value.reduce((sum, i) => sum + Number(i.orderCount || 0), 0)
)

// 占比
const percent = (value) => {
  if (!totalOrders.value) return '0%'
  return ((Number(value || 0) / totalOrders.value) * 100).toFixed(1) + '%'
}

/* ---- 渲染状态分布环形图 ---- */
const renderPie = async () => {
  if (!statusData.value.length) return
  await nextTick()
  if (!pieRef.value) return

  if (!pieChart) pieChart = echarts.init(pieRef.value)

  pieChart.setOption(
    {
      tooltip: { trigger: 'item', formatter: '{b}：{c} 单 ({d}%)' },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
        textStyle: { color: '#4e5969' }
      },
      color: ['#ff7d00', '#1f4e79', '#4a80c4', '#00b42a', '#86909c'],
      series: [
        {
          name: '订单状态',
          type: 'pie',
          radius: ['46%', '68%'],
          center: ['38%', '50%'],
          avoidLabelOverlap: true,
          itemStyle: { borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          emphasis: { scale: true, scaleSize: 6 },
          data: statusData.value.map((i) => ({
            name: i.statusName || orderStatusText(i.status),
            value: Number(i.orderCount || 0)
          }))
        }
      ]
    },
    true
  )
  pieChart.resize()
}

/* ---- 渲染订单趋势折线图 ---- */
const renderLine = async () => {
  if (!trendData.value.length) return
  await nextTick()
  if (!lineRef.value) return

  if (!lineChart) lineChart = echarts.init(lineRef.value)

  const dates = trendData.value.map((i) => i.date)
  const counts = trendData.value.map((i) => Number(i.orderCount || 0))
  const amounts = trendData.value.map((i) => Number(i.amount || 0))

  lineChart.setOption(
    {
      grid: { left: 10, right: 20, top: 40, bottom: 10, containLabel: true },
      tooltip: {
        trigger: 'axis',
        formatter: (p) => {
          const d = p[0].axisValue
          const count = p.find((x) => x.seriesName === '订单数')
          const amount = p.find((x) => x.seriesName === '成交金额')
          return `${d}<br/>订单数：${count ? count.value : 0} 单<br/>成交金额：￥${
            amount ? moneyThousands(amount.value) : '0.00'
          }`
        }
      },
      legend: { data: ['订单数', '成交金额'], right: 10, top: 6 },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLine: { lineStyle: { color: '#e5e6eb' } },
        axisTick: { show: false },
        axisLabel: { color: '#86909c', fontSize: 12 }
      },
      yAxis: [
        {
          type: 'value',
          name: '订单数',
          // 订单数是整数，坐标轴只允许出现整数刻度
          minInterval: 1,
          splitLine: { lineStyle: { color: '#f2f3f5' } },
          axisLabel: { color: '#86909c', fontSize: 12 }
        },
        {
          type: 'value',
          name: '金额',
          splitLine: { show: false },
          axisLabel: { color: '#86909c', fontSize: 12 }
        }
      ],
      series: [
        {
          name: '订单数',
          type: 'line',
          // 订单数是离散的整数计数，不能用平滑插值，
          // 否则曲线上会冒出"2.5 笔"这类实际不存在的值
          smooth: false,
          symbolSize: 7,
          data: counts,
          itemStyle: { color: '#1f4e79' },
          lineStyle: { width: 3 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(31,78,121,0.22)' },
              { offset: 1, color: 'rgba(31,78,121,0.02)' }
            ])
          }
        },
        {
          name: '成交金额',
          type: 'line',
          yAxisIndex: 1,
          // 同理，折线直接连点，避免平滑曲线超出真实最高点造成误读
          smooth: false,
          symbolSize: 7,
          data: amounts,
          itemStyle: { color: '#ff7d00' },
          lineStyle: { width: 3, type: 'dashed' }
        }
      ]
    },
    true
  )
  lineChart.resize()
}

/* ---- 加载数据 ---- */
const loadStatus = async () => {
  loading.value = true
  try {
    const res = await adminApi.getOrderStatusStat()
    statusData.value = res.data || []
  } catch (e) {
    // 拦截器已统一提示
    statusData.value = []
  } finally {
    loading.value = false
  }
  // loading 关闭后再渲染，确保图表容器已可见
  renderPie()
}

const loadTrend = async () => {
  trendLoading.value = true
  try {
    const res = await adminApi.getOrderTrend(7)
    trendData.value = res.data || []
  } catch (e) {
    // 拦截器已统一提示
    trendData.value = []
  } finally {
    trendLoading.value = false
  }
  renderLine()
}

const handleResize = () => {
  pieChart && pieChart.resize()
  lineChart && lineChart.resize()
}

onMounted(() => {
  loadStatus()
  loadTrend()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
  if (lineChart) {
    lineChart.dispose()
    lineChart = null
  }
})
</script>

<style scoped>
.panel {
  padding: 18px 20px 20px;
  margin-bottom: 16px;
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
  position: relative;
}

.chart {
  width: 100%;
  height: 320px;
}

.chart-lg {
  height: 340px;
}

.chart-loading {
  padding: 40px;
}

/* ---------- 环形图中心文案 ---------- */
.pie-center {
  position: absolute;
  left: 38%;
  top: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
}

.pie-num {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: var(--eb-text-primary);
  font-variant-numeric: tabular-nums;
}

.pie-label {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}
</style>
