<template>
  <!-- 电商前台首页：主视觉 + 推荐 + 商品列表 -->
  <div class="index-view">
    <HeaderView />

    <!-- ============ 主视觉横幅 ============ -->
    <section class="hero">
      <div class="eb-container hero-inner">
        <!-- 左侧：文案 + 搜索 -->
        <div class="hero-text">
          <div class="hero-tag">
            <el-icon><Sunny /></el-icon>
            <span>今日上新 · 全场好物</span>
          </div>
          <h1 class="hero-title">
            发现值得买的好物<span class="dot">。</span>
          </h1>
          <p class="hero-sub">手机数码 · 电脑办公 · 智能穿戴 · 影音娱乐，一站购齐</p>

          <div class="hero-search">
            <el-input
              v-model="query.keywords"
              placeholder="搜索你想买的商品，如「华为」「笔记本」"
              size="large"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
          </div>

          <!-- 热词 -->
          <div class="hot-words">
            <span class="hot-label">热门：</span>
            <span
              v-for="w in hotWords"
              :key="w"
              class="hot-word"
              @click="handleHotWord(w)"
            >
              {{ w }}
            </span>
          </div>
        </div>

        <!-- 右侧：推荐商品轮播 -->
        <div class="hero-visual">
          <div class="visual-blob blob-a"></div>
          <div class="visual-blob blob-b"></div>

          <el-carousel
            v-if="recommendList.length"
            class="hero-carousel"
            height="330px"
            :interval="4000"
            arrow="hover"
            indicator-position="outside"
            trigger="click"
          >
            <el-carousel-item v-for="g in recommendList" :key="'hero-' + g.id">
              <div class="hero-card" @click="goDetail(g.id)">
                <div class="hero-card-img">
                  <img :src="goodsImg(g.picture)" :alt="g.name" />
                </div>
                <div class="hero-card-body">
                  <div class="hero-card-name eb-ellipsis">{{ g.name }}</div>
                  <div class="hero-card-price">
                    <span class="eb-price">
                      <span class="eb-price-symbol">￥</span>{{ money(g.price) }}
                    </span>
                    <span class="hero-card-sales">已售 {{ g.sales || 0 }}</span>
                  </div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>

          <!-- 数据未回来时用骨架屏占位，避免右侧塌陷 -->
          <div v-else class="hero-card-skeleton">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="width: 100%; height: 240px" />
                <div style="padding: 16px 18px">
                  <el-skeleton-item variant="text" style="width: 70%" />
                  <el-skeleton-item variant="text" style="width: 40%; margin-top: 10px" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>
      </div>
    </section>

    <div class="eb-container main">
      <!-- ============ 分类快捷入口 ============ -->
      <section v-if="typeList.length" class="section">
        <div class="cat-grid">
          <div
            v-for="t in typeList"
            :key="t.id"
            class="cat-item"
            :class="{ active: query.goodstypeId === t.id }"
            @click="handleTypeChange(t.id)"
          >
            <div class="cat-icon">
              <el-icon :size="22"><component :is="catIcon(t.name)" /></el-icon>
            </div>
            <div class="cat-name">{{ t.name }}</div>
            <div class="cat-count">{{ t.goodsCount || 0 }} 件</div>
          </div>
        </div>
      </section>

      <!-- ============ 为你推荐 ============ -->
      <section v-if="recommendList.length" class="section">
        <div class="section-head">
          <h2 class="eb-section-title">为你推荐</h2>
          <span class="section-more" @click="handleTypeChange(null)">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </span>
        </div>

        <div class="goods-grid">
          <div
            v-for="g in recommendList"
            :key="'rec-' + g.id"
            class="goods-card eb-card eb-card-hover"
            @click="goDetail(g.id)"
          >
            <div class="goods-thumb">
              <img :src="goodsImg(g.picture)" :alt="g.name" />
              <span class="badge badge-hot">热销</span>
            </div>
            <div class="goods-body">
              <div class="goods-name eb-ellipsis-2">{{ g.name }}</div>
              <div class="goods-meta">
                <span class="eb-price">
                  <span class="eb-price-symbol">￥</span>{{ money(g.price) }}
                </span>
                <span class="goods-sales">{{ g.sales || 0 }} 人付款</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ============ 全部商品 ============ -->
      <section class="section">
        <div class="section-head">
          <h2 class="eb-section-title">
            {{ query.goodstypeId ? currentTypeName : '全部商品' }}
          </h2>

          <!-- 排序 -->
          <div class="sort-bar">
            <span
              v-for="s in sortOptions"
              :key="s.value"
              class="sort-item"
              :class="{ active: query.sortBy === s.value }"
              @click="handleSort(s.value)"
            >
              {{ s.label }}
            </span>
          </div>
        </div>

        <!-- 骨架屏 -->
        <div v-if="loading" class="goods-grid">
          <div v-for="n in 8" :key="n" class="goods-card eb-card">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="width: 100%; height: 220px" />
                <div style="padding: 14px">
                  <el-skeleton-item variant="text" style="width: 85%" />
                  <el-skeleton-item variant="text" style="width: 45%; margin-top: 10px" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>

        <!-- 商品网格 -->
        <div v-else-if="goodsList.length" class="goods-grid">
          <div
            v-for="g in goodsList"
            :key="g.id"
            class="goods-card eb-card eb-card-hover"
            @click="goDetail(g.id)"
          >
            <div class="goods-thumb">
              <img :src="goodsImg(g.picture)" :alt="g.name" />
              <span v-if="g.stock !== null && g.stock <= 20" class="badge badge-low">
                仅剩 {{ g.stock }} 件
              </span>
              <span v-if="g.typeName" class="badge badge-type">{{ g.typeName }}</span>
              <div class="thumb-mask">
                <el-button type="primary" round size="small">
                  <el-icon><View /></el-icon>&nbsp;查看详情
                </el-button>
              </div>
            </div>
            <div class="goods-body">
              <div class="goods-name eb-ellipsis-2">{{ g.name }}</div>
              <div class="goods-intro eb-ellipsis" v-if="g.introduce">{{ g.introduce }}</div>
              <div class="goods-meta">
                <span class="eb-price">
                  <span class="eb-price-symbol">￥</span>{{ money(g.price) }}
                </span>
                <span class="goods-sales">{{ g.sales || 0 }} 人付款</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-wrap eb-card">
          <el-empty description="没有找到相关商品，换个关键词试试吧">
            <el-button type="primary" @click="resetQuery">重置筛选</el-button>
          </el-empty>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination">
          <el-pagination
            v-model:current-page="query.currentPage"
            v-model:page-size="query.pageSize"
            :page-sizes="[8, 16, 24]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleSizeChange"
            @current-change="loadGoods"
          />
        </div>
      </section>
    </div>

    <!-- ============ 页脚 ============ -->
    <footer class="eb-footer">
      <div class="eb-container footer-inner">
        <div class="footer-brand">
          <div class="logo-badge">
            <el-icon :size="18"><Shop /></el-icon>
          </div>
          <span>电子商务平台</span>
        </div>
        <p class="footer-desc">
          广东财经大学《前端开发技术课程设计》课程设计项目 ·
          Vue 3 + Element Plus + Spring Boot + MyBatis-Plus
        </p>
        <p class="footer-copy">© 2026 电子商务平台 · 仅供教学演示使用</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Sunny,
  ArrowRight,
  View,
  Shop,
  Iphone,
  Monitor,
  Notebook,
  Watch,
  Headset
} from '@element-plus/icons-vue'
import HeaderView from '@/components/HeaderView.vue'
import { beforeApi } from '@/utils/api'
import { goodsImg, money } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const goodsList = ref([])
const recommendList = ref([])
const typeList = ref([])
const total = ref(0)

