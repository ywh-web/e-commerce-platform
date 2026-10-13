/**
 * 应用入口
 * ------------------------------------------------------------------
 * 依次完成：
 *   1. 创建 Vue 应用实例
 *   2. 注册 Element Plus 组件库（含中文语言包）
 *   3. 注册 Element Plus 图标组件（全局可用）
 *   4. 注册 Pinia 状态管理
 *   5. 挂载路由
 */
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// Element Plus
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'

// Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// Pinia
import { createPinia } from 'pinia'

// 全局基础样式
import '@/assets/global.css'

const app = createApp(App)

// 注册全部 Element Plus 图标为全局组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, { locale: zhCn })   // 中文语言包
app.use(createPinia())                    // Pinia
app.use(router)                           // 路由

app.mount('#app')
