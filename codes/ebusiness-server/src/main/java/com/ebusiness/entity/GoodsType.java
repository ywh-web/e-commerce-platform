package com.ebusiness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品类型实体 —— 对应表 goodstype
 *
 * @author 杨伟宏
 */
@Data
@TableName("goodstype")
public class GoodsType implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 类型主键（自增） */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 类型名称 */
    private String name;

    /** 该类型下的商品数量（统计用，非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Long goodsCount;
}