// 热词（点击即搜索）
const hotWords = ['华为', '小米', '笔记本', '平板', '耳机']

// 排序选项
const sortOptions = [
  { label: '默认', value: '' },
  { label: '销量', value: 'salesDesc' },
  { label: '价格 ↑', value: 'priceAsc' },
  { label: '价格 ↓', value: 'priceDesc' }
]

const query = reactive({
  keywords: '',
  goodstypeId: null,
  sortBy: '',
  currentPage: 1,
  pageSize: 8
})

// 当前选中类型名
const currentTypeName = computed(() => {
  const t = typeList.value.find((x) => x.id === query.goodstypeId)
  return t ? t.name : '全部商品'
})

// 按类型名匹配图标
function catIcon(name) {
  if (!name) return Shop
  if (name.includes('手机')) return Iphone
  if (name.includes('笔记本')) return Monitor
  if (name.includes('平板')) return Notebook
  if (name.includes('穿戴') || name.includes('手表')) return Watch
  return Headset
}

/* ---- 加载商品列表 ---- */
const loadGoods = async () => {
  loading.value = true
  try {
    const res = await beforeApi.getGoodsList({
      currentPage: query.currentPage,
      pageSize: query.pageSize,
      keywords: query.keywords || undefined,
      goodstypeId: query.goodstypeId || undefined,
      sortBy: query.sortBy || undefined
    })
    const data = res.data || {}
    goodsList.value = data.goodsList || []
    total.value = data.total || 0
  } catch (e) {
    goodsList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/* ---- 加载推荐商品 ---- */
const loadRecommend = async () => {
  try {
    const res = await beforeApi.getRecommendGoods(4)
    recommendList.value = res.data || []
  } catch (e) {
    recommendList.value = []
  }
}

/* ---- 加载商品类型 ---- */
const loadTypes = async () => {
  try {
    const res = await beforeApi.getGoodsTypes()
    typeList.value = res.data || []
  } catch (e) {
    typeList.value = []
  }
}

/* ---- 搜索 ---- */
const handleSearch = () => {
  query.currentPage = 1
  loadGoods()
}

/* ---- 热词点击 ---- */
const handleHotWord = (w) => {
  query.keywords = w
  handleSearch()
}

/* ---- 类型切换：再次点击已选中的类型 = 取消筛选 ---- */
const handleTypeChange = (id) => {
  query.goodstypeId = query.goodstypeId === id ? null : id
  query.currentPage = 1
  loadGoods()
}

/* ---- 排序切换 ---- */
const handleSort = (value) => {
  query.sortBy = value
  query.currentPage = 1
  loadGoods()
}

/* ---- 每页条数变化 ---- */
const handleSizeChange = () => {
  query.currentPage = 1
  loadGoods()
}

/* ---- 重置筛选 ---- */
const resetQuery = () => {
  query.keywords = ''
  query.goodstypeId = null
  query.sortBy = ''
  query.currentPage = 1
  loadGoods()
}

/* ---- 跳转详情 ---- */
const goDetail = (id) => {
  router.push(`/before/goodsDetail/${id}`)
}

onMounted(() => {
  loadGoods()
  loadRecommend()
  loadTypes()
})
</script>

<style scoped>
/* ================= 主视觉 ================= */
.hero {
  position: relative;
  background: linear-gradient(135deg, #fff5f4 0%, #f4f7fc 55%, #eef3fa 100%);
  overflow: hidden;
  border-bottom: 1px solid var(--eb-border-color-light);
}

.hero::after {
  content: '';
  position: absolute;
  right: -120px;
  top: -160px;
  width: 520px;
  height: 520px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(225, 37, 27, 0.09), transparent 68%);
  pointer-events: none;
}

.hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--eb-space-xxl);
  padding-top: 56px;
  padding-bottom: 56px;
}

