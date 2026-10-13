<template>
  <!-- 商品详情页 -->
  <div class="goods-detail">
    <HeaderView />

    <div class="eb-container main">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="crumb">
        <el-breadcrumb-item :to="{ path: '/before/index' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="goods && goods.typeName">{{ goods.typeName }}</el-breadcrumb-item>
        <el-breadcrumb-item>商品详情</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 加载中 -->
      <div v-if="loading" class="eb-card skeleton-box">
        <el-row :gutter="30">
          <el-col :span="10">
            <el-skeleton-item variant="image" style="width: 100%; height: 400px" />
          </el-col>
          <el-col :span="14">
            <el-skeleton :rows="8" animated />
          </el-col>
        </el-row>
      </div>

      <!-- 商品内容 -->
      <div v-else-if="goods" class="eb-card detail-card">
        <div class="detail-grid">
          <!-- 左：图片 -->
          <div class="gallery">
            <div class="main-img-box">
              <img :src="goodsImg(goods.picture)" :alt="goods.name" class="main-img" />
              <span v-if="goods.stock !== null && goods.stock <= 20" class="stock-flag">
                仅剩 {{ goods.stock }} 件
              </span>
            </div>
          </div>

          <!-- 右：信息 -->
          <div class="info">
            <h1 class="gname">{{ goods.name }}</h1>

            <!-- 价格区 -->
            <div class="price-panel">
              <div class="price-main">
                <span class="price-label">促销价</span>
                <span class="eb-price price">
                  <span class="eb-price-symbol">￥</span>{{ money(goods.price) }}
                </span>
              </div>
              <div class="price-stats">
                <span>已售 {{ goods.sales || 0 }} 件</span>
                <span class="divider">|</span>
                <span>累计评价 {{ goods.sales || 0 }}</span>
              </div>
            </div>

            <!-- 属性 -->
            <dl class="attr-list">
              <div class="attr-row">
                <dt>商品编号</dt>
                <dd>GD{{ String(goods.id).padStart(6, '0') }}</dd>
              </div>
              <div class="attr-row">
                <dt>所属分类</dt>
                <dd>
                  <span class="cat-tag">{{ goods.typeName || '未分类' }}</span>
                </dd>
              </div>
              <div class="attr-row">
                <dt>库存状态</dt>
                <dd>
                  <span v-if="goods.stock > 0" class="stock-ok">
                    现货 {{ goods.stock }} 件
                  </span>
                  <span v-else class="stock-none">暂时缺货</span>
                </dd>
              </div>
              <div class="attr-row">
                <dt>配送方式</dt>
                <dd>顺丰包邮 · 7 天无理由退换</dd>
              </div>
            </dl>

            <!-- 规格选择 -->
            <div v-if="specGroups.length" class="spec-panel">
              <div v-for="group in specGroups" :key="group.name" class="spec-row">
                <span class="spec-label">{{ group.name }}</span>
                <div class="spec-options">
                  <button
                    v-for="opt in group.options"
                    :key="opt"
                    type="button"
                    class="spec-option"
                    :class="{ 'is-active': selectedSpec[group.name] === opt }"
                    @click="selectedSpec[group.name] = opt"
                  >
                    {{ opt }}
                  </button>
                </div>
              </div>
              <div class="spec-result">
                已选规格：<em>{{ currentSpecText }}</em>
              </div>
            </div>

            <!-- 购买数量 -->
            <div class="buy-row">
              <span class="buy-label">购买数量</span>
              <el-input-number
                v-model="buyNum"
                :min="1"
                :max="goods.stock > 0 ? goods.stock : 1"
                :disabled="goods.stock <= 0"
              />
              <span class="buy-hint">（最多可购 {{ goods.stock }} 件）</span>
            </div>

            <!-- 操作按钮 -->
            <div class="actions">
              <el-button
                type="primary"
                size="large"
                :disabled="goods.stock <= 0"
                @click="handleAddCart"
              >
                <el-icon><ShoppingCart /></el-icon>&nbsp;加入购物车
              </el-button>
              <el-button
                size="large"
                class="btn-focus"
                :class="{ 'is-focus': isFocused }"
                @click="handleToggleFocus"
              >
                <el-icon><StarFilled v-if="isFocused" /><Star v-else /></el-icon>&nbsp;
                {{ isFocused ? '已收藏' : '收藏商品' }}
              </el-button>
              <el-button
                type="danger"
                size="large"
                :disabled="goods.stock <= 0"
                @click="handleBuyNow"
              >
                立即购买
              </el-button>
            </div>

            <!-- 服务承诺 -->
            <ul class="promise">
              <li><el-icon><CircleCheck /></el-icon> 正品保障</li>
              <li><el-icon><Van /></el-icon> 极速发货</li>
              <li><el-icon><RefreshLeft /></el-icon> 七天退换</li>
              <li><el-icon><Service /></el-icon> 售后无忧</li>
            </ul>
          </div>
        </div>

        <!-- 商品描述 -->
        <div class="desc-section">
          <div class="desc-head">
            <span class="desc-title">商品详情</span>
          </div>
          <p class="desc-text">{{ goods.introduce || '商家暂未填写商品描述。' }}</p>
        </div>
      </div>

      <!-- 商品不存在 -->
      <div v-else class="eb-card">
        <el-empty description="商品不存在或已下架">
          <el-button type="primary" @click="$router.push('/before/index')">返回首页</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ShoppingCart,
  Star,
  StarFilled,
  CircleCheck,
  Van,
  RefreshLeft,
  Service
} from '@element-plus/icons-vue'
import HeaderView from '@/components/HeaderView.vue'
import { beforeApi } from '@/utils/api'
import { useUserStore } from '@/stores/user'
import { goodsImg, money } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const goods = ref(null)
const buyNum = ref(1)
const isFocused = ref(false)

