/**
 * 通用格式化工具
 * ------------------------------------------------------------------
 * 页面中反复用到的展示逻辑集中在这里，避免各处重复写。
 */

/** 默认商品图（无图时兜底，内联 SVG，不依赖任何文件） */
const DEFAULT_IMG =
  'data:image/svg+xml;charset=utf-8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 400 400">
       <rect width="400" height="400" fill="#f2f3f5"/>
       <circle cx="200" cy="170" r="52" fill="#e5e6eb"/>
       <rect x="120" y="250" width="160" height="14" rx="7" fill="#e5e6eb"/>
       <text x="200" y="336" font-size="18" fill="#c9cdd4" text-anchor="middle"
             font-family="Microsoft YaHei, sans-serif">暂无图片</text>
     </svg>`
  )

/**
 * 把数据库里的图片文件名拼成可访问的 URL
 *
 * 后端把上传目录映射到 /upload/**
 * 开发环境由 vue.config.js 代理到 http://localhost:8443/eBusiness/upload
 *
 * @param {string} picture 数据库 picture 字段（文件名或完整 URL）
 * @returns {string} 可直接给 img 的 src
 */
export function goodsImg(picture) {
  if (!picture) return DEFAULT_IMG
  // 已经是完整地址或 data URI，直接用
  if (/^(https?:)?\/\//.test(picture) || picture.startsWith('data:')) {
    return picture
  }
  return '/upload/' + picture
}

/**
 * 金额格式化：1299 → 1299.00
 *
 * @param {number|string} value
 * @param {number} digits 小数位，默认 2
 */
export function money(value, digits = 2) {
  const num = Number(value)
  if (Number.isNaN(num)) return '0.00'
  return num.toFixed(digits)
}

/**
 * 金额千分位：12345.6 → 12,345.60
 */
export function moneyThousands(value, digits = 2) {
  const num = Number(value)
  if (Number.isNaN(num)) return '0.00'
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })
}

/**
 * 订单状态码 → 文案与标签颜色
 * 1待付款 2已付款 3已发货 4已完成 5已取消
 */
const ORDER_STATUS = {
  1: { text: '待付款', type: 'warning' },
  2: { text: '已付款', type: 'primary' },
  3: { text: '已发货', type: 'info' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已取消', type: 'info' }
}

export function orderStatusText(status) {
  return (ORDER_STATUS[status] || { text: '未知' }).text
}

export function orderStatusType(status) {
  return (ORDER_STATUS[status] || { type: 'info' }).type
}

/**
 * 时间格式化：2025-10-09T10:00:01 → 2025-10-09 10:00
 */
export function datetime(value, withSecond = false) {
  if (!value) return '-'
  const s = String(value).replace('T', ' ')
  return withSecond ? s.slice(0, 19) : s.slice(0, 16)
}