.hero-text {
  flex: 1;
  max-width: 560px;
  animation: eb-fade-in 0.5s var(--eb-ease) both;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  margin-bottom: 18px;
  font-size: var(--eb-font-sm);
  font-weight: 500;
  color: var(--eb-brand-600);
  background: var(--eb-brand-50);
  border: 1px solid var(--eb-brand-100);
  border-radius: var(--eb-radius-full);
}

.hero-title {
  font-size: 40px;
  line-height: 1.2;
  font-weight: 800;
  letter-spacing: -1px;
  color: var(--eb-text-primary);
  margin-bottom: 12px;
}

.hero-title .dot {
  color: var(--eb-brand-500);
}

.hero-sub {
  font-size: var(--eb-font-lg);
  color: var(--eb-text-secondary);
  margin-bottom: 28px;
}

.hero-search {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.hero-search :deep(.el-input__wrapper) {
  border-radius: var(--eb-radius-full);
  padding-left: 18px;
  box-shadow: 0 6px 20px rgba(29, 33, 41, 0.06);
  background: #fff;
}

.hero-search :deep(.el-button) {
  border-radius: var(--eb-radius-full);
  padding: 0 30px;
  box-shadow: 0 6px 18px rgba(225, 37, 27, 0.3);
}

.hot-words {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: var(--eb-font-sm);
}

.hot-label {
  color: var(--eb-text-secondary);
}

.hot-word {
  padding: 3px 10px;
  color: var(--eb-text-regular);
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid var(--eb-border-color);
  border-radius: var(--eb-radius-full);
  cursor: pointer;
  transition: all var(--eb-duration-fast) var(--eb-ease);
}

.hot-word:hover {
  color: var(--eb-brand-500);
  border-color: var(--eb-brand-200);
  background: var(--eb-brand-50);
}

/* ---------- 主视觉右侧商品卡 ---------- */
.hero-visual {
  position: relative;
  width: 340px;
  flex-shrink: 0;
}

.visual-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
}

