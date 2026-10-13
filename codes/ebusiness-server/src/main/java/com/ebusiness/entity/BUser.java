package com.ebusiness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 前台注册用户实体 —— 对应表 busertable
 *
 * <p>邮箱是业务主键，注册与登录都以邮箱为凭据。
 * <p>密码经 MD5 加密后存入 bpwd 字段，数据库中不存明文。
 *
 * @author 杨伟宏
 */
@Data
@TableName("busertable")
public class BUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户主键（自增），前端业务里叫 bid */
    @TableId(type = IdType.AUTO)
    private Integer bid;

    /** 注册邮箱（唯一，登录凭据） */
    private String bemail;

    /** 登录密码（MD5 密文） */
    private String bpwd;

    /** 昵称 */
    private String bname;

    /** 手机号 */
    private String bphone;

    /** 收货地址 */
    private String baddr;

    // ---------------- 以下为传输用字段，不对应数据库列 ----------------

    /** 登录/注册时前端传入的图形验证码 */
    @TableField(exist = false)
    private String code;

    /** 注册/修改资料时前端传入的确认密码（仅用于后端二次校验） */
    @TableField(exist = false)
    private String confirmPwd;
}
