# docs 目录说明

本目录用于存放课程设计提交的成果文件。

## 目录用途

按《前端开发技术课程设计》试题纸要求，最终提交材料包括：

| 材料 | 说明 | 负责人 |
|---|---|---|
| `docs/` 成果文件包 | 项目源代码、数据库脚本、启动说明 | 杨伟宏 |
| `report.doc` 课程设计报告 | 需求分析、系统设计、实现、测试、总结 | 他人 |
| `答辩.ppt` | 答辩演示文稿 | 他人 |

> 说明：课程设计报告与答辩 PPT 由同组其他同学负责，不在本项目源码仓库范围内。

## 当前项目可交付内容

源代码已完成，分布如下：

| 位置 | 内容 | 文件数 |
|---|---|---|
| `../ebusiness-vue/` | 前端工程（Vue 3 完整源码，后台 8 页 + 前台 9 页 + 3 公共组件） | 32 |
| `../ebusiness-server/` | 后端工程（Spring Boot 完整源码，含 2 个 MyBatis XML） | 56 |
| `../sql/ebusiness.sql` | 数据库建库建表脚本（8 表 + 演示数据） | 1 |
| `../README.md` | 项目说明与启动指南 | 1 |

## 源码组织要点（供报告"系统设计"章节引用）

### 前端工程化组织

```
src/
├── assets/         全局样式与主题变量
├── components/     可复用公共组件（3 个）
├── router/         路由表与全局守卫
├── stores/          Pinia 状态仓库
├── utils/          基础设施（Axios 封装、接口集中管理）
└── views/
    ├── admin/      后台管理页面（8 个）
    └── before/     前台商城页面（9 个）
```

设计要点：
- **utils/api.js 接口集中管理**：页面不直接写 URL，改接口只改一处
- **utils/request.js 统一请求**：拦截器自动注入 token、统一处理错误
- **路由级懒加载**：`() => import()` 实现代码分割，首屏加载快

### 后端分层组织

```
com.ebusiness/
├── common/
│   ├── config/     各类配置（MVC/MyBatis-Plus/Redis/CORS/全局异常）
│   ├── security/   JWT 工具、鉴权拦截器、登录上下文
│   └── util/       工具类（响应封装、MD5、验证码、上传）
├── controller/
│   ├── admin/      后台接口（6 个控制器）
│   └── before/     前台接口（5 个控制器）
├── entity/         实体类（8 个，与数据库表一一对应）
├── mapper/         Mapper 接口（8 个，继承 BaseMapper）
└── service/
    ├── impl/       业务实现（7 个）
    └── *.java      业务接口（7 个）
```

设计要点：
- **Controller 按端分包**：`admin` 与 `before` 职责清晰，便于权限管控
- **三层架构**：Controller → Service → Mapper，各层职责单一
- **统一响应封装**：`ResponseResult` 全站统一返回结构
- **全局异常处理**：`@RestControllerAdvice` 兜底，避免异常直接抛给前端

## 数据库设计

8 张表，含完整外键约束、唯一索引与初始数据。

详见 `../sql/ebusiness.sql`，表结构说明见 `../README.md` 第七节。

## 核心功能实现要点（供报告"三、核心功能实现"章节引用）

| 功能模块 | 实现位置 | 技术要点 |
|---|---|---|
| 登录鉴权 | `utils/request.js` + `common/security/` | JWT 签发校验；前端路由守卫 + Axios 拦截器自动注入 token；后端 `AuthInterceptor` 兜底 401/403 |
| 商品多条件筛选 | `GoodsServiceImpl#getGoodsByPage` | 关键词模糊 + 类型精确，MyBatis-Plus 分页插件 |
| **商品规格选择** | `GoodsDetailView.vue` + `goodstable.specs` | 规格定义 / 购物车快照 / 订单快照**三段式建模**，见 `../README.md` |
| 库存校验下单事务 | `OrderBaseServiceImpl#submitOrder` | `@Transactional` 保证"校验库存→建单→写明细→扣库存→清购物车"五步原子 |
| 订单状态管理 | `OrderManageView.vue` | 5 种状态流转，列表分页 + 展开行懒加载明细 |
| **批量上下架** | `GoodsController#batchStatus` | 多选批量提交，上架前过滤零库存商品 |
| **类型分页查询** | `GoodsTypeController#page` | 分页 + 关键词搜索，行内统计关联商品数 |
| 数据统计可视化 | `SalesStatisticsView.vue` / `OrderStatisticsView.vue` | ECharts 柱状 / 折线 / 饼图；**有效订单口径统一为 `status IN (2,3,4)`** |
| **按月销量统计** | `GoodsMapper.getSalesByMonth` | 无成交月份补 0 保证 X 轴连续；双 Y 轴（金额 + 件数） |

### 统计口径说明（报告可直接引用）

系统将所有销售类统计的有效订单统一为 **`status IN (2,3,4)`**（已付款 / 已发货 / 已完成），
排除待付款与已取消订单。SQL 中通过 `SUM(CASE WHEN ob.status IN (2,3,4) THEN ... ELSE 0 END)`
在**聚合层**过滤，而非写在 `LEFT JOIN ... ON` 上——后者只让被连接表变 NULL，
明细行仍会参与求和，导致无效订单被计入。

当前演示数据下三处统计互相对账一致：**类目销售合计 = 月度成交合计 = 首页概览销售总额 = 163,212.00 元**。
