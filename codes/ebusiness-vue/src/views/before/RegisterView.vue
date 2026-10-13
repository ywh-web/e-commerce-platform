<template>
  <!-- 用户注册页：左品牌区 + 右表单区 -->
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
          <h2>加入电子商务平台</h2>
          <p class="brand-sub">注册后即可享受完整的购物体验</p>

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
            <h1>创建账号</h1>
            <p>只需几秒，即可开启购物之旅</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            size="large"
          >
            <el-form-item label="邮箱" prop="bemail">
              <el-input v-model="form.bemail" placeholder="请输入邮箱" clearable>
                <template #prefix><el-icon><Message /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="昵称" prop="bname">
              <el-input v-model="form.bname" placeholder="给自己取个昵称" clearable>
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码" prop="bpwd">
              <el-input
                v-model="form.bpwd"
                type="password"
                placeholder="不少于 6 位"
                show-password
              >
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPwd">
              <el-input
                v-model="form.confirmPwd"
                type="password"
                placeholder="请再次输入密码"
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
                @click="handleRegister"
              >
                注 册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-foot">
            <span>已有账号？</span>
            <a class="link" @click="goLogin">去登录</a>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop, Message, User, Lock, ShoppingCart, Star, List } from '@element-plus/icons-vue'
import HeaderView from '@/components/HeaderView.vue'
import { beforeApi } from '@/utils/api'
import { required, emailRules, passwordRules, confirmPasswordRules } from '@/utils/validators'

const router = useRouter()

const formRef = ref(null)
const loading = ref(false)

// 左侧卖点
const features = [
  { icon: ShoppingCart, text: '一键加购下单', desc: '购物车、订单、支付流程完整' },
  { icon: Star, text: '收藏心仪好物', desc: '随时回看，不错过任何优惠' },
  { icon: List, text: '订单全程可查', desc: '从下单到完成，状态实时同步' }
]

const form = reactive({
  bemail: '',
  bname: '',
  bpwd: '',
  confirmPwd: ''
})

const rules = {
  bemail: emailRules(),
  bname: [required('请输入昵称')],
  bpwd: passwordRules(),
  // 确认密码需与上面输入的密码一致，用函数延迟取值
  confirmPwd: confirmPasswordRules(() => form.bpwd)
}

/* ---- 提交注册 ---- */
const handleRegister = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await beforeApi.register({
        bemail: form.bemail,
        bname: form.bname,
        bpwd: form.bpwd
      })
      ElMessage.success('注册成功，请登录')
      router.push({ path: '/login', query: { bemail: form.bemail } })
    } catch (e) {
      // 拦截器已统一提示
    } finally {
      loading.value = false
    }
  })
}

const goLogin = () => router.push('/login')
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
  gap: 0;
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
</style>
