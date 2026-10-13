/**
 * 路由配置
 * ------------------------------------------------------------------
 * 设计要点：
 *   1. 全部页面采用「路由级懒加载」：component: () => import(...)
 *      —— 首屏只加载首页，其余页面按需加载，优化页面加载性能（对应报告 4.4）
 *   2. 路由分两组：
 *        /before/*  电商前台（普通用户 + 游客）
 *        /admin/*   后台管理（仅管理员）
 *   3. meta 字段：
 *        title    页面标题
 *        auth     是否需要登录才能访问
 *        role     'admin' 表示仅管理员可访问
 *
 * 路由守卫：见文件底部 beforeEach
 */
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  /* ---------------------------------------------------------------
   * 根路径重定向到前台首页
   * --------------------------------------------------------------- */
  { path: '/', redirect: '/before/index' },

  /* ---------------------------------------------------------------
   * 电商前台（/before/*）
   * --------------------------------------------------------------- */
  {
    path: '/before',
    redirect: '/before/index',
    children: [
      {
        path: 'index',
        name: 'beforeIndex',
        component: () => import('@/views/before/IndexView.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'goodsDetail/:id',
        name: 'goodsDetail',
        component: () => import('@/views/before/GoodsDetailView.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'register',
        name: 'beforeRegister',
        component: () => import('@/views/before/RegisterView.vue'),
        meta: { title: '用户注册' }
      },
      {
        path: 'myCart',
        name: 'myCart',
        component: () => import('@/views/before/MyCartView.vue'),
        meta: { title: '我的购物车', auth: true }
      },
      {
        path: 'myOrder',
        name: 'myOrder',
        component: () => import('@/views/before/MyOrderView.vue'),
        meta: { title: '我的订单', auth: true }
      },
      {
        path: 'myFocus',
        name: 'myFocus',
        component: () => import('@/views/before/MyFocusView.vue'),
        meta: { title: '我的收藏', auth: true }
      },
      {
        path: 'myselfInfo',
        name: 'myselfInfo',
        component: () => import('@/views/before/MyselfInfoView.vue'),
        meta: { title: '个人信息', auth: true }
      },
      {
        path: 'goOrder/:id',
        name: 'goOrder',
        component: () => import('@/views/before/GoOrderView.vue'),
        meta: { title: '订单确认', auth: true }
      }
    ]
  },

  /* 前台用户登录（独立路径，不走 /before 子路由） */
  {
    path: '/login',
    name: 'beforeLogin',
    component: () => import('@/views/before/LoginView.vue'),
    meta: { title: '用户登录' }
  },

  /* ---------------------------------------------------------------
   * 后台管理（/admin/*）
   * --------------------------------------------------------------- */
  {
    path: '/adminLogin',
    name: 'adminLogin',
    component: () => import('@/views/admin/LoginView.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/admin',
    redirect: '/admin/home',
    children: [
      {
        path: 'home',
        name: 'adminHome',
        component: () => import('@/views/admin/HomeView.vue'),
        meta: { title: '后台主页', auth: true, role: 'admin' }
      },
      {
        path: 'typeManage',
        name: 'typeManage',
        component: () => import('@/views/admin/TypeManageView.vue'),
        meta: { title: '商品类型管理', auth: true, role: 'admin' }
      },
      {
        path: 'goodsManage',
        name: 'goodsManage',
        component: () => import('@/views/admin/GoodsManageView.vue'),
        meta: { title: '商品管理', auth: true, role: 'admin' }
      },
      {
        path: 'orderManage',
        name: 'orderManage',
        component: () => import('@/views/admin/OrderManageView.vue'),
        meta: { title: '订单管理', auth: true, role: 'admin' }
      },
      {
        path: 'salesStatistics',
        name: 'salesStatistics',
        component: () => import('@/views/admin/SalesStatisticsView.vue'),
        meta: { title: '销量统计', auth: true, role: 'admin' }
      },
      {
        path: 'orderStatistics',
        name: 'orderStatistics',
        component: () => import('@/views/admin/OrderStatisticsView.vue'),
        meta: { title: '订单统计', auth: true, role: 'admin' }
      },
      {
        path: 'userManage',
        name: 'userManage',
        component: () => import('@/views/admin/UserManageView.vue'),
        meta: { title: '用户管理', auth: true, role: 'admin' }
      }
    ]
  },

  /* 404 兜底：展示独立错误页，而不是静默重定向（便于用户发现链接错误） */
  {
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '页面不存在', auth: false }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

/* ------------------------------------------------------------------
 * 全局前置守卫：登录与权限校验（前端兜底）
 * ------------------------------------------------------------------ */
router.beforeEach((to, from, next) => {
  // 动态设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 电子商务平台` : '电子商务平台'

  const token = window.sessionStorage.getItem('token')
  const role = window.sessionStorage.getItem('role')

  // 需要登录的页面
  if (to.meta.auth) {
    if (!token) {
      // 未登录：跳转到对应登录页，并记录来源路径
      const loginPath = to.meta.role === 'admin' ? '/adminLogin' : '/login'
      next({ path: loginPath, query: { redirect: to.fullPath } })
      return
    }
    // 需要管理员权限但当前不是管理员
    if (to.meta.role === 'admin' && role !== 'admin') {
      next({ path: '/adminLogin' })
      return
    }
  }
  next()
})

export default router
