package com.ebusiness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.BUser;

/**
 * 前台用户业务接口
 *
 * @author 杨伟宏
 */
public interface BUserService extends IService<BUser> {

    /**
     * 用户注册
     *
     * @param bUser 含 bemail、bpwd、bname 等
     * @return 注册结果
     */
    ResponseResult<Object> register(BUser bUser);

    /**
     * 用户登录
     *
     * @param bUser 含 bemail、bpwd、code（验证码）
     * @return data 中携带 token、bemail、bname、bid、role
     */
    ResponseResult<Object> login(BUser bUser);

    /**
     * 查询用户资料
     *
     * @param bid 用户 ID
     * @return 用户信息（密码字段已清空）
     */
    ResponseResult<Object> getInfo(Integer bid);

    /**
     * 修改用户资料
     *
     * @param bUser 含 bid 及待修改字段
     * @return 修改结果
     */
    ResponseResult<Object> updateInfo(BUser bUser);

    /**
     * 修改密码
     *
     * @param bid     用户 ID
     * @param oldPwd  原密码（明文）
     * @param newPwd  新密码（明文）
     * @return 修改结果
     */
    ResponseResult<Object> updatePassword(Integer bid, String oldPwd, String newPwd);

    /**
     * 后台用户列表（分页）
     *
     * @param currentPage 页码，从 1 开始
     * @param bUser       可选筛选条件（邮箱/昵称模糊匹配）
     * @return data 含 users 数组与 totalPage 总页数
     */
    ResponseResult<Object> getAllUsers(Integer currentPage, BUser bUser);

    /**
     * 后台删除用户
     *
     * @param bUser 含 bid
     * @return 删除结果；该用户有订单时不允许删除
     */
    ResponseResult<Object> delete(BUser bUser);
}