/** 各规格维度的选中值，形如 { 颜色: '幻夜黑', 版本: '12+256G' } */
const selectedSpec = reactive({})

const goodsId = route.params.id

/* ---- 解析规格定义 ----
 * 后端格式：颜色:幻夜黑|星河银;版本:12+256G|12+512G
 * 无规格时 specs 为空串，specGroups 即为空数组，页面不渲染规格区块
 */
const specGroups = computed(() => {
  const raw = goods.value && goods.value.specs
  if (!raw) return []
  return raw
    .split(';')
    .map((seg) => seg.trim())
    .filter(Boolean)
    .map((seg) => {
      const idx = seg.indexOf(':')
      if (idx === -1) return null
      const name = seg.slice(0, idx).trim()
      const options = seg
        .slice(idx + 1)
        .split('|')
        .map((o) => o.trim())
        .filter(Boolean)
      if (!name || !options.length) return null
      return { name, options }
    })
    .filter(Boolean)
})

/** 用户当前选中的规格文本，多维度用 " / " 连接；未选完则为空 */
const currentSpecText = computed(() =>
  specGroups.value
    .map((g) => selectedSpec[g.name])
    .filter(Boolean)
    .join(' / ')
)

/** 逐个维度重置为第一项，保证默认就有值 */
const resetSpecSelection = () => {
  Object.keys(selectedSpec).forEach((k) => delete selectedSpec[k])
  specGroups.value.forEach((g) => {
    selectedSpec[g.name] = g.options[0]
  })
}

/* ---- 加载商品详情 ---- */
const loadDetail = async () => {
  loading.value = true
  try {
    const res = await beforeApi.getGoodsDetail(goodsId)
    goods.value = res.data || null
    if (goods.value && goods.value.stock > 0) {
      buyNum.value = 1
    }
    resetSpecSelection()
  } catch (e) {
    goods.value = null
  } finally {
    loading.value = false
  }
}

/* ---- 加载收藏状态 ---- */
const loadFocusState = async () => {
  if (!userStore.isLogin) return
  try {
    const res = await beforeApi.checkFocus(goodsId)
    isFocused.value = !!(res.data && res.data.focused)
  } catch (e) {
    isFocused.value = false
  }
}

