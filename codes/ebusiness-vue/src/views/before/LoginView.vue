<template>
  <!-- 前台用户登录页：左品牌区 + 右表单区 -->
  <div class="auth-page">
    <HeaderView />

    <div class="auth-wrap">
      <!-- 左侧品牌区 -->
      <aside class="auth-brand">
        <div class="brand-glow glow-a"></div>
        <div class="brand-glow glow-b"></div>

        <div class="brand-content">
          <div class="brand-badge">
            <el-icon :size="26"><Shop /></el-icon>
          </div>
          <h2>欢迎回来</h2>
          <p class="brand-sub">登录后可同步你的购物车、收藏与订单</p>

          <ul class="feature-list">
            <li v-for="f in features" :key="f.text">
              <div class="feature-icon">
                <el-icon :size="16"><component :is="f.icon" /></el-icon>
              </div>
              <div class="feature-text">
                <strong>{{ f.text }}</strong>
                <span>{{ f.desc }}</span>
              </div>
            </li>
          </ul>
        </div>
      </aside>

      <!-- 右侧表单区 -->
      <main class="auth-form-area">
        <div class="form-card">
          <div class="form-head">
            <h1>账号登录</h1>
            <p>请输入注册时使用的邮箱与密码</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            size="large"
            @keyup.enter="handleLogin"
          >
            <el-form-item label="邮箱" prop="bemail">
              <el-input v-model="form.bemail" placeholder="请输入邮箱" clearable>
                <template #prefix><el-icon><Message /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码" prop="bpwd">
              <el-input
                v-model="form.bpwd"
                type="password"
                placeholder="请输入密码"
                show-password
              >
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item class="btn-item">
              <el-button
                type="primary"
                class="submit-btn"
                :loading="loading"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-foot">
            <span>还没有账号？</span>
            <a class="link" @click="goRegister">立即注册</a>
          </div>

          <!-- 演示账号提示（课程演示用） -->
          <div class="demo-tip">
            <el-icon><InfoFilled /></el-icon>
            <div>
              <div>演示账号：<code>test@ebusiness.com</code></div>
              <div>密码：<code>123456</code></div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Shop,
  Message,
  Lock,
  InfoFilled,
  ShoppingCart,
  Star,
  List
} from '@element-plus/icons-vue'
import HeaderView from '@/components/HeaderView.vue'
import { beforeApi } from '@/utils/api'
import { emailRules, passwordRules } from '@/utils/validators'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const features = [
  { icon: ShoppingCart, text: '购物车云端同步', desc: '换设备也不丢，随时继续买' },
  { icon: Star, text: '收藏夹随身带', desc: '收藏的好物一处集中管理' },
  { icon: List, text: '订单状态实时可查', desc: '待付款到已完成一目了然' }
]

const form = reactive({
  bemail: '',
  bpwd: ''
})

const rules = {
  bemail: emailRules(),
  bpwd: passwordRules()
}

/* ---- 登录 ---- */
const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await beforeApi.login({ bemail: form.bemail, bpwd: form.bpwd })
      const data = res.data || {}
      // 保存登录态（含用户信息，刷新后可恢复）
      userStore.setLoginInfo({
        token: data.token,
        role: 'user',
        info: { uid: data.bid, bid: data.bid, bemail: data.bemail, bname: data.bname }
      })
      ElMessage.success('登录成功')
      const redirect = route.query.redirect
      router.push(redirect || '/before/index')
    } catch (e) {
      // 拦截器已统一提示
    } finally {
      loading.value = false
    }
  })
}

const goRegister = () => router.push('/before/register')

onMounted(() => {
  // 从注册页带过来的邮箱自动填充
  if (route.query.bemail) {
    form.bemail = String(route.query.bemail)
  }
})
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--eb-bg-page);
}

.auth-wrap {
  flex: 1;
  display: flex;
  max-width: 1080px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 16px 56px;
  align-items: stretch;
}

/* ================= 左侧品牌区 ================= */
.auth-brand {
  position: relative;
  flex: 0 0 44%;
  overflow: hidden;
  border-radius: var(--eb-radius-xl) 0 0 var(--eb-radius-xl);
  background: linear-gradient(150deg, #d81e14 0%, #e1251b 40%, #f4604f 100%);
  color: #fff;
  padding: 44px 38px;
  display: flex;
  align-items: center;
}

.brand-glow {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.glow-a {
  width: 260px;
  height: 260px;
  top: -80px;
  right: -70px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.28), transparent 70%);
}

.glow-b {
  width: 200px;
  height: 200px;
  bottom: -70px;
  left: -50px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.18), transparent 70%);
}

.brand-content {
  position: relative;
  z-index: 1;
  width: 100%;
  animation: eb-fade-in 0.5s var(--eb-ease) both;
}

.brand-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: var(--eb-radius-lg);
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.28);
  margin-bottom: 22px;
}

.brand-content h2 {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.5px;
  margin-bottom: 10px;
}

.brand-sub {
  font-size: var(--eb-font-md);
  color: rgba(255, 255, 255, 0.82);
  margin-bottom: 34px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-list li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.feature-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: var(--eb-radius-md);
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.feature-text {
  display: flex;
  flex-direction: column;
}

.feature-text strong {
  font-size: var(--eb-font-md);
  font-weight: 600;
}

.feature-text span {
  font-size: var(--eb-font-xs);
  color: rgba(255, 255, 255, 0.72);
  margin-top: 2px;
}

/* ================= 右侧表单区 ================= */
.auth-form-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--eb-bg-container);
  border-radius: 0 var(--eb-radius-xl) var(--eb-radius-xl) 0;
  box-shadow: var(--eb-shadow-lg);
  padding: 44px 40px;
}

.form-card {
  width: 100%;
  max-width: 380px;
  animation: eb-fade-in 0.55s var(--eb-ease) 0.06s both;
}

.form-head {
  margin-bottom: 26px;
}

.form-head h1 {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: var(--eb-text-primary);
  margin-bottom: 6px;
}

.form-head p {
  font-size: var(--eb-font-sm);
  color: var(--eb-text-secondary);
}

.form-card :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--eb-text-regular);
  padding-bottom: 4px;
}

.btn-item {
  margin-top: 26px;
  margin-bottom: 0;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: var(--eb-font-lg);
  letter-spacing: 6px;
  font-weight: 600;
  border-radius: var(--eb-radius-md);
  box-shadow: 0 8px 20px rgba(225, 37, 27, 0.28);
}

.form-foot {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid var(--eb-border-color-light);
  text-align: center;
  font-size: var(--eb-font-sm);
  color: var(--eb-text-secondary);
}

.link {
  color: var(--eb-brand-500);
  font-weight: 600;
  cursor: pointer;
  margin-left: 4px;
}

.link:hover {
  text-decoration: underline;
}

/* ---------- 演示账号提示 ---------- */
.demo-tip {
  display: flex;
  gap: 8px;
  margin-top: 18px;
  padding: 12px 14px;
  font-size: var(--eb-font-xs);
  line-height: 1.7;
  color: var(--eb-admin-600);
  background: var(--eb-admin-50);
  border: 1px solid var(--eb-admin-100);
  border-radius: var(--eb-radius-md);
}

.demo-tip code {
  font-family: var(--eb-font-mono);
  font-size: var(--eb-font-xs);
  padding: 1px 5px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: var(--eb-radius-sm);
  color: var(--eb-brand-600);
}
</style>
