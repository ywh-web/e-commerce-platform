<template>
  <!-- 我的购物车 -->
  <div class="my-cart">
    <HeaderView />

    <div class="eb-container main">
      <div class="page-head">
        <h2 class="eb-section-title">我的购物车</h2>
        <span v-if="cartList.length" class="head-sub">共 {{ totalNum }} 件商品</span>
      </div>

      <div class="eb-card table-wrap">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="cartList"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />

          <el-table-column label="商品" min-width="320">
            <template #default="{ row }">
              <div class="goods-cell">
                <img
                  :src="goodsImg(row.picture)"
                  :alt="row.goodsName"
                  class="goods-thumb"
                  @click="goDetail(row.goodstableId)"
                />
                <div class="goods-meta">
                  <div class="goods-name eb-ellipsis-2" @click="goDetail(row.goodstableId)">
                    {{ row.goodsName }}
                  </div>
                  <div v-if="row.spec" class="goods-spec">规格：{{ row.spec }}</div>
                  <div class="goods-price">
                    <span class="eb-price">
                      <span class="eb-price-symbol">￥</span>{{ money(row.price) }}
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="数量" width="170" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.shoppingnum"
                :min="1"
                :max="99"
                size="small"
                @change="(val) => handleNumChange(row, val)"
              />
            </template>
          </el-table-column>

          <el-table-column label="小计" width="140" align="center">
            <template #default="{ row }">
              <span class="eb-price subtotal">
                <span class="eb-price-symbol">￥</span>{{ money(row.subtotal) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="购物车还是空的，快去挑选商品吧">
              <el-button type="primary" @click="goShopping">去逛逛</el-button>
            </el-empty>
          </template>
        </el-table>

        <!-- 结算栏 -->
        <div v-if="cartList.length" class="checkout-bar">
          <div class="left">
            <el-checkbox v-model="checkAll" @change="handleCheckAll">全选</el-checkbox>
            <span class="selected-info">
              已选 <em>{{ selectedList.length }}</em> 件 / 共 {{ totalNum }} 件
            </span>
          </div>
          <div class="right">
            <span class="total-label">合计：</span>
            <span class="eb-price total-price">
              <span class="eb-price-symbol">￥</span>{{ money(payAmount) }}
            </span>
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              :disabled="selectedList.length === 0"
              @click="handleCheckout"
            >
              结算
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import HeaderView from '@/components/HeaderView.vue'
import { beforeApi } from '@/utils/api'
import { goodsImg, money } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const tableRef = ref(null)
const cartList = ref([])
const selectedList = ref([])
const checkAll = ref(false)
// 后端返回的整单合计（件数用于展示，金额在未选择时兜底）
const totalAmount = ref(0)
const totalNum = ref(0)

// 应付金额：有勾选时按勾选计算，未勾选时回退到后端返回的整单合计
const payAmount = computed(() => {
  if (selectedList.value.length === 0) return totalAmount.value
  return selectedList.value.reduce(
    (acc, item) => acc + Number(item.price || 0) * Number(item.shoppingnum || 0),
    0
  )
})

/* ---- 加载购物车 ---- */
const loadCart = async () => {
  loading.value = true
  try {
    const res = await beforeApi.getCartList()
    const data = res.data || {}
    cartList.value = data.carts || []
    totalAmount.value = Number(data.totalAmount || 0)
    totalNum.value = Number(data.totalNum || 0)
    // 默认全选，便于直接结算
    await nextTick()
    tableRef.value && tableRef.value.toggleAllSelection()
  } catch (e) {
    // 拦截器已统一提示
    cartList.value = []
    totalAmount.value = 0
    totalNum.value = 0
  } finally {
    loading.value = false
  }
}

/* ---- 勾选变化 ---- */
const handleSelectionChange = (val) => {
  selectedList.value = val
  checkAll.value = val.length > 0 && val.length === cartList.value.length
}

const handleCheckAll = (val) => {
  tableRef.value && (val ? tableRef.value.toggleAllSelection() : tableRef.value.clearSelection())
}

/* ---- 修改数量：批量接口用数组入参 ---- */
const handleNumChange = async (row, val) => {
  try {
    await beforeApi.updateCart({
      bcid: [row.cid],
      bshoppingnum: [val]
    })
    loadCart()
  } catch (e) {
    // 拦截器已统一提示，回滚显示
    loadCart()
  }
}

/* ---- 删除单项 ---- */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定从购物车移除「${row.goodsName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await beforeApi.deleteCart(row.cid)
        ElMessage.success('已删除')
        loadCart()
      } catch (e) {
        // 拦截器已统一提示
      }
    })
    .catch(() => {})
}

/* ---- 结算：勾选商品提交订单 ---- */
const handleCheckout = async () => {
  if (selectedList.value.length === 0) return
  submitting.value = true
  try {
    const res = await beforeApi.submitOrder({
      cartIds: selectedList.value.map((i) => i.cid)
    })
    const order = res.data || {}
    ElMessage.success(`订单提交成功${order.ordernumber ? '：' + order.ordernumber : ''}`)
    router.push('/before/myOrder')
  } catch (e) {
    // 拦截器已统一提示
  } finally {
    submitting.value = false
  }
}

const goDetail = (id) => router.push(`/before/goodsDetail/${id}`)
const goShopping = () => router.push('/before/index')

onMounted(loadCart)
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

/* ---------- 商品单元格 ---------- */
.goods-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.goods-thumb {
  width: 72px;
  height: 72px;
  flex-shrink: 0;
  object-fit: cover;
  border-radius: var(--eb-radius-md);
  background: var(--eb-bg-page);
  cursor: pointer;
}

.goods-name {
  font-size: var(--eb-font-md);
  color: var(--eb-text-primary);
  line-height: 1.45;
  cursor: pointer;
  transition: color var(--eb-duration-fast) var(--eb-ease);
}

.goods-name:hover {
  color: var(--eb-brand-500);
}

.goods-price {
  margin-top: 6px;
  font-size: var(--eb-font-sm);
}

.goods-spec {
  margin-top: 4px;
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
  background: var(--eb-bg-page);
  border-radius: var(--eb-radius-sm);
  padding: 2px 8px;
  display: inline-block;
}

.subtotal {
  font-size: 16px;
}

/* ---------- 结算栏 ---------- */
.checkout-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding: 14px 20px;
  background: var(--eb-brand-50);
  border: 1px solid var(--eb-brand-100);
  border-radius: var(--eb-radius-md);
}

.checkout-bar .left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.selected-info {
  font-size: var(--eb-font-sm);
  color: var(--eb-text-regular);
}

.selected-info em {
  color: var(--eb-brand-500);
  font-style: normal;
  font-weight: 600;
}

.checkout-bar .right {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.total-label {
  color: var(--eb-text-regular);
}

.total-price {
  font-size: 24px;
  margin-right: 6px;
}
</style>