/* ---- 登录校验 ---- */
const checkLogin = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

/* ---- 校验规格是否已选全 ---- */
const checkSpec = () => {
  if (specGroups.value.length && !currentSpecText.value) {
    ElMessage.warning('请先选择商品规格')
    return false
  }
  return true
}

/* ---- 加入购物车 ---- */
const handleAddCart = async () => {
  if (!checkLogin()) return
  if (!checkSpec()) return
  try {
    await beforeApi.addCart({
      goodstableId: Number(goodsId),
      shoppingnum: buyNum.value,
      spec: currentSpecText.value
    })
    ElMessage.success('已加入购物车')
  } catch (e) {
    // 拦截器已统一提示
  }
}

/* ---- 收藏 / 取消收藏 ---- */
const handleToggleFocus = async () => {
  if (!checkLogin()) return
  try {
    const res = await beforeApi.toggleFocus({ goodstableId: Number(goodsId) })
    isFocused.value = !!(res.data && res.data.focused)
    ElMessage.success(isFocused.value ? '收藏成功' : '已取消收藏')
  } catch (e) {
    // 拦截器已统一提示
  }
}

/* ---- 立即购买 ---- */
const handleBuyNow = () => {
  if (!checkLogin()) return
  if (!checkSpec()) return
  router.push({
    path: `/before/goOrder/${goodsId}`,
    query: { num: buyNum.value, spec: currentSpecText.value }
  })
}

onMounted(() => {
  loadDetail()
  loadFocusState()
})
</script>

<style scoped>
.main {
  padding: 20px 0 48px;
}

.crumb {
  margin-bottom: 16px;
  font-size: var(--eb-font-sm);
}

.skeleton-box {
  padding: 28px;
}

.detail-card {
  padding: 28px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 36px;
}

/* ---------- 左侧图片 ---------- */
.main-img-box {
  position: relative;
  width: 100%;
  height: 400px;
  border-radius: var(--eb-radius-lg);
  overflow: hidden;
  background: var(--eb-bg-page);
  border: 1px solid var(--eb-border-color-light);
}

