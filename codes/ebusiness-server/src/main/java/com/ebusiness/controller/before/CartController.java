package com.ebusiness.controller.before;

import com.ebusiness.common.security.UserContext;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.Cart;
import com.ebusiness.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 前台购物车控制器
 *
 * <p>接口前缀：/api/before/cart
 * <p>所有接口都从 token 取当前用户 ID，不信任前端传参，避免越权操作他人购物车。
 *
 * @author 杨伟宏
 */
@RestController
@RequestMapping("/api/before/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 加入购物车
     *
     * <p>POST /api/before/cart/add
     * <p>请求体：{ "goodstableId": 1, "shoppingnum": 2 }
     */
    @PostMapping("/add")
    public ResponseResult<Object> add(@RequestBody Cart cart) {
        cart.setBusertableId(UserContext.getUid());
        return cartService.addCart(cart);
    }

    /**
     * 查询当前用户购物车
     *
     * <p>GET /api/before/cart/list
     */
    @GetMapping("/list")
    public ResponseResult<Object> list() {
        return cartService.getCartByUid(UserContext.getUid());
    }

    /**
     * 批量修改购物车数量
     *
     * <p>POST /api/before/cart/update
     * <p>请求体：{ "bcid": [1,2], "bshoppingnum": [3,5] }
     */
    @PostMapping("/update")
    public ResponseResult<Object> update(@RequestBody Cart cart) {
        return cartService.updateCart(cart);
    }

    /**
     * 删除单条购物车记录
     *
     * <p>DELETE /api/before/cart/delete/{cid}
     */
    @DeleteMapping("/delete/{cid}")
    public ResponseResult<Object> delete(@PathVariable Integer cid) {
        return cartService.deleteCart(cid);
    }

    /**
     * 清空购物车
     *
     * <p>POST /api/before/cart/clear
     */
    @PostMapping("/clear")
    public ResponseResult<Object> clear() {
        Cart cart = new Cart();
        cart.setBusertableId(UserContext.getUid());
        return cartService.clearCart(cart);
    }

    /**
     * 购物车商品总件数（导航栏角标）
     *
     * <p>GET /api/before/cart/count
     */
    @GetMapping("/count")
    public ResponseResult<Object> count() {
        return ResponseResult.success(cartService.getCartCount(UserContext.getUid()).getData());
    }
}
