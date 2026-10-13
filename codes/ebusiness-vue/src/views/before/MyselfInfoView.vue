<template>
  <!-- 个人信息页 -->
  <div class="myself-info">
    <HeaderView />

    <div class="eb-container main">
      <h2 class="eb-section-title">个人信息</h2>

      <el-row :gutter="16" class="info-row">
        <!-- 左：基本资料 -->
        <el-col :span="14">
          <div class="eb-card block" v-loading="loading">
            <h3 class="block-title">基本资料</h3>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-width="90px"
            >
              <el-form-item label="用户邮箱">
                <el-input v-model="form.bemail" disabled />
              </el-form-item>

              <el-form-item label="昵称" prop="bname">
                <el-input v-model="form.bname" placeholder="请输入昵称" clearable />
              </el-form-item>

              <el-form-item label="联系电话" prop="bphone">
                <el-input v-model="form.bphone" placeholder="请输入手机号" clearable />
              </el-form-item>

              <el-form-item label="收货地址" prop="baddr">
                <el-input
                  v-model="form.baddr"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入收货地址"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="saving" @click="handleSave">
                  保存修改
                </el-button>
                <el-button @click="loadInfo">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-col>

        <!-- 右：账号信息 -->
        <el-col :span="10">
          <div class="eb-card block">
            <h3 class="block-title">账号信息</h3>
            <div class="avatar-box">
              <el-avatar :size="80" class="avatar">
                <el-icon :size="40"><UserFilled /></el-icon>
              </el-avatar>
              <div class="avatar-name">{{ form.bname || '未设置昵称' }}</div>
              <div class="avatar-email">{{ form.bemail || '-' }}</div>
            </div>

            <el-descriptions :column="1" border style="margin-top: 20px">
              <el-descriptions-item label="用户ID">
                {{ form.bid || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ form.bphone || '未填写' }}
              </el-descriptions-item>
              <el-descriptions-item label="账号状态">
                <el-tag type="success" size="small">正常</el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <el-button
              class="pwd-btn"
              type="primary"
              plain
              @click="openPwdDialog"
            >
              修改密码
            </el-button>
            <el-button
              class="logout-btn"
              type="danger"
              plain
              @click="handleLogout"
            >
              退出登录
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="440px" @closed="resetPwdForm">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="原密码" prop="oldPwd">
          <el-input v-model="pwdForm.oldPwd" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPwd">
          <el-input v-model="pwdForm.newPwd" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPwd">
          <el-input
            v-model="pwdForm.confirmPwd"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSaving" @click="handleChangePwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import HeaderView from '@/components/HeaderView.vue'
import { beforeApi } from '@/utils/api'
import { required, phoneRules } from '@/utils/validators'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  bid: null,
  bemail: '',
  bname: '',
  bphone: '',
  baddr: ''
})

const rules = {
  bname: [required('请输入昵称')],
  bphone: phoneRules()
}

/* ---- 加载个人信息 ---- */
const loadInfo = async () => {
  loading.value = true
  try {
    const res = await beforeApi.getUserInfo()
    const data = res.data || {}
    Object.assign(form, {
      bid: data.bid,
      bemail: data.bemail || '',
      bname: data.bname || '',
      bphone: data.bphone || '',
      baddr: data.baddr || ''
    })
    formRef.value && formRef.value.clearValidate()
  } catch (e) {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
}

/* ---- 保存修改 ---- */
const handleSave = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await beforeApi.updateUserInfo({
        bname: form.bname,
        bphone: form.bphone,
        baddr: form.baddr
      })
      // 同步 Pinia store，让顶部导航昵称即时更新
      userStore.updateUserInfo({ bname: form.bname })
      ElMessage.success('保存成功')
    } catch (e) {
      // 拦截器已统一提示
    } finally {
      saving.value = false
    }
  })
}

/* ---- 修改密码 ---- */
const pwdVisible = ref(false)
const pwdSaving = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({
  oldPwd: '',
  newPwd: '',
  confirmPwd: ''
})

const pwdRules = {
  oldPwd: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPwd) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

const openPwdDialog = () => {
  pwdVisible.value = true
}

const resetPwdForm = () => {
  pwdForm.oldPwd = ''
  pwdForm.newPwd = ''
  pwdForm.confirmPwd = ''
  pwdFormRef.value && pwdFormRef.value.resetFields()
}

const handleChangePwd = () => {
  pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdSaving.value = true
    try {
      await beforeApi.updatePassword({
        oldPwd: pwdForm.oldPwd,
        newPwd: pwdForm.newPwd
      })
      ElMessage.success('密码修改成功，请重新登录')
      pwdVisible.value = false
      userStore.logout()
      router.push('/login')
    } catch (e) {
      // 拦截器已统一提示
    } finally {
      pwdSaving.value = false
    }
  })
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已安全退出')
  router.push('/before/index')
}

onMounted(loadInfo)
</script>

<style scoped>
.main {
  padding: 20px 0 48px;
}

.info-row {
  margin-top: 4px;
}

.block {
  padding: 20px;
}

.block-title {
  font-size: var(--eb-font-lg);
  font-weight: 600;
  color: var(--eb-text-primary);
  margin-bottom: 18px;
}

.avatar-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 0;
}

.avatar {
  background: linear-gradient(135deg, var(--eb-brand-400), var(--eb-brand-600));
}

.avatar-name {
  font-size: var(--eb-font-lg);
  font-weight: 600;
  color: var(--eb-text-primary);
}

.avatar-email {
  font-size: var(--eb-font-sm);
  color: var(--eb-text-secondary);
}

.pwd-btn {
  width: 100%;
  margin-top: 20px;
}

.logout-btn {
  width: 100%;
  margin: 12px 0 0;
}
</style>