.main-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.stock-flag {
  position: absolute;
  left: 14px;
  top: 14px;
  padding: 4px 12px;
  font-size: var(--eb-font-xs);
  color: #fff;
  background: linear-gradient(135deg, #ff9a2e, var(--eb-warning));
  border-radius: var(--eb-radius-sm);
  box-shadow: 0 2px 8px rgba(255, 125, 0, 0.35);
}

/* ---------- 右侧信息 ---------- */
.gname {
  font-size: 24px;
  line-height: 1.4;
  font-weight: 700;
  letter-spacing: -0.3px;
  color: var(--eb-text-primary);
  margin-bottom: 18px;
}

.price-panel {
  padding: 18px 20px;
  border-radius: var(--eb-radius-lg);
  background: linear-gradient(135deg, #fff5f4, #fff9f8);
  border: 1px solid var(--eb-brand-100);
  margin-bottom: 22px;
}

.price-main {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price-label {
  font-size: var(--eb-font-sm);
  color: var(--eb-text-secondary);
}

.price-main .eb-price {
  font-size: 34px;
  line-height: 1;
}

.price-stats {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

.price-stats .divider {
  color: var(--eb-border-color-dark);
}

/* ---------- 属性表 ---------- */
.attr-list {
  margin-bottom: 20px;
}

.attr-row {
  display: flex;
  align-items: center;
  padding: 9px 0;
  font-size: var(--eb-font-md);
}

.attr-row dt {
  width: 88px;
  flex-shrink: 0;
  color: var(--eb-text-secondary);
}

.attr-row dd {
  color: var(--eb-text-regular);
}

.cat-tag {
  display: inline-block;
  padding: 2px 10px;
  font-size: var(--eb-font-xs);
  color: var(--eb-brand-600);
  background: var(--eb-brand-50);
  border: 1px solid var(--eb-brand-100);
  border-radius: var(--eb-radius-full);
}

.stock-ok {
  color: var(--eb-success);
  font-weight: 500;
}

.stock-none {
  color: var(--eb-danger);
  font-weight: 500;
}

/* ---------- 规格选择 ---------- */
.spec-panel {
  padding: 16px 0 4px;
  border-top: 1px dashed var(--eb-border-color);
}

.spec-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.spec-label {
  width: 88px;
  flex-shrink: 0;
  padding-top: 7px;
  font-size: var(--eb-font-md);
  color: var(--eb-text-secondary);
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-option {
  min-width: 64px;
  padding: 6px 16px;
  font-size: var(--eb-font-sm);
  color: var(--eb-text-regular);
  background: #fff;
  border: 1px solid var(--eb-border-color);
  border-radius: var(--eb-radius-sm);
  cursor: pointer;
  transition: all var(--eb-ease);
}

.spec-option:hover {
  color: var(--eb-brand-500);
  border-color: var(--eb-brand-300);
}

.spec-option.is-active {
  color: var(--eb-brand-600);
  background: var(--eb-brand-50);
  border-color: var(--eb-brand-500);
  font-weight: 600;
  box-shadow: 0 0 0 1px var(--eb-brand-500) inset;
}

.spec-result {
  margin: 2px 0 12px 100px;
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

.spec-result em {
  font-style: normal;
  color: var(--eb-brand-600);
  font-weight: 600;
}

/* ---------- 购买 ---------- */
.buy-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 0;
  border-top: 1px dashed var(--eb-border-color);
  margin-bottom: 8px;
}

.buy-label {
  width: 88px;
  flex-shrink: 0;
  font-size: var(--eb-font-md);
  color: var(--eb-text-secondary);
}

.buy-hint {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-placeholder);
}

.actions {
  display: flex;
  gap: 12px;
  margin: 22px 0 20px;
}

.actions :deep(.el-button) {
  height: 46px;
  padding: 0 26px;
  font-size: var(--eb-font-md);
  font-weight: 600;
  border-radius: var(--eb-radius-md);
}

.actions :deep(.el-button--primary) {
  box-shadow: 0 8px 20px rgba(225, 37, 27, 0.28);
}

.btn-focus.is-focus {
  color: var(--eb-brand-500);
  border-color: var(--eb-brand-300);
  background: var(--eb-brand-50);
}

/* ---------- 服务承诺 ---------- */
.promise {
  display: flex;
  gap: 26px;
  padding-top: 18px;
  border-top: 1px solid var(--eb-border-color-light);
}

.promise li {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: var(--eb-font-sm);
  color: var(--eb-text-secondary);
}

.promise li .el-icon {
  color: var(--eb-success);
}

/* ---------- 商品详情 ---------- */
.desc-section {
  margin-top: 36px;
  padding-top: 28px;
  border-top: 1px solid var(--eb-border-color-light);
}

.desc-head {
  margin-bottom: 16px;
}

.desc-title {
  display: inline-block;
  font-size: var(--eb-font-xl);
  font-weight: 700;
  color: var(--eb-text-primary);
  padding-left: 12px;
  border-left: 4px solid var(--eb-brand-500);
}

.desc-text {
  font-size: var(--eb-font-md);
  line-height: 1.9;
  color: var(--eb-text-regular);
  padding: 18px 20px;
  background: var(--eb-bg-page);
  border-radius: var(--eb-radius-md);
}

/* ---------- 响应式适配（桌面分辨率）----------
 * ≤1280px：左图缩窄为 340px，给右栏信息区留更多空间
 * ≤1024px：两栏改单列上下堆叠，避免右栏被挤成窄条
 */
@media (max-width: 1280px) {
  .detail-grid {
    grid-template-columns: 340px 1fr;
    gap: 28px;
  }
}

@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .main-img-box {
    height: 320px;
  }

  .buy-label,
  .attr-row dt,
  .spec-label {
    width: 76px;
  }
}
</style>
