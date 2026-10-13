package com.ebusiness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.Cart;

/**
 * 购物车业务接口
 *
 * @author 杨伟宏
 */
public interface CartService extends IService<Cart> {

    /**
     * 加入购物车
     *
     * <p>同一用户对同一商品：已存在则累加数量，否则新增一条记录。
     *
     * @param cart 含 busertableId、goodstableId、shoppingnum
     * @return 操作结果
     */
    ResponseResult<Object> addCart(Cart cart);

    /**
     * 查询某用户的购物车（联表带出商品名称/单价/图片/小计）
     *
     * @param busertableId 用户 ID
     * @return 购物车列表
     */
    ResponseResult<Object> getCartByUid(Integer busertableId);

    /**
     * 批量修改购物车数量
     *
     * @param cart 含 bcid（cid 列表）与 bshoppingnum（对应数量列表）
     * @return 操作结果
     */
    ResponseResult<Object> updateCart(Cart cart);

    /**
     * 删除单条购物车记录
     *
     * @param cid 购物车记录 ID
     * @return 操作结果
     */
    ResponseResult<Object> deleteCart(Integer cid);

    /**
     * 清空某用户的购物车
     *
     * @param cart 含 busertableId
     * @return 操作结果
     */
    ResponseResult<Object> clearCart(Cart cart);

    /**
     * 购物车商品件数（前台导航栏角标用）
     *
     * @param busertableId 用户 ID
     * @return 商品总件数
     */
    ResponseResult<Integer> getCartCount(Integer busertableId);
}
