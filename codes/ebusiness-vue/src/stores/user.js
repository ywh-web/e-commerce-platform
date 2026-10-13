/**
 * Pinia 用户状态仓库
 * ------------------------------------------------------------------
 * 统一管理登录态：
 *   - token    ：JWT 令牌，持久化在 sessionStorage
 *   - role     ：当前角色（admin 管理员 / user 普通用户）
 *   - userInfo ：登录用户的基本信息（邮箱 / 昵称 / id），一并持久化
 *
 * 说明：sessionStorage 与教材保持一致；但这里存的是 token（教材存 bemail/aname），
 *      属于本项目在教材基础上的改进点，便于用 Axios 拦截器统一鉴权。
 *      关闭浏览器标签页即清除，比 localStorage 更适合登录态场景。
 */
import { defineStore } from 'pinia'

/** 从 sessionStorage 安全读取用户信息 */
function readUserInfo() {
  try {
    const raw = window.sessionStorage.getItem('userInfo')
    return raw ? JSON.parse(raw) : null
  } catch (e) {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    // 从 sessionStorage 恢复，保证刷新页面后登录态不丢失
    token: window.sessionStorage.getItem('token') || '',
    role: window.sessionStorage.getItem('role') || '',
    userInfo: readUserInfo()
  }),

  getters: {
    // 是否已登录
    isLogin: (state) => !!state.token,
    // 是否为管理员
    isAdmin: (state) => state.role === 'admin',
    // 用户主键 ID（前台用户是 bid，管理员是 aid，统一存成 uid）
    uid: (state) => (state.userInfo ? state.userInfo.uid || state.userInfo.bid || state.userInfo.aid : null),
    // 展示名：昵称 > 邮箱前缀 > 账号
    displayName: (state) => {
      const info = state.userInfo
      if (!info) return '用户'
      if (info.bname) return info.bname
      if (info.bemail) return info.bemail.split('@')[0]
      if (info.aname) return info.aname
      return '用户'
    }
  },

  actions: {
    /**
     * 保存登录信息
     * @param {object} payload
     * @param {string} payload.token 令牌
     * @param {string} payload.role  角色：admin / user
     * @param {object} payload.info  用户信息（含 uid/bid/aid、bemail、bname 等）
     */
    setLoginInfo({ token, role, info }) {
      this.token = token || ''
      this.role = role || 'user'
      this.userInfo = info || null

      window.sessionStorage.setItem('token', this.token)
      window.sessionStorage.setItem('role', this.role)
      window.sessionStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    /**
     * 局部更新用户信息（如修改个人资料后）
     */
    updateUserInfo(info) {
      this.userInfo = { ...(this.userInfo || {}), ...(info || {}) }
      window.sessionStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    /**
     * 退出登录：清空状态 + 清空 sessionStorage
     */
    logout() {
      this.token = ''
      this.role = ''
      this.userInfo = null

      window.sessionStorage.removeItem('token')
      window.sessionStorage.removeItem('role')
      window.sessionStorage.removeItem('userInfo')
    }
  }
})
