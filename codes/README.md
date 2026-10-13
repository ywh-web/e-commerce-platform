# 电子商务平台前端系统设计与实现

> 广东财经大学《前端开发技术课程设计》课程设计项目
> 课程代码：25110631 ｜ 指导老师：罗东俊 博士
> 完成人：杨伟宏（软件工程 2025 级 1 班）

---

## 一、项目简介

本项目是一个前后端分离的电商平台系统，包含**前台商城**与**后台管理系统**两大模块，覆盖
商品浏览、搜索、购物车、收藏、下单、订单管理、用户管理、商品管理、数据统计等完整业务闭环。

- **前台商城**（游客 / 注册用户）：首页推荐、商品搜索、类型筛选、商品详情、加入购物车、收藏、下单、个人中心（资料 / 订单 / 收藏）
- **后台管理**（系统管理员）：商品类型管理、商品管理、订单管理、用户管理、销售统计、订单统计

## 二、技术栈

### 前端 `ebusiness-vue`

| 技术 | 版本 | 用途 |
|---|---|---|
| Vue | 3.5 | 核心框架（组合式 API） |
| Vue Router | 4.4 | 路由管理 + 路由级懒加载 |
| Pinia | 2.2 | 状态管理（登录态） |
| Axios | 1.7 | HTTP 请求 + 拦截器统一封装 |
| Element Plus | 2.8 | UI 组件库 |
| @element-plus/icons-vue | 2.3 | 图标库 |
| ECharts | 5.5 | 后台统计图表 |
| Vue CLI | 5.0 | 工程脚手架 |

### 后端 `ebusiness-server`

| 技术 | 版本 | 用途 |
|---|---|---|
| Spring Boot | 2.7.18 | 应用框架 |
| MyBatis-Plus | 3.5.3.2 | ORM + 分页插件 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | — | 验证码、登录态缓存 |
| Hutool | 5.8.22 | JWT / MD5 / 文件工具 |
| Lombok | — | 减少样板代码 |

## 三、目录结构

> 用 VS Code 直接打开本目录（`codes/`）即可，前后端两个工程都在里面。

```
codes/
├── ebusiness-vue/                  # 前端工程（Vue 3 + Vue CLI 标准结构）
│   ├── public/
│   ├── src/
│   │   ├── assets/                 # 全局样式（global.css，CSS 变量主题）
│   │   ├── components/             # 公共组件
│   │   │   ├── HeaderView.vue      #   前台顶部导航（含安全退出）
│   │   │   ├── HeaderCom.vue       #   后台顶栏（含安全退出）
│   │   │   └── SidebarCom.vue      #   后台侧边栏
│   │   ├── router/index.js         # 路由表（17 条）+ 全局前置守卫
│   │   ├── stores/user.js          # Pinia 登录态仓库
│   │   ├── utils/
│   │   │   ├── request.js          # Axios 封装（拦截器注入 token / 处理 401）
│   │   │   ├── format.js           # 格式化工具（金额 / 日期 / 图片路径）
│   │   │   └── api.js              # 接口集中管理（adminApi / beforeApi）
│   │   ├── views/
│   │   │   ├── admin/              # 后台 8 个页面
│   │   │   └── before/             # 前台 9 个页面
│   │   ├── App.vue
│   │   └── main.js
│   ├── vue.config.js               # 开发代理：/api → http://localhost:8443/eBusiness
│   └── package.json
│
├── ebusiness-server/               # 后端工程（Spring Boot + MyBatis-Plus）
│   ├── src/main/java/com/ebusiness/
│   │   ├── EbusinessApplication.java   # 启动类
│   │   ├── common/                     # 公共基础设施
│   │   │   ├── config/                 #   各类配置
│   │   │   ├── security/               #   JWT / 拦截器 / 登录上下文
│   │   │   └── util/                   #   工具类
│   │   ├── controller/
│   │   │   ├── admin/                  # 后台接口
│   │   │   └── before/                 # 前台接口
│   │   ├── entity/                     # 8 个实体
│   │   ├── mapper/                     # 8 个 Mapper
│   │   └── service/                    # 业务接口 + impl
│   ├── src/main/resources/
│   │   ├── application.properties      # 主配置
│   │   ├── mapper/                     # MyBatis XML 自定义 SQL
│   │   └── static/upload/              # 商品图片（14 张）
│   └── pom.xml
│
├── sql/ebusiness.sql               # 建库建表脚本（8 张表 + 初始数据）
├── docs/README.md                  # 成果文件说明与源码组织要点
└── README.md                       # 本文件
```

## 四、快速启动

> 以下命令都以 **`codes/` 目录为当前目录**（即用 VS Code 打开的那个目录）。

### 1. 环境准备

- JDK 8 / 11
- Maven 3.6+
- Node.js 16+ / npm
- MySQL 8.0
- Redis 5.0+（**可选**：仅图形验证码接口依赖，未安装时后端仍可正常启动，
  登录、下单等主流程不受影响）

