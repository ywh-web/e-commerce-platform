<template>
  <!-- 我的收藏 -->
  <div class="my-focus">
    <HeaderView />

    <div class="eb-container main">
      <div class="page-head">
        <h2 class="eb-section-title">我的收藏</h2>
        <span class="head-sub">共 {{ total }} 件商品</span>
      </div>

      <!-- 加载中骨架屏 -->
      <div v-if="loading" class="focus-grid">
        <div v-for="n in 4" :key="n" class="focus-card eb-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
              <div style="padding: 14px">
                <el-skeleton-item variant="text" style="width: 80%" />
                <el-skeleton-item variant="text" style="width: 40%; margin-top: 12px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 收藏列表 -->
      <div v-else-if="focusList.length" class="focus-grid">
        <div v-for="f in focusList" :key="f.fid" class="focus-card eb-card eb-card-hover">
          <div class="focus-thumb" @click="goDetail(f.goodstableId)">
            <img :src="goodsImg(f.picture)" :alt="f.goodsName" />
            <span v-if="f.stock <= 0" class="badge badge-sold-out">已售罄</span>
            <span v-else-if="f.stock <= 20" class="badge badge-low">仅剩 {{ f.stock }} 件</span>
          </div>

          <div class="focus-info">
            <div class="focus-name eb-ellipsis-2" @click="goDetail(f.goodstableId)">
              {{ f.goodsName }}
            </div>
            <div v-if="f.introduce" class="focus-intro eb-ellipsis">{{ f.introduce }}</div>

            <div class="focus-bottom">
              <span class="eb-price focus-price">
                <span class="eb-price-symbol">￥</span>{{ money(f.price) }}
              </span>
            </div>

            <div class="focus-actions">
              <el-button
                type="primary"
                size="small"
                :disabled="f.stock <= 0"
                @click="handleAddCart(f)"
              >
                <el-icon><ShoppingCart /></el-icon>&nbsp;加入购物车
              </el-button>
              <el-button type="danger" plain size="small" @click="handleDelete(f)">
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="eb-card empty-wrap">
        <el-empty description="还没有收藏商品，快去收藏喜欢的商品吧">
          <el-button type="primary" @click="goShopping">去逛逛</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import HeaderView from '@/components/HeaderView.vue'
import { beforeApi } from '@/utils/api'
import { goodsImg, money } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const focusList = ref([])
const total = ref(0)

/* ---- 加载收藏列表 ---- */
const loadFocus = async () => {
  loading.value = true
  try {
    const res = await beforeApi.getFocusList()
    const data = res.data || {}
    focusList.value = data.focusList || []
    total.value = Number(data.total || focusList.value.length)
  } catch (e) {
    // 拦截器已统一提示
    focusList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/* ---- 取消收藏 ---- */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定取消收藏「${row.goodsName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await beforeApi.deleteFocus(row.fid)
        ElMessage.success('已取消收藏')
        loadFocus()
      } catch (e) {
        // 拦截器已统一提示
      }
    })
    .catch(() => {})
}

/* ---- 加入购物车 ---- */
const handleAddCart = async (row) => {
  try {
    await beforeApi.addCart({ goodstableId: row.goodstableId, shoppingnum: 1 })
    ElMessage.success('已加入购物车')
  } catch (e) {
    // 拦截器已统一提示
  }
}

const goDetail = (id) => router.push(`/before/goodsDetail/${id}`)
const goShopping = () => router.push('/before/index')

onMounted(loadFocus)
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

.focus-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.focus-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.focus-thumb {
  position: relative;
  height: 200px;
  background: var(--eb-bg-page);
  overflow: hidden;
  cursor: pointer;
}

.focus-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--eb-duration-slow) var(--eb-ease);
}

.focus-card:hover .focus-thumb img {
  transform: scale(1.06);
}

.badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 9px;
  font-size: var(--eb-font-xs);
  font-weight: 500;
  color: #fff;
  border-radius: var(--eb-radius-sm);
}

.badge-low {
  background: linear-gradient(135deg, #ff9a2e, var(--eb-warning));
}

.badge-sold-out {
  background: rgba(29, 33, 41, 0.55);
}

.focus-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 14px 16px 16px;
}

.focus-name {
  font-size: var(--eb-font-md);
  font-weight: 500;
  color: var(--eb-text-primary);
  line-height: 1.45;
  min-height: 40px;
  cursor: pointer;
  transition: color var(--eb-duration-fast) var(--eb-ease);
}

.focus-name:hover {
  color: var(--eb-brand-500);
}

.focus-intro {
  margin-top: 4px;
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
}

.focus-bottom {
  margin-top: auto;
  padding-top: 10px;
}

.focus-price {
  font-size: 20px;
}

.focus-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.focus-actions :deep(.el-button) {
  flex: 1;
  margin-left: 0;
}

.empty-wrap {
  padding: 40px 0;
}

/* ---------- 响应式适配（桌面分辨率）---------- */
@media (max-width: 1280px) {
  .focus-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1024px) {
  .focus-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
