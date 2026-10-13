<template>
  <!-- 我的订单 -->
  <div class="my-order">
    <HeaderView />

    <div class="eb-container main">
      <div class="page-head">
        <h2 class="eb-section-title">我的订单</h2>
        <span class="head-sub">共 {{ total }} 笔订单</span>
      </div>

      <div class="eb-card table-wrap">
        <el-table
          v-loading="loading"
          :data="orderList"
          style="width: 100%"
          @expand-change="handleExpand"
        >
          <!-- 展开行：订单商品明细 -->
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="detail-box">
                <div v-if="detailOf(row.id).loading" class="detail-loading">
                  <el-skeleton :rows="2" animated />
                </div>
                <el-table
                  v-else-if="detailOf(row.id).list.length"
                  :data="detailOf(row.id).list"
                  size="small"
                  border
                >
                  <el-table-column label="商品图片" width="90" align="center">
                    <template #default="{ row: d }">
                      <img :src="goodsImg(d.picture)" :alt="d.goodsname" class="detail-thumb" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="goodsname" label="商品名称" min-width="200" />
                  <el-table-column label="规格" width="150" align="center">
                    <template #default="{ row: d }">
                      <span v-if="d.spec" class="spec-text">{{ d.spec }}</span>
                      <span v-else class="spec-none">—</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="单价" width="120" align="center">
                    <template #default="{ row: d }">
                      <span class="eb-price">￥{{ money(d.price) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="shoppingnum" label="数量" width="90" align="center" />
                  <el-table-column label="小计" width="130" align="center">
                    <template #default="{ row: d }">
                      <span class="eb-price">￥{{ money(d.subtotal) }}</span>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-else description="暂无订单明细" :image-size="60" />
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="ordernumber" label="订单号" min-width="180" />
          <el-table-column label="订单金额" width="140" align="center">
            <template #default="{ row }">
              <span class="eb-price">
                <span class="eb-price-symbol">￥</span>{{ money(row.amount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="下单时间" width="180" align="center">
            <template #default="{ row }">{{ datetime(row.datetime) }}</template>
          </el-table-column>
          <el-table-column label="订单状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="orderStatusType(row.status)" size="small">
                {{ orderStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 1"
                type="danger"
                link
                @click="handleCancel(row)"
              >
                取消订单
              </el-button>
              <span v-else class="done-text">-</span>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="你还没有订单，快去下单吧">
              <el-button type="primary" @click="goShopping">去购物</el-button>
            </el-empty>
          </template>
        </el-table>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            :total="total"
            layout="total, prev, pager, next"
            background
            @current-change="loadOrders"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import HeaderView from '@/components/HeaderView.vue'
import { beforeApi } from '@/utils/api'
import { goodsImg, money, datetime, orderStatusText, orderStatusType } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const currentPage = ref(1)

// 订单明细缓存：{ [orderId]: { loading, list } }
const detailCache = reactive({})

const detailOf = (id) => detailCache[id] || { loading: false, list: [] }

/* ---- 加载订单列表 ---- */
const loadOrders = async () => {
  loading.value = true
  try {
    const res = await beforeApi.getOrderList({ currentPage: currentPage.value })
    const data = res.data || {}
    orderList.value = data.orders || []
    total.value = Number(data.total || 0)
    if (data.currentPage) currentPage.value = Number(data.currentPage)
  } catch (e) {
    // 拦截器已统一提示
    orderList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/* ---- 展开行时懒加载明细 ---- */
const handleExpand = async (row, expandedRows) => {
  const expanded = Array.isArray(expandedRows)
    ? expandedRows.some((r) => r.id === row.id)
    : !!expandedRows
  if (!expanded || detailCache[row.id]) return

  detailCache[row.id] = { loading: true, list: [] }
  try {
    const res = await beforeApi.getOrderDetail(row.id)
    detailCache[row.id] = { loading: false, list: res.data || [] }
  } catch (e) {
    // 拦截器已统一提示
    detailCache[row.id] = { loading: false, list: [] }
  }
}

/* ---- 取消订单（仅待付款） ---- */
const handleCancel = (row) => {
  ElMessageBox.confirm(`确定取消订单「${row.ordernumber}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await beforeApi.cancelOrder({ id: row.id })
        ElMessage.success('订单已取消')
        loadOrders()
      } catch (e) {
        // 拦截器已统一提示
      }
    })
    .catch(() => {})
}

const goShopping = () => router.push('/before/index')

onMounted(loadOrders)
</script>

<style scoped>
.main {
  padding: 20px 0 48px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.head-sub {
  font-size: var(--eb-font-sm);
  color: var(--eb-text-secondary);
}

.table-wrap {
  padding: 8px 16px 16px;
}

.detail-box {
  padding: 12px 24px 16px 56px;
}

.detail-loading {
  padding: 8px 0;
}

.detail-thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: var(--eb-radius-sm);
  background: var(--eb-bg-page);
}

.done-text {
  color: var(--eb-text-secondary);
  font-size: var(--eb-font-sm);
}

.spec-text {
  font-size: var(--eb-font-sm);
  color: var(--eb-text-regular);
}

.spec-none {
  color: var(--eb-text-placeholder);
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
