package com.ebusiness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品实体 —— 对应表 goodstable
 *
 * @author 杨伟宏
 */
@Data
@TableName("goodstable")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品主键（自增），前端业务里叫 id / gid */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 商品名称 */
    private String name;

    /** 商品图片文件名（存放在后端静态资源 upload 目录） */
    private String picture;

    /** 商品单价（元），用 BigDecimal 避免浮点精度丢失 */
    private BigDecimal price;

    /** 库存数量 */
    private Integer stock;

    /** 所属类型 ID（外键 → goodstype.id） */
    private Integer goodstypeId;

    /** 是否下架：0-上架 1-下架 */
    private Integer isoff;

    /** 累计销量 */
    private Integer sales;

    /** 商品简介 / 详情描述 */
    private String introduce;

    /**
     * 可选规格定义，格式：{规格名}:{选项1}|{选项2};{规格名}:{选项1}|{选项2}
     *
     * <p>例：{@code 颜色:幻夜黑|星河银;版本:12+256G|12+512G}
     * <p>为空表示该商品无规格可选，详情页不展示规格选择器。
     */
    private String specs;

    // ---------------- 以下为传输用字段，不对应数据库列 ----------------

    /** 批量上下架时提交的商品 ID 数组 */
    @TableField(exist = false)
    private List<Integer> ids;

    /** 新增/修改商品时的操作类型："add" 表示新增，其余表示修改 */
    @TableField(exist = false)
    private String act;

    /** 上传的图片文件（新增/修改商品时使用） */
    @TableField(exist = false)
    private MultipartFile logoFile;

    /** 上传的图片原始文件名 */
    @TableField(exist = false)
    private String fileName;

    /** 类型名称（列表展示用，由关联查询填充） */
    @TableField(exist = false)
    private String typeName;

    /** 当前页码（分页查询用） */
    @TableField(exist = false)
    private Integer currentPage = 1;

    /** 搜索关键词（按商品名称模糊匹配） */
    @TableField(exist = false)
    private String keywords;
}
