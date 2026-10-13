package com.ebusiness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品收藏实体 —— 对应表 focustable
 *
 * <p>(busertable_id, goodstable_id) 上有唯一索引，防止重复收藏。
 *
 * @author 杨伟宏
 */
@Data
@TableName("focustable")
public class Focus implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收藏记录主键（自增） */
    @TableId(type = IdType.AUTO)
    private Integer fid;

    /** 所属用户 ID（外键 → busertable.bid） */
    private Integer busertableId;

    /** 商品 ID（外键 → goodstable.id） */
    private Integer goodstableId;

    // ---------------- 以下为传输用字段，不对应数据库列 ----------------

    /** 关联查询出的商品名称 */
    @TableField(exist = false)
    private String goodsName;

    /** 关联查询出的商品单价 */
    @TableField(exist = false)
    private BigDecimal price;

    /** 关联查询出的商品图片 */
    @TableField(exist = false)
    private String picture;

    /** 关联查询出的商品库存 */
    @TableField(exist = false)
    private Integer stock;

    /** 关联查询出的商品简介 */
    @TableField(exist = false)
    private String introduce;
}
