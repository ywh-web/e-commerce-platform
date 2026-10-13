package com.ebusiness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单基础实体 —— 对应表 orderbasestable
 *
 * <p>下单流程：选购物车商品 → 生成一条订单基础记录 → 循环生成订单详情 →
 * 扣减商品库存与增加销量 → 清空已下单的购物车记录。
 *
 * @author 杨伟宏
 */
@Data
@TableName("orderbasestable")
public class OrderBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单主键（自增） */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 下单用户 ID（外键 → busertable.bid） */
    private Integer busertableId;

    /** 订单号（唯一，由时间戳生成） */
    private String ordernumber;

    /** 订单总金额（元） */
    private BigDecimal amount;

    /** 订单状态：1待付款 2已付款 3已发货 4已完成 5已取消 */
    private Integer status;

    /** 下单时间 */
    private LocalDateTime datetime;

    // ---------------- 以下为传输用字段，不对应数据库列 ----------------

    /** 当前页码（分页查询用） */
    @TableField(exist = false)
    private Integer currentPage = 1;

    /** 下单用户邮箱（后台订单列表展示用） */
    @TableField(exist = false)
    private String bemail;

    /** 下单用户昵称（后台订单列表展示用） */
    @TableField(exist = false)
    private String bname;
}
