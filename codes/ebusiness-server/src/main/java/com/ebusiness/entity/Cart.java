package com.ebusiness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车实体 —— 对应表 carttable
 *
 * <p>同一用户对同一商品只保留一条记录；再次加购时在 Service 层累加 shoppingnum。
 *
 * @author 杨伟宏
 */
@Data
@TableName("carttable")
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 购物车记录主键（自增） */
    @TableId(type = IdType.AUTO)
    private Integer cid;

    /** 所属用户 ID（外键 → busertable.bid） */
    private Integer busertableId;

    /** 商品 ID（外键 → goodstable.id） */
    private Integer goodstableId;

    /** 购买数量 */
    private Integer shoppingnum;

    /**
     * 选中的规格，如 {@code 幻夜黑 / 12+256G}。
     *
     * <p>无规格商品存空串（不存 NULL），这样"同一用户 + 同一商品 + 同一规格"
     * 可以用等值条件精确匹配，避免不同规格被错误合并成一条。
     */
    private String spec;

    // ---------------- 以下为传输用字段，不对应数据库列 ----------------

    /** 批量更新数量时提交的 cid 数组 */
    @TableField(exist = false)
    private List<Integer> bcid;

    /** 批量更新数量时提交的数量数组（与 bcid 一一对应） */
    @TableField(exist = false)
    private List<Integer> bshoppingnum;

    /** 关联查询出的商品名称 */
    @TableField(exist = false)
    private String goodsName;

    /** 关联查询出的商品单价 */
    @TableField(exist = false)
    private BigDecimal price;

    /** 关联查询出的商品图片 */
    @TableField(exist = false)
    private String picture;

    /** 该条购物车记录的金额小计 = 单价 × 数量 */
    @TableField(exist = false)
    private BigDecimal subtotal;
}
