package com.ebusiness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.AUser;

/**
 * 管理员业务接口
 *
 * @author 杨伟宏
 */
public interface AUserService extends IService<AUser> {

    /**
     * 管理员登录
     *
     * @param aUser 含 aname（账号）、apwd（明文密码）、code（验证码）等
     * @return data 中携带 token、aname、role
     */
    ResponseResult<Object> login(AUser aUser);
}
