# 电子商务平台前端系统设计与实现

> 广东财经大学《前端开发技术课程设计》课程设计项目
> 课程代码：25110631 ｜ 指导老师：罗东俊 博士
> 完成人：杨伟宏（软件工程 2025 级 1 班）

前后端分离的电商平台系统，包含**前台商城**与**后台管理系统**两大模块，覆盖商品浏览、搜索、
购物车、收藏、下单、订单管理、用户管理、商品管理、数据统计等完整业务闭环。

## 目录结构

| 路径 | 内容 |
|---|---|
| `codes/ebusiness-vue/` | 前端源码（Vue 3 + Element Plus + ECharts） |
| `codes/ebusiness-server/` | 后端源码（Spring Boot + MyBatis-Plus） |
| `codes/sql/ebusiness.sql` | 建库建表 + 演示数据脚本 |
| `codes/README.md` | **完整说明文档**（技术栈 / 数据库 / 接口清单 / 启动步骤） |
| `一键启动.bat` | Windows 双击一键拉起前后端并自动打开网页 |
| `.shots/` | 运行截图（课程报告附录素材） |
| `需求符合性核对报告.md` | 需求点与实现的逐条对照 |
| `易发BUG梳理与验收报告.md` | 缺陷梳理 + 修复与回归验证记录 |
| `给同事的环境准备与启动清单.md` | 换机器部署的环境准备与排错清单 |

## 快速开始

1. 需要先装 **JDK 11（或 17）**、**Node 18+**、**MySQL 8**
2. 导入数据库：
   ```
   mysql -uroot -p --default-character-set=utf8mb4 < codes/sql/ebusiness.sql
   ```
3. 若本机数据库密码不是 `root`，改 `codes/ebusiness-server/src/main/resources/application.properties`
4. 双击根目录的 `一键启动.bat`（会自动打包后端、装依赖、起服务并打开浏览器）

> 详细步骤与常见问题见 [`codes/README.md`](codes/README.md) 与
> [`给同事的环境准备与启动清单.md`](给同事的环境准备与启动清单.md)

## 演示账号

| 角色 | 账号 | 密码 | 入口 |
|---|---|---|---|
| 系统管理员 | `admin` | `123456` | http://localhost:8080/adminLogin |
| 注册用户 | `test@ebusiness.com` | `123456` | http://localhost:8080/login |
| 注册用户 | `xiaoyang@ebusiness.com` | `123456` | http://localhost:8080/login |

**端口**：后端 `8443`（context-path `/eBusiness`）｜ 前端 `8080`。
路由为 history 模式（地址无 `#`），刷新二级路由可正常打开。
