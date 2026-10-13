/**
 * 表单校验规则与校验函数（集中管理）
 *
 * 背景：项目里登录（前后台）、注册、个人信息、商品/类型表单共 6 处各自写了 `rules`，
 *      其中邮箱、密码、昵称等规则完全重复。集中到这里后有两个好处：
 *      1. 复用——同一条规则只维护一份；
 *      2. 一致——"密码最少几位""手机号格式"这类口径改动时，不会漏改某个页面。
 *
 * 用法：
 *   import { required, emailRules, passwordRules } from '@/utils/validators'
 *   const rules = {
 *     bemail: emailRules(),
 *     bpwd: passwordRules()
 *   }
 *
 * @author 杨伟宏
 */

/* ------------------------------------------------------------------ *
 * 规则工厂
 * ------------------------------------------------------------------ */

/**
 * 必填规则
 * @param {string} message 提示文案
 * @param {'blur'|'change'} trigger 触发时机（下拉框等非输入类用 change）
 */
export const required = (message, trigger = 'blur') => ({
  required: true,
  message,
  trigger
})

/**
 * 邮箱规则：必填 + 格式校验
 * @param {string} message 为空时的提示文案
 */
export const emailRules = (message = '请输入邮箱') => [
  required(message),
  { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
]

/**
 * 密码规则：必填 + 最短长度
 * 后台管理员密码统一 123456，与前台的 6 位口径保持一致
 * @param {string} message 为空时的提示文案
 * @param {number} min 最短长度
 */
export const passwordRules = (message = '请输入密码', min = 6) => [
  required(message),
  { min, message: `密码长度不少于 ${min} 位`, trigger: 'blur' }
]

/**
 * 手机号规则：填了就必须合法（默认非必填，与"收货电话可为空"的业务一致）
 * @param {boolean} isRequired 是否强制必填
 */
export const phoneRules = (isRequired = false) => {
  const list = [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
  if (isRequired) list.unshift(required('请输入手机号'))
  return list
}

/**
 * 确认密码规则：必填 + 与上文密码比对
 * @param {() => string} getPassword 取原密码的函数（延迟取值，避免规则定义时密码还没填）
 * @param {string} message 为空时的提示文案
 */
export const confirmPasswordRules = (getPassword, message = '请再次输入密码') => [
  required(message),
  {
    validator: (rule, value, callback) => {
      if (value !== getPassword()) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
]

/* ------------------------------------------------------------------ *
 * 独立校验函数（非 el-form 场景直接用，如提交订单前的兜底校验）
 * ------------------------------------------------------------------ */

/** 是否为空（含纯空白） */
export const isBlank = (value) => !String(value == null ? '' : value).trim()

/** 是否为合法手机号 */
export const isPhone = (value) => /^1[3-9]\d{9}$/.test(String(value || '').trim())

/** 是否为合法邮箱 */
export const isEmail = (value) => /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/.test(String(value || '').trim())
