<template>
  <!-- 后台管理员登录页：左品牌区 + 右表单区 -->
  <div class="admin-login">
    <div class="login-wrap">
      <!-- 左侧品牌区 -->
      <aside class="brand-panel">
        <div class="glow glow-a"></div>
        <div class="glow glow-b"></div>

        <div class="brand-inner">
          <div class="brand-logo">
            <el-icon :size="26"><Shop /></el-icon>
          </div>
          <h2>电子商务平台</h2>
          <p class="brand-sub">后台管理系统</p>

          <div class="brand-divider"></div>

          <ul class="brand-features">
            <li v-for="f in features" :key="f.text">
              <el-icon :size="16"><component :is="f.icon" /></el-icon>
              <span>{{ f.text }}</span>
            </li>
          </ul>

          <p class="brand-foot">
            广东财经大学 · 前端开发技术课程设计
          </p>
        </div>
      </aside>

      <!-- 右侧表单区 -->
      <main class="form-panel">
        <div class="form-card">
          <div class="form-head">
            <h1>管理员登录</h1>
            <p>请使用管理员账号登录后台管理系统</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            size="large"
            @keyup.enter="handleLogin"
          >
            <el-form-item label="管理员账号" prop="aname">
              <el-input v-model="form.aname" placeholder="请输入账号" clearable>
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码" prop="apwd">
              <el-input
                v-model="form.apwd"
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
            <a class="link" @click="goBefore">
              <el-icon><Back /></el-icon> 返回前台首页
            </a>
          </div>

          <!-- 演示账号提示 -->
          <div class="demo-tip">
            <el-icon><InfoFilled /></el-icon>
            <div>
              <div>演示账号：<code>admin</code></div>
              <div>密码：<code>123456</code></div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Shop,
  User,
  Lock,
  Back,
  InfoFilled,
  Goods,
  List,
  DataAnalysis
} from '@element-plus/icons-vue'
import { adminApi } from '@/utils/api'
import { required } from '@/utils/validators'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const features = [
  { icon: Goods, text: '商品与类型管理' },
  { icon: List, text: '订单与用户管理' },
  { icon: DataAnalysis, text: '销售与订单统计' }
]

const form = reactive({
  aname: '',
  apwd: ''
})

const rules = {
  aname: [required('请输入管理员账号')],
  apwd: [required('请输入密码')]
}

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await adminApi.login({ aname: form.aname, apwd: form.apwd })
      const data = res.data || {}
      userStore.setLoginInfo({
        token: data.token,
        role: 'admin',
        info: { uid: data.aid, aid: data.aid, aname: data.aname }
      })
      ElMessage.success('登录成功')
      router.push(route.query.redirect || '/admin/home')
    } catch (e) {
      // 错误已在拦截器统一提示
    } finally {
      loading.value = false
    }
  })
}

const goBefore = () => router.push('/before/index')
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  background:
    radial-gradient(circle at 15% 20%, rgba(74, 128, 196, 0.22), transparent 45%),
    radial-gradient(circle at 85% 80%, rgba(31, 78, 121, 0.28), transparent 50%),
    linear-gradient(135deg, #0d2033 0%, #163655 55%, #1f4e79 100%);
}

.login-wrap {
  display: flex;
  width: 100%;
  max-width: 940px;
  min-height: 520px;
  border-radius: var(--eb-radius-xl);
  overflow: hidden;
  box-shadow: 0 32px 64px rgba(0, 0, 0, 0.35);
}

/* ================= 左侧品牌 ================= */
.brand-panel {
  position: relative;
  flex: 0 0 44%;
  padding: 48px 40px;
  background: linear-gradient(150deg, #17263b 0%, #1f4e79 100%);
  color: #fff;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.glow {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.glow-a {
  width: 240px;
  height: 240px;
  top: -90px;
  right: -70px;
  background: radial-gradient(circle, rgba(122, 166, 218, 0.4), transparent 70%);
}

.glow-b {
  width: 180px;
  height: 180px;
  bottom: -60px;
  left: -50px;
  background: radial-gradient(circle, rgba(31, 78, 121, 0.6), transparent 70%);
}

.brand-inner {
  position: relative;
  z-index: 1;
  width: 100%;
  animation: eb-fade-in 0.5s var(--eb-ease) both;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: var(--eb-radius-lg);
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.22);
  margin-bottom: 22px;
}

.brand-inner h2 {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.4px;
  margin-bottom: 6px;
}

.brand-sub {
  font-size: var(--eb-font-md);
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 2px;
}

.brand-divider {
  width: 40px;
  height: 3px;
  border-radius: var(--eb-radius-full);
  background: var(--eb-admin-300);
  margin: 26px 0;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.brand-features li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: var(--eb-font-md);
  color: rgba(255, 255, 255, 0.86);
}

.brand-foot {
  margin-top: 42px;
  font-size: var(--eb-font-xs);
  color: rgba(255, 255, 255, 0.45);
  line-height: 1.7;
}

/* ================= 右侧表单 ================= */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 48px 44px;
}

.form-card {
  width: 100%;
  max-width: 340px;
  animation: eb-fade-in 0.55s var(--eb-ease) 0.06s both;
}

.form-head {
  margin-bottom: 26px;
}

.form-head h1 {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.4px;
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

.form-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--eb-admin-500) inset, 0 0 0 3px var(--eb-admin-50);
}

.btn-item {
  margin-top: 24px;
  margin-bottom: 0;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: var(--eb-font-lg);
  letter-spacing: 6px;
  font-weight: 600;
  border-radius: var(--eb-radius-md);
  --el-button-bg-color: var(--eb-admin-500);
  --el-button-border-color: var(--eb-admin-500);
  --el-button-hover-bg-color: var(--eb-admin-400);
  --el-button-hover-border-color: var(--eb-admin-400);
  --el-button-active-bg-color: var(--eb-admin-600);
  --el-button-active-border-color: var(--eb-admin-600);
  box-shadow: 0 8px 20px rgba(31, 78, 121, 0.32);
}

.form-foot {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--eb-border-color-light);
  text-align: center;
}

.link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--eb-font-sm);
  color: var(--eb-text-secondary);
  cursor: pointer;
  transition: color var(--eb-duration-fast) var(--eb-ease);
}

.link:hover {
  color: var(--eb-admin-500);
}

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
  padding: 1px 5px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: var(--eb-radius-sm);
  color: var(--eb-admin-700);
}
</style>
