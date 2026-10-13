package com.ebusiness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单详情实体 —— 对应表 orderdetail
 *
 * <p>goodsname 与 price 是下单时的快照，商品后续改价不影响历史订单金额。
 *
 * @author 杨伟宏
 */
@Data
@TableName("orderdetail")
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 明细主键（自增） */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 所属订单 ID（外键 → orderbasestable.id） */
    private Integer orderbaseId;

    /** 商品 ID（外键 → goodstable.id） */
    private Integer goodstableId;

    /** 商品名称快照 */
    private String goodsname;

    /** 成交单价快照（元） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer shoppingnum;

    /** 规格快照，如 {@code 幻夜黑 / 12+256G}；无规格商品为空串 */
    private String spec;

    // ---------------- 以下为传输用字段，不对应数据库列 ----------------

    /** 商品图片（订单详情展示用） */
    @TableField(exist = false)
    private String picture;

    /** 明细行小计 = 单价 × 数量 */
    @TableField(exist = false)
    private BigDecimal subtotal;
}
