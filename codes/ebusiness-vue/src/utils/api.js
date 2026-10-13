/**
 * 接口统一管理
 * ------------------------------------------------------------------
 * 把后端接口按「模块」集中定义，页面中只引入 api 方法，不直接写 URL。
 * 好处：
 *   1. 接口地址集中维护，后端改路径只改一处
 *   2. 页面代码更干净，体现工程化的分层思想
 *
 * 约定：
 *   - request 的 baseURL = '/api'，由 vue.config.js 代理到
 *     http://localhost:8443/eBusiness
 *   - 因此下面路径均以 /api 之后的片段书写
 *   - 统一响应结构：{ code, msg, data }
 */
import request from '@/utils/request'

/* ==================================================================
 * 公共接口（/api/common/*）
 * ================================================================== */
export const commonApi = {
  /** 获取图形验证码 → data: { uuid, img, code } */
  getCaptcha() {
    return request.get('/common/captcha')
  }
}

/* ==================================================================
 * 后台管理端接口（/api/admin/*）
 * ================================================================== */
export const adminApi = {
  /* ---- 管理员登录 ---- */
  /** 登录 → data: { token, aname, aid, role } */
  login(data) {
    return request.post('/admin/au/login', data)
  },
  /** 当前管理员信息 */
  getAdminInfo() {
    return request.get('/admin/au/info')
  },

  /* ---- 商品类型管理 ---- */
  /** 类型列表（全部，不分页；下拉框 / 前台筛选栏用） → data: [{ id, name, goodsCount }] */
  getTypeList() {
    return request.get('/admin/type/list')
  },
  /** 类型分页列表（后台类型管理表格用） → data: { typeList, total, totalPage, currentPage } */
  getTypePage(params) {
    return request.get('/admin/type/page', { params })
  },
  addType(data) {
    return request.post('/admin/type/add', data)
  },
  updateType(data) {
    return request.post('/admin/type/update', data)
  },
  deleteType(id) {
    return request.delete(`/admin/type/delete/${id}`)
  },

  /* ---- 商品管理 ---- */
  /** 商品列表 → data: { goodsList, total, currentPage, totalPage } */
  getGoodsList(params) {
    return request.get('/admin/goods/list', { params })
  },
  /** 商品详情 */
  getGoodsDetail(id) {
    return request.get(`/admin/goods/detail/${id}`)
  },
  /** 新增商品（FormData，含图片） */
  addGoods(formData) {
    return request.post('/admin/goods/add', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  /** 修改商品（FormData，含图片） */
  updateGoods(formData) {
    return request.post('/admin/goods/update', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  deleteGoods(id) {
    return request.delete(`/admin/goods/delete/${id}`)
  },
  /** 上架/下架 → { id, isoff } */
  changeGoodsStatus(data) {
    return request.post('/admin/goods/status', data)
  },
  /** 批量上架/下架 → { ids: [], isoff } */
  batchChangeGoodsStatus(data) {
    return request.post('/admin/goods/batchStatus', data)
  },

  /* ---- 订单管理 ---- */
  /** 订单列表 → data: { orders, total, currentPage, totalPage } */
  getOrderList(params) {
    return request.get('/admin/order/list', { params })
  },
  /** 订单明细 → data: [ { goodsname, price, shoppingnum, picture, subtotal } ] */
  getOrderDetail(id) {
    return request.get(`/admin/order/detail/${id}`)
  },
  /** 修改订单状态 → { id, status } */
  changeOrderStatus(data) {
    return request.post('/admin/order/status', data)
  },
  deleteOrder(id) {
    return request.delete(`/admin/order/delete/${id}`)
  },

  /* ---- 用户管理 ---- */
  /** 用户列表 → data: { users, total, currentPage, totalPage } */
  getUserList(params) {
    return request.get('/admin/user/list', { params })
  },
  deleteUser(bid) {
    return request.delete(`/admin/user/delete/${bid}`)
  },

  /* ---- 数据统计 ---- */
  /** 首页概览 → data: { goodsCount, userCount, orderCount, totalAmount, pendingOrderCount } */
  getDashboard() {
    return request.get('/admin/statistics/dashboard')
  },
  /** 按类型销售统计 → data: [ { typeName, totalAmount, totalNum } ] */
  getSalesByType() {
    return request.get('/admin/statistics/salesByType')
  },
  /** 按月销售统计 → data: [ { month, totalAmount, totalNum } ]，无成交月份补 0 */
  getSalesByMonth(months = 6) {
    return request.get('/admin/statistics/salesByMonth', { params: { months } })
  },
  /** 热销商品 → data: [ { goodsName, price, sales, totalNum, totalAmount } ] */
  getHotGoods(limit = 10) {
    return request.get('/admin/statistics/hotGoods', { params: { limit } })
  },
  /** 订单状态分布 → data: [ { status, statusName, orderCount } ] */
  getOrderStatusStat() {
    return request.get('/admin/statistics/orderStatus')
  },
  /** 订单趋势 → data: [ { date, orderCount, amount } ] */
  getOrderTrend(days = 7) {
    return request.get('/admin/statistics/orderTrend', { params: { days } })
  }
}

/* ==================================================================
 * 电商前台接口（/api/before/*）
 * ================================================================== */
export const beforeApi = {
  /* ---- 用户 ---- */
  /** 注册 → { bemail, bpwd, bname, ... } */
  register(data) {
    return request.post('/before/register', data)
  },
  /** 登录 → data: { token, bemail, bname, bid, role } */
  login(data) {
    return request.post('/before/login', data)
  },
  /** 当前登录用户资料 */
  getUserInfo() {
    return request.get('/before/info')
  },
  /** 修改资料 → { bname, bphone, baddr } */
  updateUserInfo(data) {
    return request.post('/before/info/update', data)
  },
  /** 修改密码 → { oldPwd, newPwd } */
  updatePassword(data) {
    return request.post('/before/password', data)
  },
  /** 退出登录 */
  logout() {
    return request.post('/before/logout')
  },

  /* ---- 商品 ---- */
  /** 商品列表 → data: { goodsList, total, currentPage, totalPage } */
  getGoodsList(params) {
    return request.get('/before/goods/list', { params })
  },
  /** 商品详情 → data: Goods */
  getGoodsDetail(id) {
    return request.get(`/before/goods/detail/${id}`)
  },
  /** 首页推荐 → data: [ Goods ] */
  getRecommendGoods(limit = 8) {
    return request.get('/before/goods/recommend', { params: { limit } })
  },
  /** 商品类型 → data: [ { id, name, goodsCount } ] */
  getGoodsTypes() {
    return request.get('/before/goods/types')
  },

  /* ---- 购物车 ---- */
  /** 加入购物车 → { goodstableId, shoppingnum } */
  addCart(data) {
    return request.post('/before/cart/add', data)
  },
  /** 购物车列表 → data: { carts, totalAmount, totalNum } */
  getCartList() {
    return request.get('/before/cart/list')
  },
  /** 批量修改数量 → { bcid: [], bshoppingnum: [] } */
  updateCart(data) {
    return request.post('/before/cart/update', data)
  },
  deleteCart(cid) {
    return request.delete(`/before/cart/delete/${cid}`)
  },
  /** 清空购物车 */
  clearCart() {
    return request.post('/before/cart/clear')
  },
  /** 购物车件数 → data: number */
  getCartCount() {
    return request.get('/before/cart/count')
  },

  /* ---- 收藏 ---- */
  /** 收藏/取消收藏（切换） → { goodstableId }，data: { focused } */
  toggleFocus(data) {
    return request.post('/before/focus/toggle', data)
  },
  /** 收藏列表 → data: { focusList, total } */
  getFocusList() {
    return request.get('/before/focus/list')
  },
  /** 是否已收藏 → data: { focused } */
  checkFocus(goodstableId) {
    return request.get('/before/focus/check', { params: { goodstableId } })
  },
  deleteFocus(fid) {
    return request.delete(`/before/focus/delete/${fid}`)
  },

  /* ---- 订单 ---- */
  /** 提交订单 → { cartIds: [] }，data: { orderId, ordernumber, amount } */
  submitOrder(data) {
    return request.post('/before/order/submit', data)
  },
  /** 我的订单 → data: { orders, total, currentPage, totalPage } */
  getOrderList(params) {
    return request.get('/before/order/list', { params })
  },
  /** 订单明细 → data: [ ... ] */
  getOrderDetail(id) {
    return request.get(`/before/order/detail/${id}`)
  },
  /** 取消订单 → { id } */
  cancelOrder(data) {
    return request.post('/before/order/cancel', data)
  }
}

export default { commonApi, adminApi, beforeApi }
