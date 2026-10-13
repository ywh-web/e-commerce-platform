-- =====================================================================
--  电子商务平台 数据库建库建表脚本
--  工程：ebusiness-server
--  数据库：ebusiness（MySQL 8.0）
--  作者：杨伟宏
--
--  使用说明：
--    1. 用 Navicat / DataGrip / MySQL 命令行执行本脚本
--    2. 脚本会自动删除旧的 ebusiness 库并重建（首次执行时无影响）
--    3. 执行完成后库中有 8 张表 + 初始演示数据
-- =====================================================================

-- ---------------------------------------------------------------
-- 0. 建库（utf8mb4 支持 emoji，商品名称可能出现特殊字符）
-- ---------------------------------------------------------------
DROP DATABASE IF EXISTS `ebusiness`;
CREATE DATABASE `ebusiness`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;
USE `ebusiness`;


-- ---------------------------------------------------------------
-- 1. ausertable —— 管理员表（后台登录用）
--    aname：管理员账号（业务主键，登录凭据）
--    apwd ：管理员密码（MD5 加密后存储，不明文）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `ausertable`;
CREATE TABLE `ausertable`
(
    `aid`      INT(11)     NOT NULL AUTO_INCREMENT COMMENT '管理员主键，自增',
    `aname`    VARCHAR(50) NOT NULL COMMENT '管理员账号（登录用）',
    `apwd`     VARCHAR(50) NOT NULL COMMENT '管理员密码（MD5 加密）',
    PRIMARY KEY (`aid`),
    UNIQUE KEY `uk_aname` (`aname`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='管理员表';


-- ---------------------------------------------------------------
-- 2. busertable —— 前台注册用户表
--    bemail：注册邮箱（业务主键，登录凭据）
--    bpwd  ：密码（MD5 加密）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `busertable`;
CREATE TABLE `busertable`
(
    `bid`    INT(11)      NOT NULL AUTO_INCREMENT COMMENT '用户主键，自增',
    `bemail` VARCHAR(50)  NOT NULL COMMENT '注册邮箱（登录用，唯一）',
    `bpwd`   VARCHAR(50)  NOT NULL COMMENT '登录密码（MD5 加密）',
    `bname`  VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `bphone` VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `baddr`  VARCHAR(200) DEFAULT NULL COMMENT '收货地址',
    PRIMARY KEY (`bid`),
    UNIQUE KEY `uk_bemail` (`bemail`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='前台注册用户表';


-- ---------------------------------------------------------------
-- 3. goodstype —— 商品类型表
--    后台"商品类型管理"页面维护此表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `goodstype`;
CREATE TABLE `goodstype`
(
    `id`   INT(11)     NOT NULL AUTO_INCREMENT COMMENT '类型主键，自增',
    `name` VARCHAR(50) NOT NULL COMMENT '类型名称（如：智能手机、笔记本电脑）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品类型表';


-- ---------------------------------------------------------------
-- 4. goodstable —— 商品表
--    goodstype_id 外键关联 goodstype.id
--    stock：库存；sales：累计销量（下单成功后累加）
--    isoff：0-上架 1-下架（下架商品前台不可见）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `goodstable`;
CREATE TABLE `goodstable`
(
    `id`           INT(11)        NOT NULL AUTO_INCREMENT COMMENT '商品主键，自增',
    `name`         VARCHAR(100)   NOT NULL COMMENT '商品名称',
    `picture`      VARCHAR(200)   DEFAULT NULL COMMENT '商品图片文件名',
    `price`        DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '商品单价（元）',
    `stock`        INT(11)        NOT NULL DEFAULT 0 COMMENT '库存数量',
    `goodstype_id` INT(11)        NOT NULL COMMENT '所属类型ID（外键 → goodstype.id）',
    `isoff`        INT(1)         NOT NULL DEFAULT 0 COMMENT '是否下架：0-上架 1-下架',
    `sales`        INT(11)        NOT NULL DEFAULT 0 COMMENT '累计销量',
    `introduce`    VARCHAR(500)   DEFAULT NULL COMMENT '商品简介/详情描述',
    `specs`        VARCHAR(255)   NOT NULL DEFAULT '' COMMENT '规格定义，格式 规格名:选项1|选项2;规格名:选项1|选项2；为空表示无规格',
    PRIMARY KEY (`id`),
    KEY `idx_goodstype_id` (`goodstype_id`),
    KEY `idx_isoff` (`isoff`),
    CONSTRAINT `fk_goods_type` FOREIGN KEY (`goodstype_id`) REFERENCES `goodstype` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品表';


-- ---------------------------------------------------------------
-- 5. carttable —— 购物车表
--    同一用户对同一商品只保留一条记录，数量累加（Service 层保证）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `carttable`;
CREATE TABLE `carttable`
(
    `cid`            INT(11) NOT NULL AUTO_INCREMENT COMMENT '购物车记录主键，自增',
    `busertable_id`  INT(11) NOT NULL COMMENT '所属用户ID（外键 → busertable.bid）',
    `goodstable_id`  INT(11) NOT NULL COMMENT '商品ID（外键 → goodstable.id）',
    `shoppingnum`    INT(11) NOT NULL DEFAULT 1 COMMENT '购买数量',
    `spec`           VARCHAR(100) NOT NULL DEFAULT '' COMMENT '选中的规格，如「幻夜黑 / 12+256G」；无规格为空串',
    PRIMARY KEY (`cid`),
    KEY `idx_cart_user` (`busertable_id`),
    KEY `idx_cart_goods` (`goodstable_id`),
    -- 同一用户 + 同一商品 + 同一规格 只保留一条记录，加购时在 Service 层累加数量
    UNIQUE KEY `uk_cart_user_goods_spec` (`busertable_id`, `goodstable_id`, `spec`),
    CONSTRAINT `fk_cart_user` FOREIGN KEY (`busertable_id`) REFERENCES `busertable` (`bid`),
    CONSTRAINT `fk_cart_goods` FOREIGN KEY (`goodstable_id`) REFERENCES `goodstable` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='购物车表';


-- ---------------------------------------------------------------
-- 6. focustable —— 商品收藏表
--    同一用户对同一商品只能收藏一次
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `focustable`;
CREATE TABLE `focustable`
(
    `fid`            INT(11) NOT NULL AUTO_INCREMENT COMMENT '收藏记录主键，自增',
    `busertable_id`  INT(11) NOT NULL COMMENT '所属用户ID（外键 → busertable.bid）',
    `goodstable_id`  INT(11) NOT NULL COMMENT '商品ID（外键 → goodstable.id）',
    PRIMARY KEY (`fid`),
    UNIQUE KEY `uk_focus_user_goods` (`busertable_id`, `goodstable_id`),
    CONSTRAINT `fk_focus_user` FOREIGN KEY (`busertable_id`) REFERENCES `busertable` (`bid`),
    CONSTRAINT `fk_focus_goods` FOREIGN KEY (`goodstable_id`) REFERENCES `goodstable` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品收藏表';


-- ---------------------------------------------------------------
-- 7. orderbasestable —— 订单基础表（订单主表）
--    ordernumber：订单号（时间戳生成，唯一）
--    status：订单状态 1-待付款 2-已付款 3-已发货 4-已完成 5-已取消
--    amount：订单总金额（下单时由 orderdetail 汇总写入）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `orderbasestable`;
CREATE TABLE `orderbasestable`
(
    `id`            INT(11)        NOT NULL AUTO_INCREMENT COMMENT '订单主键，自增',
    `busertable_id` INT(11)        NOT NULL COMMENT '下单用户ID（外键 → busertable.bid）',
    `ordernumber`   VARCHAR(50)    NOT NULL COMMENT '订单号（唯一）',
    `amount`        DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额（元）',
    `status`        INT(1)         NOT NULL DEFAULT 1 COMMENT '订单状态：1待付款 2已付款 3已发货 4已完成 5已取消',
    `datetime`      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ordernumber` (`ordernumber`),
    KEY `idx_order_user` (`busertable_id`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`busertable_id`) REFERENCES `busertable` (`bid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单基础表';


-- ---------------------------------------------------------------
-- 8. orderdetail —— 订单详情表
--    一个订单对应多条明细（下几件商品就有几条）
--    冗余保存商品名称与单价：避免商品改价后历史订单金额变动
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `orderdetail`;
CREATE TABLE `orderdetail`
(
    `id`             INT(11)        NOT NULL AUTO_INCREMENT COMMENT '明细主键，自增',
    `orderbase_id`   INT(11)        NOT NULL COMMENT '所属订单ID（外键 → orderbasestable.id）',
    `goodstable_id`  INT(11)        NOT NULL COMMENT '商品ID（外键 → goodstable.id）',
    `goodsname`      VARCHAR(100)   NOT NULL COMMENT '商品名称快照（下单时冗余保存）',
    `price`          DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '成交单价快照（元）',
    `shoppingnum`    INT(11)        NOT NULL DEFAULT 1 COMMENT '购买数量',
    `spec`           VARCHAR(100)   NOT NULL DEFAULT '' COMMENT '规格快照，如「幻夜黑 / 12+256G」；无规格为空串',
    PRIMARY KEY (`id`),
    KEY `idx_detail_order` (`orderbase_id`),
    KEY `idx_detail_goods` (`goodstable_id`),
    CONSTRAINT `fk_detail_order` FOREIGN KEY (`orderbase_id`) REFERENCES `orderbasestable` (`id`),
    CONSTRAINT `fk_detail_goods` FOREIGN KEY (`goodstable_id`) REFERENCES `goodstable` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单详情表';


-- =====================================================================
--  初始化演示数据
--  说明：密码统一用 MD5("123456") = e10adc3949ba59abbe56e057f20f883e
--        （与 MD5Util.md5() 小写输出保持一致）
-- =====================================================================

-- ---------- 管理员：admin / 123456 ----------
INSERT INTO `ausertable` (`aname`, `apwd`)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e');

-- ---------- 前台测试用户 ----------
INSERT INTO `busertable` (`bemail`, `bpwd`, `bname`, `bphone`, `baddr`)
VALUES ('test@ebusiness.com', 'e10adc3949ba59abbe56e057f20f883e', '测试用户', '13800138000', '广东省广州市海珠区广东财经大学'),
       ('xiaoyang@ebusiness.com', 'e10adc3949ba59abbe56e057f20f883e', '小样', '13900139000', '广东省深圳市南山区科技园');

-- ---------- 商品类型 ----------
INSERT INTO `goodstype` (`id`, `name`)
VALUES (1, '智能手机'),
       (2, '笔记本电脑'),
       (3, '平板电脑'),
       (4, '智能穿戴'),
       (5, '影音娱乐');

-- ---------- 商品（isoff=0 表示上架） ----------
INSERT INTO `goodstable` (`name`, `picture`, `price`, `stock`, `goodstype_id`, `isoff`, `sales`, `introduce`, `specs`)
VALUES ('华为 Mate 60 Pro', 'mate60pro.jpg', 6999.00, 120, 1, 0, 328, '麒麟芯片回归之作，卫星通话，超可靠玄武架构。', '颜色:雅川青|白沙银|雅丹黑;版本:12+256G|12+512G'),
       ('小米 14 Ultra', 'mi14ultra.jpg', 6499.00, 85, 1, 0, 216, '徕卡光学全焦段四摄，骁龙 8 Gen3 旗舰性能。', '颜色:黑色|白色;版本:12+256G|16+512G'),
       ('iPhone 15 Pro', 'iphone15pro.jpg', 7999.00, 60, 1, 0, 405, '钛金属机身，A17 Pro 芯片，USB-C 接口。', '颜色:原色钛金属|蓝色钛金属|白色钛金属;存储:128G|256G|512G'),
       ('OPPO Find X7', 'findx7.jpg', 4499.00, 150, 1, 0, 132, '天玑 9300 平台，超光影三主摄。', ''),
       ('联想拯救者 Y9000P', 'y9000p.jpg', 9299.00, 40, 2, 0, 95, 'i9-14900HX + RTX 4060，电竞级散热。', '配置:i7+RTX4060|i9+RTX4060|i9+RTX4070'),
       ('华为 MateBook X Pro', 'matebookxpro.jpg', 13999.00, 25, 2, 0, 68, '3.1K 原色全面屏，980 克超轻机身。', ''),
       ('MacBook Air M3', 'macbookairm3.jpg', 8999.00, 55, 2, 0, 187, 'M3 芯片，18 小时续航，1.24kg 轻薄本。', '颜色:午夜色|星光色|银色;内存:8G+256G|16G+512G'),
       ('iPad Pro 12.9', 'ipadpro.jpg', 8499.00, 70, 3, 0, 143, 'M2 芯片，Liquid 视网膜 XDR 显示屏。', '颜色:深空灰|银色;存储:128G|256G'),
       ('华为 MatePad Pro', 'matepadpro.jpg', 4299.00, 90, 3, 0, 76, '12.6 英寸 OLED 大屏，多设备协同。', ''),
       ('Apple Watch Series 9', 'watchs9.jpg', 2999.00, 110, 4, 0, 254, 'S9 芯片，双指互点隔空操作。', '表壳:41mm|45mm;颜色:午夜色|星光色'),
       ('华为 Watch GT 4', 'watchgt4.jpg', 1488.00, 200, 4, 0, 312, '八天长续航，百种运动模式。', ''),
       ('AirPods Pro 2', 'airpodspro2.jpg', 1899.00, 180, 5, 0, 489, 'H2 芯片，自适应降噪，USB-C 充电盒。', ''),
       ('索尼 WH-1000XM5', 'sonyxm5.jpg', 2499.00, 95, 5, 0, 178, '行业标杆级降噪，30 小时续航。', '颜色:黑色|铂金银'),
       ('Bose  SoundLink Flex', 'boseflex.jpg', 1299.00, 130, 5, 0, 92, 'IP67 防水，便携蓝牙音箱。', '颜色:黑色|白色烟灰|柑橘蓝');

-- ---------- 收藏（演示数据） ----------
INSERT INTO `focustable` (`busertable_id`, `goodstable_id`)
VALUES (1, 1),
       (1, 3),
       (2, 11);

-- ---------- 购物车（演示数据） ----------
-- spec：把各规格维度选中的选项用 " / " 连接，无规格商品为空串
INSERT INTO `carttable` (`busertable_id`, `goodstable_id`, `shoppingnum`, `spec`)
VALUES (1, 2, 1, '黑色 / 12+256G'),
       (1, 12, 2, ''),
       (2, 5, 1, 'i9+RTX4060');

-- ---------- 订单（演示数据） ----------
-- 说明：
--   1) 下单时间用「NOW() - INTERVAL n DAY」相对生成，而不是写死某一天，
--      这样无论什么时候导入脚本，统计报表（近 7 天 / 近 30 天订单趋势）
--      都能取到数据，不会因为日期过期变成空图。
--   2) 订单号按「下单日期 + 序号」拼成，保证唯一（uk_ordernumber）。
--   3) amount 先留 0，等明细插入后用 orderdetail 汇总回填，
--      保证「订单总金额」与「明细金额之和」永远对得上。
--   4) id 严格按「下单时间由远到近」递增，这样"我的订单""订单管理"
--      等页面按 id 倒序分页时，看到的就是最新的订单排在最前面。
INSERT INTO `orderbasestable` (`id`, `busertable_id`, `ordernumber`, `amount`, `status`, `datetime`)
VALUES (1, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 13 DAY, '%Y%m%d'), '100001'), 0, 4, NOW() - INTERVAL 13 DAY),
       (2, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 12 DAY, '%Y%m%d'), '100002'), 0, 4, NOW() - INTERVAL 12 DAY),
       (3, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 10 DAY, '%Y%m%d'), '100003'), 0, 4, NOW() - INTERVAL 10 DAY),
       (4, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 9 DAY, '%Y%m%d'), '100004'), 0, 4, NOW() - INTERVAL 9 DAY),
       (5, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 7 DAY, '%Y%m%d'), '100005'), 0, 3, NOW() - INTERVAL 7 DAY),
       (6, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 6 DAY, '%Y%m%d'), '100006'), 0, 4, NOW() - INTERVAL 6 DAY),
       (7, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 6 DAY, '%Y%m%d'), '100007'), 0, 4, NOW() - INTERVAL 6 DAY),
       (8, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 6 DAY, '%Y%m%d'), '100008'), 0, 4, NOW() - INTERVAL 6 DAY),
       (9, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 5 DAY, '%Y%m%d'), '100009'), 0, 3, NOW() - INTERVAL 5 DAY),
       (10, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 4 DAY, '%Y%m%d'), '100010'), 0, 2, NOW() - INTERVAL 4 DAY),
       (11, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 4 DAY, '%Y%m%d'), '100011'), 0, 4, NOW() - INTERVAL 4 DAY),
       (12, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 3 DAY, '%Y%m%d'), '100012'), 0, 4, NOW() - INTERVAL 3 DAY),
       (13, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 3 DAY, '%Y%m%d'), '100013'), 0, 3, NOW() - INTERVAL 3 DAY),
       (14, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 2 DAY, '%Y%m%d'), '100014'), 0, 1, NOW() - INTERVAL 2 DAY),
       (15, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 2 DAY, '%Y%m%d'), '100015'), 0, 4, NOW() - INTERVAL 2 DAY),
       (16, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 1 DAY, '%Y%m%d'), '100016'), 0, 2, NOW() - INTERVAL 1 DAY),
       (17, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 1 DAY, '%Y%m%d'), '100017'), 0, 5, NOW() - INTERVAL 1 DAY),
       (18, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 1 DAY, '%Y%m%d'), '100018'), 0, 2, NOW() - INTERVAL 1 DAY),
       (19, 1, CONCAT(DATE_FORMAT(NOW(), '%Y%m%d'), '100019'), 0, 2, NOW()),
       (20, 1, CONCAT(DATE_FORMAT(NOW(), '%Y%m%d'), '100020'), 0, 4, NOW()),
       -- 以下为历史月份订单（覆盖最近半年），用于"按月销量统计"的月度趋势图。
       -- 用 NOW() - INTERVAL n MONTH 相对生成，保证任何时间导入都有近半年的数据。
       (21, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y%m%d'), '100021'), 0, 4, NOW() - INTERVAL 5 MONTH),
       (22, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y%m%d'), '100022'), 0, 4, NOW() - INTERVAL 5 MONTH),
       (23, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y%m%d'), '100023'), 0, 4, NOW() - INTERVAL 4 MONTH),
       (24, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y%m%d'), '100024'), 0, 4, NOW() - INTERVAL 4 MONTH),
       (25, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y%m%d'), '100025'), 0, 4, NOW() - INTERVAL 3 MONTH),
       (26, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y%m%d'), '100026'), 0, 4, NOW() - INTERVAL 3 MONTH),
       (27, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y%m%d'), '100027'), 0, 4, NOW() - INTERVAL 2 MONTH),
       (28, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y%m%d'), '100028'), 0, 4, NOW() - INTERVAL 2 MONTH),
       (29, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y%m%d'), '100029'), 0, 4, NOW() - INTERVAL 1 MONTH),
       (30, 2, CONCAT(DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y%m%d'), '100030'), 0, 4, NOW() - INTERVAL 1 MONTH),
       (31, 1, CONCAT(DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y%m%d'), '100031'), 0, 4, NOW() - INTERVAL 1 MONTH);

-- 订单明细：price 为下单时的成交单价快照，spec 为规格快照
INSERT INTO `orderdetail` (`orderbase_id`, `goodstable_id`, `goodsname`, `price`, `shoppingnum`, `spec`)
VALUES (1, 1, '华为 Mate 60 Pro', 6999.00, 1, '雅川青 / 12+256G'),
       (2, 12, 'AirPods Pro 2', 1899.00, 2, ''),
       (3, 7, 'MacBook Air M3', 8999.00, 1, '午夜色 / 16G+512G'),
       (4, 11, '华为 Watch GT 4', 1488.00, 1, ''),
       (5, 3, 'iPhone 15 Pro', 7999.00, 1, '原色钛金属 / 256G'),
       (6, 10, 'Apple Watch Series 9', 2999.00, 1, '45mm / 午夜色'),
       (7, 12, 'AirPods Pro 2', 1899.00, 1, ''),
       (8, 1, '华为 Mate 60 Pro', 6999.00, 1, '白沙银 / 12+512G'),
       (9, 8, 'iPad Pro 12.9', 8499.00, 1, '深空灰 / 256G'),
       (9, 12, 'AirPods Pro 2', 1899.00, 1, ''),
       (10, 5, '联想拯救者 Y9000P', 9299.00, 1, 'i9+RTX4060'),
       (11, 11, '华为 Watch GT 4', 1488.00, 1, ''),
       (12, 13, '索尼 WH-1000XM5', 2499.00, 1, '铂金银'),
       (13, 13, '索尼 WH-1000XM5', 2499.00, 1, '黑色'),
       (14, 9, '华为 MatePad Pro', 4299.00, 1, ''),
       (15, 10, 'Apple Watch Series 9', 2999.00, 1, '41mm / 星光色'),
       (16, 2, '小米 14 Ultra', 6499.00, 1, '黑色 / 16+512G'),
       (17, 14, 'Bose  SoundLink Flex', 1299.00, 1, '柑橘蓝'),
       (18, 6, '华为 MateBook X Pro', 13999.00, 1, ''),
       (19, 12, 'AirPods Pro 2', 1899.00, 1, ''),
       (19, 11, '华为 Watch GT 4', 1488.00, 1, ''),
       (20, 7, 'MacBook Air M3', 8999.00, 1, '星光色 / 8G+256G'),
       (21, 3, 'iPhone 15 Pro', 7999.00, 1, '蓝色钛金属 / 128G'),
       (22, 12, 'AirPods Pro 2', 1899.00, 1, ''),
       (23, 7, 'MacBook Air M3', 8999.00, 1, '午夜色 / 8G+256G'),
       (24, 10, 'Apple Watch Series 9', 2999.00, 1, '45mm / 星光色'),
       (25, 1, '华为 Mate 60 Pro', 6999.00, 1, '雅丹黑 / 12+256G'),
       (26, 11, '华为 Watch GT 4', 1488.00, 2, ''),
       (27, 13, '索尼 WH-1000XM5', 2499.00, 1, '黑色'),
       (28, 8, 'iPad Pro 12.9', 8499.00, 1, '银色 / 128G'),
       (29, 5, '联想拯救者 Y9000P', 9299.00, 1, 'i7+RTX4060'),
       (30, 2, '小米 14 Ultra', 6499.00, 1, '白色 / 12+256G'),
       (31, 14, 'Bose  SoundLink Flex', 1299.00, 1, '黑色');

-- 用明细汇总回填订单总金额，保证主表与明细永远一致
UPDATE `orderbasestable` ob
SET ob.amount = (SELECT IFNULL(SUM(od.price * od.shoppingnum), 0)
                 FROM `orderdetail` od
                 WHERE od.orderbase_id = ob.id);


-- =====================================================================
--  执行完毕。校验语句（可选执行）：
--    SELECT COUNT(*) FROM goodstable;        -- 应为 14
--    SELECT COUNT(*) FROM goodstype;         -- 应为 5
--    SELECT aname FROM ausertable;           -- 应为 admin
--    SELECT COUNT(*) FROM orderbasestable;   -- 应为 31
--    SELECT COUNT(*) FROM orderdetail;       -- 应为 33
--    -- 主表金额应等于明细金额之和（自洽性校验）：均为 168810.00
--    SELECT (SELECT SUM(amount) FROM orderbasestable) AS 主表,
--           (SELECT SUM(price * shoppingnum) FROM orderdetail) AS 明细;
--    -- 有效订单金额（status IN 2,3,4）应为 163212.00
-- =====================================================================