### 2. 初始化数据库

```bash
mysql -u root -p < sql/ebusiness.sql
```

脚本会创建 `ebusiness` 库、8 张表，并写入初始数据：1 个管理员、2 个测试用户、
5 个商品类型、14 件商品（其中 9 件带多规格）、购物车与收藏若干，以及 **31 笔订单 / 33 条订单明细**
（覆盖待付款、已付款、已发货、已完成、已取消全部状态，下单时间横跨**最近 6 个自然月**），
用于演示订单与统计报表。

> 订单的下单时间用 `NOW() - INTERVAL n DAY` / `n MONTH` 相对生成，所以无论什么时候执行脚本，
> 统计页的「近 7 天订单趋势」与「近 6 个月销量」都能取到数据，不会因为日期过期变成空图。
> 脚本末尾用明细汇总回填了订单总金额，可用它自带的校验语句确认主表与明细一致。

### 3. 修改后端配置

编辑 `ebusiness-server/src/main/resources/application.properties`，把数据库账号密码改成你本机的：

```properties
spring.datasource.username=root
spring.datasource.password=你的密码
spring.redis.password=          # Redis 无密码则留空
```

### 4. 启动 Redis（可选）

```bash
redis-server
```

不启动也可以，只影响图形验证码接口。

### 5. 启动后端

```bash
cd ebusiness-server
mvn spring-boot:run
```

启动成功后接口地址为 `http://localhost:8443/eBusiness`。

> 如果本机环境变量里存在 `SERVER__PORT`，它会覆盖 `server.port`，
> 此时改用打包运行并显式指定端口：
> `java -jar target/ebusiness-server-1.0.0.jar --server.port=8443`

### 6. 启动前端

```bash
cd ebusiness-vue
npm install              # 首次执行
npm run serve
```

访问 `http://localhost:8080`。

## 五、演示账号

| 角色 | 账号 | 密码 | 入口 |
|---|---|---|---|
| 系统管理员 | `admin` | `123456` | http://localhost:8080/adminLogin |
| 注册用户 | `test@ebusiness.com` | `123456` | http://localhost:8080/login |
| 注册用户 | `xiaoyang@ebusiness.com` | `123456` | 同上 |

> 路由使用 **history 模式**，地址里**没有 `#`**；直接刷新二级路由也能正常打开（开发服务器已配置 history 回退）。

## 六、核心设计说明

### 6.1 认证方案

- **存储**：token 存 `sessionStorage`（关闭标签页即失效，比 localStorage 更安全）
- **签发**：登录成功后由后端 `JwtTokenUtil` 签发 JWT，载荷含 `account` / `role` / `uid`
- **注入**：前端 Axios 请求拦截器自动加 `Authorization: Bearer <token>`
- **前端兜底**：路由全局前置守卫校验 `meta.auth` 与 `meta.role`，未登录跳转登录页
- **后端防线**：`AuthInterceptor` 校验 token 与角色，非法返回 401；**这才是真正的安全边界**
- **失效处理**：响应拦截器捕获 401 后清除本地 token 并踢回登录页

### 6.2 渲染方案

- Vue 3 SPA 客户端渲染
- **路由级懒加载**：所有页面 `() => import()`，首屏体积小
- **三类状态兜底**：加载中 `v-loading` / `el-skeleton`，空状态 `el-empty`，错误 `ElMessage`
- `el-container` 布局：后台为侧边栏 + 顶栏 + 内容区；前台为顶部导航 + 内容区（1200px 居中）
- 不做 SSR、不做微前端（课程规模不需要）

### 6.3 下单事务流程（后端核心）

`OrderBaseServiceImpl#submitOrder` 使用 `@Transactional` 保证以下五步原子性：

1. 取出选中的购物车记录，逐条校验商品存在性、上架状态、库存充足性
2. 生成订单基础记录（订单号由时间戳生成，状态为"待付款"）
3. 批量写入订单详情（商品名称与单价做**快照**，后续改价不影响历史订单）
4. 扣减商品库存、累加销量（库存清零时自动下架）
5. 清空已下单的购物车记录

任意一步抛异常，整体回滚。

### 6.4 接口分组

| 分组 | 前缀 | 说明 |
|---|---|---|
| 公共 | `/api/common` | 图形验证码 |
| 后台 | `/api/admin/au` | 管理员登录 |
| 后台 | `/api/admin/type` | 商品类型管理 |
| 后台 | `/api/admin/goods` | 商品管理 |
| 后台 | `/api/admin/order` | 订单管理 |
| 后台 | `/api/admin/user` | 用户管理 |
| 后台 | `/api/admin/statistics` | 数据统计 |
| 前台 | `/api/before/*` | 注册登录、商品浏览、购物车、收藏、订单 |

## 七、数据库表结构

