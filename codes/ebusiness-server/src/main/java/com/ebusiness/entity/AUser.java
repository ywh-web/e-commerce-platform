package com.ebusiness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员实体 —— 对应表 ausertable
 *
 * <p>仅后台管理端登录使用，账号密码由系统预置，不开放注册。
 *
 * @author 杨伟宏
 */
@Data
@TableName("ausertable")
public class AUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 管理员主键（自增） */
    @TableId(type = IdType.AUTO)
    private Integer aid;

    /** 管理员账号（登录凭据，唯一） */
    private String aname;

    /** 管理员密码（数据库中存 MD5 密文） */
    private String apwd;

    /** 登录时前端传入的图形验证码，非数据库字段 */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String code;
}
