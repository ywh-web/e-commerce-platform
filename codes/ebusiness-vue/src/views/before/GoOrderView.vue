<template>
  <!-- 订单确认页：确认收货信息与商品后提交订单 -->
  <div class="go-order">
    <HeaderView />

    <div class="eb-container main">
      <h2 class="eb-section-title">确认订单</h2>

      <!-- 收货信息 -->
      <div class="eb-card block">
        <div class="block-head">
          <h3 class="block-title">收货信息</h3>
          <el-button type="primary" link @click="goEditInfo">
            修改收货信息
          </el-button>
        </div>

        <div v-loading="infoLoading" class="receiver">
          <template v-if="hasReceiver">
            <div class="receiver-row">
              <span class="rc-label">收货人</span>
              <span class="rc-value">{{ userInfo.bname || '未填写' }}</span>
            </div>
            <div class="receiver-row">
              <span class="rc-label">联系电话</span>
              <span class="rc-value">{{ userInfo.bphone || '未填写' }}</span>
            </div>
            <div class="receiver-row">
              <span class="rc-label">收货地址</span>
              <span class="rc-value">{{ userInfo.baddr || '未填写' }}</span>
            </div>
          </template>
          <el-empty v-else description="请先完善收货信息（收货人 / 电话 / 地址）" :image-size="80">
            <el-button type="primary" @click="goEditInfo">去完善</el-button>
          </el-empty>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="eb-card block">
        <h3 class="block-title">商品清单</h3>
        <el-table v-loading="loading" :data="goodsList" border>
          <el-table-column label="商品图片" width="100" align="center">
            <template #default="{ row }">
              <img :src="goodsImg(row.picture)" :alt="row.name" class="goods-thumb" />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
          <el-table-column label="规格" width="160" align="center">
            <template #default="{ row }">
              <span v-if="row.spec" class="spec-text">{{ row.spec }}</span>
              <span v-else class="spec-none">—</span>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="center">
            <template #default="{ row }">
              <span class="eb-price">￥{{ money(row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="shoppingnum" label="数量" width="100" align="center" />
          <el-table-column label="小计" width="130" align="center">
            <template #default="{ row }">
              <span class="eb-price">￥{{ money(row.subtotal) }}</span>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="没有待结算的商品" />
          </template>
        </el-table>
      </div>

      <!-- 提交栏 -->
      <div class="eb-card submit-bar">
        <div class="left">
          <span>应付金额：</span>
          <span class="eb-price total">
            <span class="eb-price-symbol">￥</span>{{ money(totalPrice) }}
          </span>
        </div>
        <div class="right">
          <el-button size="large" @click="goBack">返回</el-button>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            :disabled="goodsList.length === 0 || submitted"
            @click="handleSubmit"
          >
            提交订单
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import HeaderView from '@/components/HeaderView.vue'
import { beforeApi } from '@/utils/api'
import { goodsImg, money } from '@/utils/format'
import { isPhone } from '@/utils/validators'

const route = useRoute()
const router = useRouter()

const goodsId = route.params.id
const buyNum = Number(route.query.num) || 1
/** 详情页选好的规格，多维度以 " / " 连接（Vue Router 已自动解码） */
const buySpec = route.query.spec || ''

const loading = ref(false)
const infoLoading = ref(false)
const submitting = ref(false)
const goodsList = ref([])

const userInfo = reactive({
  bname: '',
  bphone: '',
  baddr: ''
})

const hasReceiver = computed(
  () => !!(userInfo.bname && userInfo.bphone && userInfo.baddr)
)

const totalPrice = computed(() =>
  goodsList.value.reduce(
    (sum, item) => sum + Number(item.price || 0) * Number(item.shoppingnum || 0),
    0
  )
)

/* ---- 加载待结算商品 ---- */
const loadGoods = async () => {
  loading.value = true
  try {
    const res = await beforeApi.getGoodsDetail(goodsId)
    const goods = res.data || null
    if (goods) {
      goodsList.value = [
        {
          ...goods,
          shoppingnum: buyNum,
          spec: buySpec,
          subtotal: Number(goods.price || 0) * buyNum
        }
      ]
    } else {
      goodsList.value = []
    }
  } catch (e) {
    // 拦截器已统一提示
    goodsList.value = []
  } finally {
    loading.value = false
  }
}

/* ---- 加载收货信息 ---- */
const loadUserInfo = async () => {
  infoLoading.value = true
  try {
    const res = await beforeApi.getUserInfo()
    const data = res.data || {}
    Object.assign(userInfo, {
      bname: data.bname || '',
      bphone: data.bphone || '',
      baddr: data.baddr || ''
    })
  } catch (e) {
    // 拦截器已统一提示
  } finally {
    infoLoading.value = false
  }
}

/* ---- 提交订单 ---- */
// 一次成功即锁定：防止"提交成功到路由跳转之间"的重复点击造成重复下单
// （submitting 只保证不并发，请求回包后会复位，若不额外锁定仍可能被连点钻空子）
const submitted = ref(false)
const handleSubmit = async () => {
  if (submitted.value) return
  if (submitting.value) return
  if (!hasReceiver.value) {
    ElMessage.warning('请先完善收货信息（收货人 / 电话 / 地址）')
    return
  }
  // 收货电话格式兜底：历史数据或早前录入的号码可能不合规，提交前再拦一道
  if (!isPhone(userInfo.bphone)) {
    ElMessage.warning('收货电话格式不正确，请到个人信息页修改为 11 位手机号')
    return
  }
  if (goodsList.value.length === 0) return

  submitting.value = true
  try {
    // 直接购买的商品可能不在购物车，先加入购物车拿到 cid 再提交订单
    await beforeApi.addCart({
      goodstableId: Number(goodsId),
      shoppingnum: buyNum,
      spec: buySpec
    })

    const cartRes = await beforeApi.getCartList()
    const carts = (cartRes.data && cartRes.data.carts) || []
    // 同一商品可能因规格不同存在多条，必须"商品 + 规格"双条件匹配刚加入的那条
    const matched = [...carts]
      .reverse()
      .find(
        (c) =>
          Number(c.goodstableId) === Number(goodsId) &&
          (c.spec || '') === buySpec
      )

    if (!matched) {
      ElMessage.error('下单失败，请稍后重试')
      return
    }

    const res = await beforeApi.submitOrder({ cartIds: [matched.cid] })
    const order = res.data || {}
    submitted.value = true // 下单已成功，锁死入口，后续点击一律忽略
    ElMessage.success(`订单提交成功${order.ordernumber ? '：' + order.ordernumber : ''}`)
    router.push('/before/myOrder')
  } catch (e) {
    // 拦截器已统一提示；失败不锁定，允许用户重试
  } finally {
    submitting.value = false
  }
}

const goEditInfo = () => router.push('/before/myselfInfo')
const goBack = () => router.back()

onMounted(() => {
  loadGoods()
  loadUserInfo()
})
</script>

<style scoped>
.main {
  padding: 20px 0 48px;
}

.block {
  padding: 20px;
  margin-top: 16px;
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.block-title {
  font-size: var(--eb-font-lg);
  font-weight: 600;
  color: var(--eb-text-primary);
}

/* ---------- 收货信息 ---------- */
.receiver {
  min-height: 80px;
}

.receiver-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  font-size: var(--eb-font-md);
}

.rc-label {
  width: 90px;
  flex-shrink: 0;
  color: var(--eb-text-secondary);
}

.rc-value {
  color: var(--eb-text-regular);
}

/* ---------- 商品 ---------- */
.goods-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: var(--eb-radius-sm);
  background: var(--eb-bg-page);
}

.spec-text {
  font-size: var(--eb-font-sm);
  color: var(--eb-text-regular);
}

.spec-none {
  color: var(--eb-text-placeholder);
}

/* ---------- 提交栏 ---------- */
.submit-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  margin-top: 16px;
  background: var(--eb-brand-50);
  border: 1px solid var(--eb-brand-100);
}

.submit-bar .left {
  display: flex;
  align-items: baseline;
  gap: 8px;
  color: var(--eb-text-regular);
}

.submit-bar .total {
  font-size: 26px;
}

.submit-bar .right {
  display: flex;
  gap: 12px;
}
</style>