.blob-a {
  width: 150px;
  height: 150px;
  top: -34px;
  right: -26px;
  background: linear-gradient(135deg, rgba(225, 37, 27, 0.22), rgba(249, 106, 94, 0.12));
}

.blob-b {
  width: 100px;
  height: 100px;
  bottom: -28px;
  left: -30px;
  background: linear-gradient(135deg, rgba(31, 78, 121, 0.2), rgba(74, 128, 196, 0.1));
}

.hero-card {
  position: relative;
  z-index: 1;
  background: #fff;
  border-radius: var(--eb-radius-xl);
  overflow: hidden;
  box-shadow: var(--eb-shadow-xl);
  cursor: pointer;
}

/* 轮播里的卡片不做整体位移动效：el-carousel__item 是 overflow:hidden，
   卡片外移会被裁掉。改成放大内部图片，视觉上一样有反馈且不会被裁。 */
.hero-carousel .hero-card-img {
  overflow: hidden;
  height: 232px;
}

.hero-carousel .hero-card-img img {
  transition: transform var(--eb-duration-slow) var(--eb-ease);
}

.hero-carousel .hero-card:hover .hero-card-img img {
  transform: scale(1.06);
}

/* 轮播容器：让底部指示点有位置，且不撑高整块区域 */
.hero-carousel {
  position: relative;
  z-index: 1;
}

.hero-carousel :deep(.el-carousel__indicators--outside) {
  margin-top: 10px;
}

.hero-carousel :deep(.el-carousel__indicator button) {
  background-color: var(--eb-border-strong);
}

.hero-carousel :deep(.el-carousel__indicator.is-active button) {
  background-color: var(--eb-brand-500);
  width: 20px;
}

.hero-card-skeleton {
  position: relative;
  z-index: 1;
  background: #fff;
  border-radius: var(--eb-radius-xl);
  overflow: hidden;
  box-shadow: var(--eb-shadow-xl);
}

.hero-card-img {
  height: 240px;
  background: var(--eb-bg-page);
}

.hero-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-card-body {
  padding: 16px 18px 18px;
}

.hero-card-name {
  font-size: var(--eb-font-md);
  font-weight: 600;
  color: var(--eb-text-primary);
  margin-bottom: 8px;
}

.hero-card-price {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.hero-card-price .eb-price {
  font-size: 22px;
}

.hero-card-sales {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

/* ================= 主体 ================= */
.main {
  padding-top: 36px;
  padding-bottom: 56px;
}

.section {
  margin-bottom: 44px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-more {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: var(--eb-font-sm);
  color: var(--eb-text-secondary);
  cursor: pointer;
  transition: color var(--eb-duration-fast) var(--eb-ease);
}

.section-more:hover {
  color: var(--eb-brand-500);
}

/* ---------- 分类入口 ---------- */
.cat-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

.cat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 22px 10px;
  background: var(--eb-bg-container);
  border: 1px solid var(--eb-border-color-light);
  border-radius: var(--eb-radius-lg);
  cursor: pointer;
  transition: all var(--eb-duration) var(--eb-ease);
}

.cat-item:hover {
  transform: translateY(-3px);
  border-color: var(--eb-brand-200);
  box-shadow: var(--eb-shadow-md);
}

.cat-item.active {
  border-color: var(--eb-brand-500);
  background: var(--eb-brand-50);
  box-shadow: 0 6px 18px rgba(225, 37, 27, 0.14);
}

.cat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--eb-radius-md);
  color: var(--eb-brand-500);
  background: var(--eb-brand-50);
  transition: all var(--eb-duration) var(--eb-ease);
}

.cat-item.active .cat-icon {
  color: #fff;
  background: linear-gradient(135deg, var(--eb-brand-400), var(--eb-brand-600));
}