| 表名 | 说明 | 关键字段 |
|---|---|---|
| `ausertable` | 管理员 | aname（账号，唯一）、apwd |
| `busertable` | 前台用户 | bemail（邮箱，唯一）、bpwd、bname、bphone、baddr |
| `goodstype` | 商品类型 | name |
| `goodstable` | 商品 | name、picture、price、stock、goodstype_id、isoff、sales、**specs** |
| `carttable` | 购物车 | busertable_id、goodstable_id、shoppingnum、**spec**（联合唯一：用户+商品+规格） |
| `focustable` | 收藏 | busertable_id、goodstable_id（联合唯一） |
| `orderbasestable` | 订单主表 | ordernumber（唯一）、amount、status、datetime |
| `orderdetail` | 订单详情 | orderbase_id、goodstable_id、goodsname、price、shoppingnum、**spec** |

### 商品规格的三段式设计

规格信息在三张表里各司其职，避免"定义"和"选择结果"混在一起：

| 表 | 字段 | 存什么 | 示例 |
|---|---|---|---|
| `goodstable` | `specs` | **规格定义**（有哪些维度、哪些选项） | `颜色:雅川青\|白沙银\|雅丹黑;版本:12+256G\|12+512G` |
| `carttable` | `spec` | **用户选中的规格快照** | `白沙银 / 12+512G` |
| `orderdetail` | `spec` | **下单时的规格快照** | `白沙银 / 12+512G` |

- 多维度用 ` / ` 连接；无规格商品统一存**空串**（不是 `NULL`），这样"用户+商品+规格"才能用等值条件精确去重
- `specs` 为空表示该商品无规格，详情页不渲染规格选择器
- 购物车表建了 `UNIQUE KEY uk_cart_user_goods_spec (busertable_id, goodstable_id, spec)`，
  同一商品的不同规格会各占一行，数量分别累加

---

## 八、特色功能实现要点

| 功能 | 界面 | 关键实现 |
|---|---|---|
| 首页商品轮播 | 前台首页 | `el-carousel` 驱动推荐商品，骨架屏兜底；图片缩放用内部 `scale`（轮播容器 `overflow:hidden` 会裁掉整体位移） |
| 商品规格选择 | 商品详情 | 解析 `specs` 生成多维度选项，默认预选每维第一项；未选规格时后端拒绝加购 |
| 批量上下架 | 后台商品管理 | 表格多选 → `POST /batchStatus`；**上架前校验库存**，库存为 0 的商品会被跳过并回执提示 |
| 类型分页 + 搜索 | 后台类型管理 | `GET /type/page?currentPage=&keywords=`，删除当前页最后一条自动回退一页 |
| 按月销量统计 | 后台销售统计 | 柱状看金额（左轴）+ 折线看件数（右轴）；**无成交月份补 0** 保证 X 轴连续；离散件数关闭 `smooth` 并设 `minInterval: 1` |

### 统计口径（改统计代码务必遵守）

**有效订单 = `status IN (2,3,4)`**（已付款 / 已发货 / 已完成），排除 `status = 1`（待付款）
与 `status = 5`（已取消）。

- 状态过滤**不能写在 `LEFT JOIN` 的 `ON` 上**——写在 ON 上只让被连接表变 NULL，明细行照样参与 `SUM`。
  必须写进聚合：`SUM(CASE WHEN ob.status IN (2,3,4) THEN od.price*od.shoppingnum ELSE 0 END)`
- 自检方法：**类目销售金额合计**、**月度成交金额合计** 必须都等于 **首页概览的销售总额**，不等就是口径错了

---

## 九、已知限制与注意事项

### 9.1 上传目录指向源码目录

后端配置 `ebusiness.upload.path=${user.dir}/src/main/resources/static/upload/`，
上传的商品图片会落到 **源码目录** 下，而不是打包产物里。由此带来两点：

- 用 `java -jar` 运行时，该目录解析为 **jar 所在目录** 下的 `src/main/resources/static/upload/`。
  如果这个路径不存在，上传会失败（演示时目录里已有预置的占位图，所以显示正常）。
- **重新执行 `mvn package` 不会覆盖已上传的图**，但如果删除 `src/main/resources/static/upload/`
  重打包，运行期新上传的图片就会丢。

> 课程演示场景下无影响。若要长期运行，建议把 `ebusiness.upload.path` 改成 jar 同级的外部目录，
> 例如 `./upload/`，并在 `WebMvcConfig` 中把该目录注册为静态资源映射。

### 9.2 图形验证码需要 Redis

后端保留了基于 Redis 的验证码接口，但本机未安装 Redis 时该接口不可用。
前端注册 / 登录流程**未依赖验证码**，因此不启动 Redis 也能完整演示。

### 9.3 未做移动端适配

本项目按要求**只针对桌面端**（约 1024px 及以上）设计，通过
`1440 / 1280 / 1024` 三档媒体查询做桌面分辨率适配，
**不包含手机端布局**。

---

*本 README 仅说明项目结构与启动方式，详细设计文档见 `docs/` 目录。*
