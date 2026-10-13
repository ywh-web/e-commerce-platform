/**
 * Axios 请求封装
 * ------------------------------------------------------------------
 * 职责：
 *   1. 创建统一的 axios 实例，配置 baseURL / 超时时间
 *   2. 请求拦截器：自动从 sessionStorage 取出 token，注入 Authorization 请求头
 *   3. 响应拦截器：统一处理后端返回结果、统一处理 401（登录失效）
 *
 * 认证方案约定（本项目定稿）：
 *   - token 存放于 sessionStorage，键名为 'token'
 *   - 请求头格式：Authorization: Bearer <token>
 *   - 后端返回 401 时，清除本地 token 并跳回对应登录页
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  // 开发环境下由 vue.config.js 的 devServer.proxy 代理到后端 8443
  baseURL: '/api',
  timeout: 10000
})

/* ------------------------------------------------------------------
 * 请求拦截器
 * ------------------------------------------------------------------ */
request.interceptors.request.use(
  (config) => {
    // 从 sessionStorage 取出 token
    const token = window.sessionStorage.getItem('token')
    if (token) {
      // 自动注入 Authorization 请求头
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/* ------------------------------------------------------------------
 * 登录失效的统一处理
 * ------------------------------------------------------------------
 * 说明：后端采用"统一响应结构"，业务语义主要放在 body 的 code 里。
 * 鉴权拦截器返回的 401/403 **同时**写了 HTTP 状态码与 body 的 code
 * （两者一致），因此前端两条路都要能识别：
 *   - 走失败分支：读 HTTP 状态（error.response.status）
 *   - 走成功分支：读业务码（response.data.code）
 * 只认其中一条都可能出现"token 过期却只弹提示、不清 token、不跳登录页"的死角。
 */
function handleUnauthorized(msg) {
  window.sessionStorage.removeItem('token')
  window.sessionStorage.removeItem('role')
  window.sessionStorage.removeItem('userInfo')
  ElMessage.error(msg || '登录已失效，请重新登录')

  // 区分前后台，跳回各自的登录页
  const isAdmin = router.currentRoute.value.path.startsWith('/admin')
  router.replace(isAdmin ? '/adminLogin' : '/login')
}

/* ------------------------------------------------------------------
 * 响应拦截器
 * ------------------------------------------------------------------ */
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 登录失效：看业务码
    if (res && res.code === 401) {
      handleUnauthorized(res.msg)
      return Promise.reject(new Error(res.msg || '登录已失效'))
    }

    // 无权限：同样看业务码
    if (res && res.code === 403) {
      ElMessage.error(res.msg || '没有访问权限')
      return Promise.reject(new Error(res.msg || '没有访问权限'))
    }

    // 其他业务异常：code 存在且不为 200
    // 若后端直接返回原始数据（没有 code 字段），也一律放行
    if (res && res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response ? error.response.status : null

    // 后端鉴权拦截器按标准语义返回 HTTP 401/403，这里同样兜住（与成功分支互为补充）
    if (status === 401) {
      handleUnauthorized()
    } else if (status === 403) {
      ElMessage.error('没有访问权限')
    } else if (status === 500) {
      ElMessage.error('服务器内部错误')
    } else {
      ElMessage.error((error.response && error.response.data && error.response.data.msg) || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
