<template>
  <!-- 订单管理：查看全部订单、明细，修改状态、删除 -->
  <div class="order-manage">
    <!-- 操作栏 -->
    <div class="toolbar eb-card">
      <span class="eb-page-title">订单管理</span>
      <div class="toolbar-right">
        <el-input
          v-model="query.ordernumber"
          placeholder="搜索订单号"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select
          v-model="query.status"
          placeholder="全部状态"
          clearable
          style="width: 140px"
          @change="handleSearch"
        >
          <el-option
            v-for="s in statusOptions"
            :key="s.value"
            :label="s.label"
            :value="s.value"
          />
        </el-select>
        <el-button type="primary" color="#1f4e79" :icon="Search" @click="handleSearch">
          搜索
        </el-button>
      </div>
    </div>

    <div class="eb-card table-wrap">
      <!-- 统一使用通用组件 EbTable；@expand-change 会经 $attrs 透传给内部 el-table -->
      <EbTable
        v-model:current-page="query.currentPage"
        :data="tableData"
        :loading="loading"
        :total="total"
        empty-text="暂无订单数据"
        @page-change="loadData"
        @expand-change="handleExpand"
      >
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
                <el-table-column prop="goodsname" label="商品名称" min-width="180" />
                <el-table-column label="规格" width="140" align="center">
                  <template #default="{ row: d }">
                    <span v-if="d.spec">{{ d.spec }}</span>
                    <span v-else class="spec-none">—</span>
                  </template>
                </el-table-column>
                <el-table-column label="单价" width="110" align="center">
                  <template #default="{ row: d }">￥{{ money(d.price) }}</template>
                </el-table-column>
                <el-table-column prop="shoppingnum" label="数量" width="80" align="center" />
                <el-table-column label="小计" width="120" align="center">
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
        <el-table-column label="下单用户" min-width="200">
          <template #default="{ row }">
            <div class="buyer">
              <span class="buyer-name">{{ row.bname || '游客' }}</span>
              <span class="buyer-mail">{{ row.bemail || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="130" align="center">
          <template #default="{ row }">
            <span class="eb-price">
              <span class="eb-price-symbol">￥</span>{{ money(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="orderStatusType(row.status)" size="small">
              {{ orderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="170" align="center">
          <template #default="{ row }">{{ datetime(row.datetime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openStatus(row)">改状态</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

      </EbTable>
    </div>

    <!-- 修改状态弹窗 -->
    <el-dialog v-model="statusVisible" title="修改订单状态" width="420px">
      <el-form label-width="90px">
        <el-form-item label="订单号">
          <span class="text-regular">{{ currentOrder.ordernumber }}</span>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="statusForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option
              v-for="s in statusOptions"
              :key="s.value"
              :label="s.label"
              :value="s.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" color="#1f4e79" :loading="statusSaving" @click="handleStatusSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'
import { adminApi } from '@/utils/api'
import { goodsImg, money, datetime, orderStatusText, orderStatusType } from '@/utils/format'
import EbTable from '@/components/EbTable.vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  ordernumber: '',
  status: null,
  currentPage: 1
})

// 状态选项：1待付款 2已付款 3已发货 4已完成 5已取消
const statusOptions = [
  { label: '待付款', value: 1 },
  { label: '已付款', value: 2 },
  { label: '已发货', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 }
]

// 订单明细缓存
const detailCache = reactive({})
const detailOf = (id) => detailCache[id] || { loading: false, list: [] }

/* ---- 加载列表 ---- */
const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getOrderList({
      currentPage: query.currentPage,
      ordernumber: query.ordernumber || undefined,
      status: query.status || undefined
    })
    const data = res.data || {}
    tableData.value = data.orders || []
    total.value = Number(data.total || 0)
    if (data.currentPage) query.currentPage = Number(data.currentPage)
  } catch (e) {
    // 拦截器已统一提示
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.currentPage = 1
  loadData()
}

/* ---- 展开行懒加载明细 ---- */
const handleExpand = async (row, expandedRows) => {
  const expanded = Array.isArray(expandedRows)
    ? expandedRows.some((r) => r.id === row.id)
    : !!expandedRows
  if (!expanded || detailCache[row.id]) return

  detailCache[row.id] = { loading: true, list: [] }
  try {
    const res = await adminApi.getOrderDetail(row.id)
    detailCache[row.id] = { loading: false, list: res.data || [] }
  } catch (e) {
    // 拦截器已统一提示
    detailCache[row.id] = { loading: false, list: [] }
  }
}

/* ---- 修改状态 ---- */
const statusVisible = ref(false)
const statusSaving = ref(false)
const currentOrder = reactive({ id: null, ordernumber: '' })
const statusForm = reactive({ status: null })

const openStatus = (row) => {
  currentOrder.id = row.id
  currentOrder.ordernumber = row.ordernumber
  statusForm.status = row.status
  statusVisible.value = true
}

const handleStatusSubmit = async () => {
  if (!statusForm.status) {
    ElMessage.warning('请选择订单状态')
    return
  }
  statusSaving.value = true
  try {
    await adminApi.changeOrderStatus({ id: currentOrder.id, status: statusForm.status })
    ElMessage.success('状态修改成功')
    statusVisible.value = false
    loadData()
  } catch (e) {
    // 拦截器已统一提示
  } finally {
    statusSaving.value = false
  }
}

/* ---- 删除 ---- */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除订单「${row.ordernumber}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await adminApi.deleteOrder(row.id)
        ElMessage.success('删除成功')
        loadData()
      } catch (e) {
        // 拦截器已统一提示
      }
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  margin-bottom: 16px;
}

.toolbar .eb-page-title {
  margin-bottom: 0;
}

.toolbar-right {
  display: flex;
  gap: 10px;
}

.table-wrap {
  padding: 16px;
}

/* ---------- 响应式适配（桌面分辨率）---------- */
@media (max-width: 1280px) {
  .toolbar {
    flex-wrap: wrap;
    gap: 12px;
  }

  .toolbar-right {
    flex-wrap: wrap;
  }
}

@media (max-width: 1024px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
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

.spec-none {
  color: var(--eb-text-placeholder);
}

.buyer {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.buyer-name {
  color: var(--eb-text-primary);
}

.buyer-mail {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

.text-regular {
  color: var(--eb-text-regular);
}
</style>