.cat-name {
  font-size: var(--eb-font-md);
  font-weight: 600;
  color: var(--eb-text-primary);
}

.cat-count {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

/* ---------- 排序 ---------- */
.sort-bar {
  display: flex;
  gap: 6px;
}

.sort-item {
  padding: 5px 14px;
  font-size: var(--eb-font-sm);
  color: var(--eb-text-regular);
  border-radius: var(--eb-radius-full);
  cursor: pointer;
  transition: all var(--eb-duration-fast) var(--eb-ease);
}

.sort-item:hover {
  color: var(--eb-brand-500);
  background: var(--eb-brand-50);
}

.sort-item.active {
  color: #fff;
  background: var(--eb-brand-500);
  box-shadow: 0 4px 12px rgba(225, 37, 27, 0.28);
}

/* ---------- 商品网格 ---------- */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.goods-card {
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.goods-thumb {
  position: relative;
  height: 220px;
  background: var(--eb-bg-page);
  overflow: hidden;
}

.goods-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--eb-duration-slow) var(--eb-ease);
}

.goods-card:hover .goods-thumb img {
  transform: scale(1.06);
}

.badge {
  position: absolute;
  top: 10px;
  padding: 3px 9px;
  font-size: var(--eb-font-xs);
  font-weight: 500;
  color: #fff;
  border-radius: var(--eb-radius-sm);
  backdrop-filter: blur(4px);
}

.badge-hot {
  left: 10px;
  background: linear-gradient(135deg, var(--eb-brand-400), var(--eb-brand-600));
}

.badge-low {
  right: 10px;
  background: linear-gradient(135deg, #ff9a2e, var(--eb-warning));
}

.badge-type {
  left: 10px;
  top: auto;
  bottom: 10px;
  background: rgba(0, 0, 0, 0.45);
}

.thumb-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(29, 33, 41, 0.32);
  opacity: 0;
  transition: opacity var(--eb-duration) var(--eb-ease);
}

.goods-card:hover .thumb-mask {
  opacity: 1;
}

.goods-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.goods-name {
  font-size: var(--eb-font-md);
  font-weight: 500;
  color: var(--eb-text-primary);
  line-height: 1.45;
  min-height: 40px;
  margin-bottom: 6px;
}

.goods-intro {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
  margin-bottom: 10px;
}

.goods-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: auto;
}

.goods-meta .eb-price {
  font-size: 19px;
}

.goods-sales {
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

/* ---------- 空状态 ---------- */
.empty-wrap {
  padding: 40px 0;
}

/* ---------- 分页 ---------- */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* ================= 页脚 ================= */
.eb-footer {
  background: #1d2129;
  color: #86909c;
  padding: 36px 0 28px;
}

.footer-inner {
  text-align: center;
}

.footer-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: var(--eb-font-lg);
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
}

.footer-brand .logo-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--eb-radius-md);
  color: #fff;
  background: linear-gradient(135deg, var(--eb-brand-400), var(--eb-brand-600));
}

.footer-desc {
  font-size: var(--eb-font-sm);
  line-height: 1.7;
  margin-bottom: 6px;
}

.footer-copy {
  font-size: var(--eb-font-xs);
  color: #4e5969;
}

/* ---------- 响应式适配（桌面分辨率）----------
 * 基准（≥1281px）：分类 5 列 / 商品 4 列
 * ≤1280px（1366×768 笔记本、分屏）：商品降为 3 列，缩略图降高
 * ≤1024px（1280×800、小窗）：商品再降为 2 列，分类降为 4 列
 */
@media (max-width: 1280px) {
  .cat-grid {
    gap: 10px;
  }

  .hero-inner {
    gap: var(--eb-space-xl);
  }

  .hero-visual {
    width: 300px;
  }

  .goods-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 14px;
  }

  .goods-thumb {
    height: 190px;
  }
}

@media (max-width: 1024px) {
  .cat-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  /* 主视觉改上下堆叠：文案在上、轮播在下，避免右侧卡片被挤成窄条 */
  .hero-inner {
    flex-direction: column;
    align-items: stretch;
    padding-top: 36px;
    padding-bottom: 36px;
  }

  .hero-visual {
    width: 100%;
  }

  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .goods-thumb {
    height: 170px;
  }
}
</style>
